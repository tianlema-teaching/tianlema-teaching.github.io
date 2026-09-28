"""Build the teaching site into docs/, which GitHub Pages serves as-is.

Pages: every src/**/*.md becomes an HTML page (index.md -> the folder's index.html, name.md ->
name/index.html), rendered with markdown-it (CommonMark plus tables) into one shared template.
Static files: everything under static/ is copied unchanged (KaTeX, visualizations).
Decks: with --slides, every slides/<course>/<term>/decks/*.md is built by the instructor's slides
engine (default ~/tools/teaching/slides, or $SLIDES_ENGINE) into docs/<course>/<term>/slides/, and
provenance.json records each deck's source digest and the engine version and commit. Without
--slides the published decks are kept as they are (tests check they are current).

Usage: .venv/bin/python tools/build.py [--slides] [--check]
  --check  build into a temporary folder and report every file that would change; no writes.
Nothing is fetched, pushed or deployed.
"""
from pathlib import Path
import argparse
import hashlib
import html
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile

from markdown_it import MarkdownIt

ROOT = Path(__file__).resolve().parents[1]
SRC, STATIC, SLIDES, OUT = ROOT / "src", ROOT / "static", ROOT / "slides", ROOT / "docs"
CONFIG = json.loads((ROOT / "site.json").read_text(encoding="utf-8"))
MD = MarkdownIt("commonmark", {"html": False, "linkify": False, "typographer": False}).enable("table")

NAV = [("/", "Home"), ("/csi3620/2026-fall/", "CSI 3620"), ("/csi3620/2026-fall/lectures/", "CSI 3620 slides"),
       ("/csi4130-5130/2026-fall/", "CSI 4130/5130"), ("/visualizations/algorithms/index.html", "Visualizations")]
# Short folder pages that send visitors to the current term.
CURRENT = {"csi3620": "2026-fall", "csi4130-5130": "2026-fall"}

