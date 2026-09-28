"""Render built slide decks in headless Chromium and report problems a parser cannot see.

For each deck HTML file: every slide is shown fully revealed at a projector size (1280x720) and a
phone size (390x844, width only matters for layout). Reports page errors, console errors, KaTeX
errors, engine error boxes, non-local requests, slides whose content overflows the stage, and
(at projector size) code blocks, tables or math displays whose text runs past their own box.
Local loopback HTTP only; nothing is deployed or fetched from another host.

Usage: .venv-qa/bin/python tools/check_slides.py [--chromium PATH] [--root DIR] DECK.html ...
Paths are resolved against --root (default: the common parent), which is served over loopback so
relative KaTeX and visualization links resolve as they will on the site.
"""
from pathlib import Path
import argparse
import functools
import http.server
import json
import sys
import threading
from urllib.parse import urlsplit

VIEWPORTS = {"projector": (1280, 720), "phone": (390, 844)}
# Present-mode overflow above this many CSS pixels is reported; a few pixels are rounding.
OVERFLOW_TOLERANCE = 4
# The engine shrinks type to fit before it scrolls; below this scale text is hard to read in a room.
SMALL_SCALE = 0.8

MEASURE = """async () => {
  const v = window.viewer, S = v.S, out = [], wait = ms => new Promise(r => setTimeout(r, ms));
  for (let i = 0; i < S.slides.length; i++) {
    v.go(i); await wait(350);
    const node = document.querySelector('.stage .slide');
    const stage = document.querySelector('.stage');
    const errs = [...document.querySelectorAll('.stage .err, .stage .katex-error')].map(e => (e.getAttribute('title') || e.textContent).slice(0, 160));
    const rawTex = /\\$[^$\\s][^$]*\\$/.test(node ? node.innerText : '') ? 1 : 0;
    // Code, tables and math displays whose text runs past their own box (the slide itself may still fit).
    const spill = node ? Math.max(0, ...[...node.querySelectorAll('pre, table, .katex-display')].map(e => e.scrollWidth - e.clientWidth)) : 0;
    out.push({ i: i + 1, title: S.slides[i].title || '', scroll: node ? node.scrollHeight - node.clientHeight : -1,
               wide: node ? node.scrollWidth - node.clientWidth : -1, stage: stage ? stage.clientHeight : 0, errs, rawTex, spill,
               scale: node ? parseFloat(node.style.getPropertyValue('--scale') || '1') : 1 });
  }
  return out;
}"""


class Quiet(http.server.SimpleHTTPRequestHandler):
    def log_message(self, *args): pass


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("decks", nargs="+")
    ap.add_argument("--chromium", default="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
    ap.add_argument("--root")
    ap.add_argument("--json", help="write the full result here")
    args = ap.parse_args(argv)
    from playwright.sync_api import sync_playwright
    decks = [Path(d).resolve() for d in args.decks]
    root = Path(args.root).resolve() if args.root else Path(*[p for p in zip(*[d.parts for d in decks]) if len(set(p)) == 1 for p in [p[0]]])
    server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), functools.partial(Quiet, directory=str(root)))
    threading.Thread(target=server.serve_forever, daemon=True).start()
    base = f"http://127.0.0.1:{server.server_address[1]}/"
    results, failed = [], False
    with sync_playwright() as pw:
        browser = pw.chromium.launch(executable_path=args.chromium)
        for label, (w, h) in VIEWPORTS.items():
            ctx = browser.new_context(viewport={"width": w, "height": h})
            for deck in decks:
                page = ctx.new_page(); problems = []
                page.on("pageerror", lambda e, p=problems: p.append("page error: " + str(e)))
                page.on("console", lambda m, p=problems: m.type == "error" and not m.location.get("url", "").endswith("favicon.ico") and p.append("console: " + m.text[:200]))
                def route(r, p=problems):
                    host = urlsplit(r.request.url).hostname
                    if host not in {"127.0.0.1", None} and not r.request.url.startswith(("data:", "blob:")):
                        p.append("non-local request: " + r.request.url[:160]); return r.abort()
                    return r.continue_()
                page.route("**/*", route)
                page.goto(base + deck.relative_to(root).as_posix() + "?reveal=none")
                page.wait_for_function("window.viewer && window.viewer.S.slides.length > 0", timeout=20000)
                page.wait_for_timeout(600)
                page.evaluate("() => window.viewer.go(0)"); page.wait_for_timeout(300)
                slides = page.evaluate(MEASURE)
                over = [s for s in slides if s["scroll"] > OVERFLOW_TOLERANCE] if label == "projector" else []
                wide = [s for s in slides if s["wide"] > OVERFLOW_TOLERANCE]
                errs = [s for s in slides if s["errs"] or s["rawTex"]]
                small = [s for s in slides if s["scale"] < SMALL_SCALE] if label == "projector" else []
                spill = [s for s in slides if s["spill"] > OVERFLOW_TOLERANCE] if label == "projector" else []
                if problems or over or errs or spill: failed = True
                results.append({"deck": str(deck.relative_to(root)), "viewport": label, "slides": len(slides), "problems": problems,
                                "overflow": [(s["i"], s["title"], s["scroll"]) for s in over],
                                "wide": [(s["i"], s["title"], s["wide"]) for s in wide],
                                "spill": [(s["i"], s["title"], s["spill"]) for s in spill],
                                "small": [(s["i"], s["title"], round(s["scale"], 2)) for s in small],
                                "errors": [(s["i"], s["title"], s["errs"], s["rawTex"]) for s in errs]})
                page.close()
            ctx.close()
        browser.close()
    server.shutdown()
    for r in results:
        status = "FAIL" if r["problems"] or r["overflow"] or r["errors"] or r["spill"] else "ok"
        print(f"{status:4} {r['viewport']:9} {r['deck']}  slides={r['slides']}")
        for p in r["problems"]: print("      ", p)
        for s in r["overflow"]: print(f"       overflow slide {s[0]} '{s[1]}' by {s[2]}px")
        for s in r["spill"]: print(f"       code/table/math wider than its box on slide {s[0]} '{s[1]}' by {s[2]}px")
        for s in r.get("small", []): print(f"       warning: slide {s[0]} '{s[1]}' shrunk to scale {s[2]}")
        for s in r["errors"]: print(f"       render error slide {s[0]} '{s[1]}': {s[2]} rawTeX={s[3]}")
        if r["viewport"] == "phone" and r["wide"]: print(f"       note: {len(r['wide'])} slide(s) scroll sideways on a phone")
    if args.json: Path(args.json).write_text(json.dumps(results, indent=2) + "\n", encoding="utf-8")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
