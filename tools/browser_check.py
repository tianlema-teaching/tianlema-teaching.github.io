"""Load every built page and deck in headless Chrome at a laptop and a phone size.

Serves docs/ on loopback only. Fails on HTTP errors, page errors, console errors, any request to
another host (blocked and reported), a template page without exactly one h1, a template page that
scrolls sideways on a phone, or a deck that does not mount. Per-slide rendering of decks is checked
by tools/check_slides.py. Writes a summary to reports/browser.json.

Usage: .venv/bin/python tools/browser_check.py [--chromium PATH]
"""
from pathlib import Path
from urllib.parse import urlsplit
import argparse
import functools
import http.server
import json
import sys
import threading

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
VIEWPORTS = {"laptop": (1280, 800), "phone": (390, 844)}


class Quiet(http.server.SimpleHTTPRequestHandler):
    def log_message(self, *args): pass


def routes() -> list[str]:
    out = []
    for page in sorted(DOCS.rglob("*.html")):
        rel = page.relative_to(DOCS).as_posix()
        if rel == "404.html": continue
        out.append("/" + (rel[: -len("index.html")] if rel.endswith("index.html") and "visualizations" not in rel else rel))
    return out


def main(argv=None) -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--chromium", default="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
    args = ap.parse_args(argv)
    from playwright.sync_api import sync_playwright
    server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), functools.partial(Quiet, directory=str(DOCS)))
    threading.Thread(target=server.serve_forever, daemon=True).start()
    base = f"http://127.0.0.1:{server.server_address[1]}"
    results, errors = [], []
    with sync_playwright() as pw:
        browser = pw.chromium.launch(executable_path=args.chromium)
        for label, (w, h) in VIEWPORTS.items():
            ctx = browser.new_context(viewport={"width": w, "height": h})
            for route in routes():
                page = ctx.new_page(); problems = []
                page.on("pageerror", lambda e, p=problems: p.append("page error: " + str(e)[:160]))
                page.on("console", lambda m, p=problems: m.type == "error" and p.append("console: " + m.text[:160]))
                def block(r, p=problems):
                    if urlsplit(r.request.url).hostname not in {"127.0.0.1", None} and not r.request.url.startswith(("data:", "blob:")):
                        p.append("non-local request: " + r.request.url[:160]); return r.abort()
                    return r.continue_()
                page.route("**/*", block)
                response = page.goto(base + route, wait_until="networkidle")
                item = {"route": route, "viewport": label, "status": response.status if response else None}
                if "/slides/" in route:
                    page.wait_for_function("window.viewer && window.viewer.S.slides.length > 0", timeout=20000)
                    item["slides"] = page.evaluate("window.viewer.S.slides.length")
                elif "/visualizations/" not in route:
                    item["h1"] = page.locator("h1").count()
                    item["sideways"] = page.evaluate("document.documentElement.scrollWidth > innerWidth + 1")
                    if item["h1"] != 1: problems.append(f"{item['h1']} h1 elements")
                    if label == "phone" and item["sideways"]: problems.append("page scrolls sideways on a phone")
                if item["status"] != 200: problems.append(f"HTTP {item['status']}")
                item["problems"] = problems
                results.append(item)
                if problems: errors.append(item)
                page.close()
            ctx.close()
        browser.close()
    server.shutdown()
    summary = {"status": "pass" if not errors else "fail", "checks": len(results),
               "decks": len({r["route"] for r in results if "slides" in r}), "errors": errors,
               "scope": "built docs/ served on loopback; not the live site, not a full accessibility audit"}
    (ROOT / "reports").mkdir(exist_ok=True)
    (ROOT / "reports/browser.json").write_text(json.dumps({**summary, "results": results}, indent=2) + "\n")
    print(json.dumps(summary, indent=2))
    return 0 if not errors else 1


if __name__ == "__main__":
    sys.exit(main())