TEMPLATE = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="robots" content="noindex, nofollow">
<title>{title} | Tianle Ma, Teaching</title>
<meta name="description" content="{description}">
<link rel="icon" href="/favicon.svg" type="image/svg+xml">
<link rel="stylesheet" href="/style.css">
</head>
<body>
<a class="skip" href="#main">Skip to content</a>
<header class="site">
<a class="brand" href="/">Tianle Ma &middot; Teaching</a>
<nav aria-label="Site">{nav}</nav>
</header>
<main id="main">
<h1>{title}</h1>
{body}
</main>
<footer class="site">
<p>Draft course material. The instructor's own site, not an official Oakland University site. Enrolled students: Moodle is authoritative.</p>
</footer>
</body>
</html>
"""

REDIRECT = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="robots" content="noindex, nofollow">
<meta http-equiv="refresh" content="0; url={target}">
<title>{label}</title>
<link rel="stylesheet" href="/style.css">
</head>
<body>
<main id="main">
<h1>{label}</h1>
<p>The current term is <a href="{target}">{term}</a>.</p>
</main>
</body>
</html>
"""


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def slug(text: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", re.sub(r"<[^>]+>", "", text).lower()).strip("-")


def front_matter(text: str) -> tuple[dict, str]:
    match = re.match(r"^---\n(.*?)\n---\n", text, re.S)
    if not match:
        raise SystemExit("page is missing its title/description front matter")
    meta = {}
    for line in match.group(1).splitlines():
        key, _, value = line.partition(":")
        meta[key.strip()] = json.loads(value.strip())
    return meta, text[match.end():]


def render(source: Path) -> tuple[str, bytes]:
    meta, body = front_matter(source.read_text(encoding="utf-8"))
    rel = source.relative_to(SRC)
    route = "/" + (rel.parent.as_posix() + "/" if rel.parent.as_posix() != "." else "")
    if rel.stem != "index":
        route += rel.stem + "/"
    content = MD.render(body)
    # Heading anchors, and a scrolling wrapper so wide tables never widen the page.
    content = re.sub(r"<h2>(.*?)</h2>", lambda m: f'<h2 id="{slug(m.group(1))}">{m.group(1)}</h2>', content)
    content = content.replace("<table>", '<div class="table-wrap"><table>').replace("</table>", "</table></div>")
    nav = "".join(f'<a href="{href}"{" aria-current=\"page\"" if href == route else ""}>{label}</a>' for href, label in NAV)
    page = TEMPLATE.format(title=html.escape(meta["title"]), description=html.escape(meta["description"]), nav=nav, body=content)
    return route.lstrip("/") + "index.html", page.encode("utf-8")


def expected_files() -> dict[str, bytes]:
    """Every file the page stage owns, keyed by its path under docs/."""
    files = {}
    for source in sorted(SRC.rglob("*.md")):
        name, data = render(source)
        files[name] = data
    for path in sorted(p for p in STATIC.rglob("*") if p.is_file() and p.name != ".DS_Store"):
        files[path.relative_to(STATIC).as_posix()] = path.read_bytes()
    for course, term in CURRENT.items():
        label = {"csi3620": "CSI 3620", "csi4130-5130": "CSI 4130/5130"}[course]
        target = f"/{course}/{term}/"
        files[f"{course}/index.html"] = REDIRECT.format(target=target, label=label, term=term.replace("-", " ").title()).encode()
    files["404.html"] = TEMPLATE.format(title="Page not found", description="Page not found.", nav="".join(f'<a href="{h}">{l}</a>' for h, l in NAV),
                                        body='<p>This page does not exist. Start from the <a href="/">home page</a>.</p>').encode()
    files["robots.txt"] = b"User-agent: *\nDisallow: /\n"
    files[".nojekyll"] = b""
    if CONFIG.get("custom_domain"):
        files["CNAME"] = (CONFIG["custom_domain"] + "\n").encode()
    return files


def engine() -> Path:
    path = Path(os.environ.get("SLIDES_ENGINE", "~/tools/teaching/slides")).expanduser().resolve()
    if not (path / "build.mjs").is_file():
        raise SystemExit(f"slides engine not found at {path}; set SLIDES_ENGINE")
    return path


def build_decks(eng: Path) -> tuple[dict[str, bytes], dict]:
    version = json.loads((eng / "package.json").read_text())["version"]
    commit = subprocess.run(["git", "-C", str(eng), "log", "-1", "--format=%H", "--", "."], capture_output=True, text=True).stdout.strip() or None
    dirty = subprocess.run(["git", "-C", str(eng), "status", "--porcelain", "--", "src", "build.mjs"], capture_output=True, text=True)
    ident = {"engine_version": version, "engine_commit": commit, "engine_source_clean": dirty.returncode == 0 and not dirty.stdout.strip()}
    built, records = {}, {}
    with tempfile.TemporaryDirectory() as tmp:
        for deck in sorted(SLIDES.glob("*/*/decks/*.md")):
            course, term = deck.parents[2].name, deck.parents[1].name
            copy = Path(tmp) / course / term / deck.name
            copy.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(deck, copy)
            run = subprocess.run(["node", str(eng / "build.mjs"), str(copy)], capture_output=True, text=True, timeout=120)
            target = copy.with_suffix(".html")
            if run.returncode != 0 or not target.is_file():
                raise SystemExit(f"build failed for {deck.relative_to(ROOT)}:\n{run.stdout}{run.stderr}")
            name = f"{course}/{term}/slides/{deck.stem}.html"
            built[name] = target.read_bytes()
            records[name] = {"built_from": deck.relative_to(ROOT).as_posix(), "deck_sha256": sha(deck.read_bytes()),
                             "published_sha256": sha(built[name]), **ident}
    return built, records


def main(argv=None) -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--slides", action="store_true", help="rebuild every lecture deck with the slides engine")
    ap.add_argument("--check", action="store_true", help="report files that would change; write nothing")
    args = ap.parse_args(argv)
    files = expected_files()
    decks, records = build_decks(engine()) if args.slides else ({}, {})
    published_decks = {p.relative_to(OUT).as_posix() for p in OUT.glob("*/*/slides/*.html")} if OUT.is_dir() else set()
    keep = set(decks) or published_decks
    wanted = {**files, **decks}
    if args.check:
        changed = sorted(n for n, d in wanted.items() if not (OUT / n).is_file() or (OUT / n).read_bytes() != d)
        stray = sorted(p.relative_to(OUT).as_posix() for p in OUT.rglob("*") if p.is_file()
                       and p.relative_to(OUT).as_posix() not in wanted and p.relative_to(OUT).as_posix() not in keep) if OUT.is_dir() else []
        print(json.dumps({"changed": changed, "stray": stray}, indent=2))
        return 1 if changed or stray else 0
    for path in [p for p in OUT.rglob("*") if p.is_file()] if OUT.is_dir() else []:
        name = path.relative_to(OUT).as_posix()
        if name not in wanted and name not in keep:
            path.unlink()
    for name, data in wanted.items():
        target = OUT / name
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.is_file() or target.read_bytes() != data:
            target.write_bytes(data)
    for folder in sorted((p for p in OUT.rglob("*") if p.is_dir()), key=lambda p: len(p.parts), reverse=True):
        if not any(folder.iterdir()):
            folder.rmdir()
    if records:
        prov_path = ROOT / "provenance.json"
        prov = json.loads(prov_path.read_text(encoding="utf-8"))
        prov["decks"]["published"] = dict(sorted(records.items()))
        prov_path.write_text(json.dumps(prov, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"built {len(files)} page and static files" + (f" and {len(decks)} decks" if decks else f"; kept {len(keep)} published decks"))
    return 0


if __name__ == "__main__":
    sys.exit(main())
