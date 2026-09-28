"""Regression checks for the teaching site: privacy, links, provenance, decks and the Java they quote.

Run: .venv/bin/python -m unittest discover -s tests   (the Java checks need javac/java)
"""
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import urlsplit, unquote
import hashlib
import json
import re
import shutil
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
DOCS, SRC, SLIDES = ROOT / "docs", ROOT / "src", ROOT / "slides"
sys.path.insert(0, str(ROOT / "tools"))
import build  # noqa: E402

PROVENANCE = json.loads((ROOT / "provenance.json").read_text(encoding="utf-8"))
DECKS = sorted(SLIDES.glob("*/*/decks/*.md"))
PAGES = sorted(p for p in DOCS.rglob("*.html") if "slides" not in p.relative_to(DOCS).parts)

# Course logistics stay on Moodle: staff contact details, meeting links, rooms, enrollment codes.
PATTERNS = {
    "email address": re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}"),
    "meeting link": re.compile(r"(zoom\.us|teams\.microsoft\.com|meet\.google\.com)", re.I),
    "meeting passcode": re.compile(r"\bpass(code|word)\s*[:=]", re.I),
    "classroom": re.compile(r"\b(MSC|SFH|EC|DH|HHB|EH|OC)\s?\d{3}\b"),
    "enrollment code": re.compile(r"\b[A-Z]{3,}CSI\d{4}[A-Za-z0-9]*\b"),
    "local path": re.compile(r"(/Users/|/private/tmp|~/workspace|~/projects)"),
}
# Words that mark course logistics; they may appear on the course pages (which explain that such
# things live on Moodle) but never inside a lecture deck.
LOGISTICS = re.compile(r"\b(moodle|zybooks?|office hours?|midterm|final exam|homework|syllabus|due date)\b", re.I)
# Anything that would make a page load from another host or call out.
EXTERNAL = re.compile(r"""(?:src|action)\s*=\s*["']?\s*(?:https?:)?//|<link[^>]+href\s*=\s*["']?(?:https?:)?//|url\(\s*["']?(?:https?:)?//|@import|fetch\(|XMLHttpRequest|WebSocket|sendBeacon|import\(""", re.I)
# Optional loaders inside the slides engine that a deck with a local @katex and no video never calls.
ENGINE_URLS = ("https://cdn.jsdelivr.net/npm/katex", "https://player.vimeo.com/video/", "https://www.youtube-nocookie.com/embed/")
URL = re.compile(r"https?://[^\s\"'`)<>\\]+")
# Hosts a page may link to (plain links the reader follows, never loaded automatically).
LINK_HOSTS = {"learn.tianlema.com"}


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def findings(text: str) -> list[str]:
    return [name for name, pattern in PATTERNS.items() if pattern.search(text)]


class Links(HTMLParser):
    def __init__(self):
        super().__init__(); self.links = []; self.h1 = 0; self.ids = set()

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if tag == "h1": self.h1 += 1
        if a.get("id"): self.ids.add(a["id"])
        for key in ("href", "src"):
            if a.get(key): self.links.append(a[key])


def resolve(page: Path, href: str) -> Path | None:
    """The docs/ file a same-site link points to, or None for another host."""
    parts = urlsplit(href)
    if parts.scheme or parts.netloc: return None
    path = unquote(parts.path)
    if not path: return page
    target = (DOCS / path.lstrip("/")) if path.startswith("/") else (page.parent / path)
    target = Path(target.as_posix())
    return target / "index.html" if path.endswith("/") or target.is_dir() else target


class PrivacyTests(unittest.TestCase):
    def test_patterns_catch_examples(self):
        for sample in ["a.b@oakland.edu", "https://oakland.zoom.us/j/1", "Passcode: 1", "EC 550", "ABCCSI3620X", "/Users/x/y"]:
            self.assertTrue(findings(sample), sample)
        self.assertEqual(findings("CSI 3620 covers linked lists."), [])

    def test_repository_text_is_clean(self):
        skip = {".git", ".venv", "__pycache__"}
        files = [p for p in ROOT.rglob("*") if p.is_file() and not skip & set(p.relative_to(ROOT).parts)
                 and p.suffix not in {".woff2", ".pyc", ".ico"} and p.name != "test_site.py"]
        problems = [f"{p.relative_to(ROOT)}: {findings(p.read_text(encoding='utf-8'))}" for p in files
                    if findings(p.read_text(encoding="utf-8"))]
        self.assertEqual(problems, [])

    def test_pages_make_no_external_requests(self):
        katex = PROVENANCE["katex"]["files"]
        for path in sorted(p for p in DOCS.rglob("*") if p.is_file() and p.suffix in {".html", ".js", ".css"}):
            name = path.relative_to(DOCS).as_posix()
            if katex.get(name, {}).get("published_sha256") == sha(path.read_bytes()): continue  # official KaTeX release
            if "slides" in path.relative_to(DOCS).parts: continue  # decks: test_built_decks_contain_no_unexpected_urls
            with self.subTest(file=name):
                self.assertIsNone(EXTERNAL.search(path.read_text(encoding="utf-8")))

    def test_every_template_page_is_noindex(self):
        for page in PAGES:
            if "visualizations" in page.parts: continue
            with self.subTest(page=page.relative_to(DOCS).as_posix()):
                self.assertIn('<meta name="robots" content="noindex, nofollow">', page.read_text(encoding="utf-8"))
        self.assertEqual((DOCS / "robots.txt").read_text(), "User-agent: *\nDisallow: /\n")


class SiteTests(unittest.TestCase):
    def test_build_output_is_current(self):
        expected = build.expected_files()
        for name, data in expected.items():
            with self.subTest(file=name):
                self.assertTrue((DOCS / name).is_file(), "missing: run tools/build.py")
                self.assertEqual((DOCS / name).read_bytes(), data, "stale: run tools/build.py")
        decks = {p.relative_to(DOCS).as_posix() for p in DOCS.glob("*/*/slides/*.html")}
        stray = {p.relative_to(DOCS).as_posix() for p in DOCS.rglob("*") if p.is_file()} - set(expected) - decks
        self.assertEqual(stray, set())

    def test_same_site_links_resolve(self):
        for page in PAGES:
            parser = Links(); parser.feed(page.read_text(encoding="utf-8"))
            for href in parser.links:
                if href.startswith(("mailto:", "data:", "javascript:")): continue
                target = resolve(page, href)
                with self.subTest(page=page.relative_to(DOCS).as_posix(), href=href):
                    if target is None:
                        self.assertIn(urlsplit(href).hostname, LINK_HOSTS)
                    else:
                        self.assertTrue(target.is_file(), target)

    def test_template_pages_have_one_h1(self):
        for page in PAGES:
            if "visualizations" in page.parts: continue
            parser = Links(); parser.feed(page.read_text(encoding="utf-8"))
            with self.subTest(page=page.relative_to(DOCS).as_posix()):
                self.assertEqual(parser.h1, 1)

    def test_lectures_page_links_every_deck_and_its_study_guide(self):
        for deck in DECKS:
            course, term = deck.parents[2].name, deck.parents[1].name
            text = (SRC / course / term / "lectures.md").read_text(encoding="utf-8")
            with self.subTest(deck=deck.name):
                self.assertIn(f"/{course}/{term}/slides/{deck.stem}.html)", text)
                self.assertIn(f"/{course}/{term}/slides/{deck.stem}.html?mode=scroll)", text)

    def test_static_files_match_provenance(self):
        records = {**PROVENANCE["visualizations"]["files"], **PROVENANCE["katex"]["files"]}
        published = {p.relative_to(DOCS).as_posix() for d in ("visualizations", "katex") for p in (DOCS / d).rglob("*") if p.is_file()}
        self.assertEqual(published, set(records))
        for name, record in records.items():
            with self.subTest(file=name):
                self.assertEqual(sha((DOCS / name).read_bytes()), record["published_sha256"])


class DeckTests(unittest.TestCase):
    def test_every_deck_is_published_from_its_current_source(self):
        published = PROVENANCE["decks"]["published"]
        expected = {f"{d.parents[2].name}/{d.parents[1].name}/slides/{d.stem}.html" for d in DECKS}
        self.assertEqual(set(published), expected)
        self.assertEqual({p.relative_to(DOCS).as_posix() for p in DOCS.glob("*/*/slides/*.html")}, expected)
        for name, record in published.items():
            with self.subTest(deck=name):
                self.assertEqual(record["deck_sha256"], sha((ROOT / record["built_from"]).read_bytes()), "stale: run tools/build.py --slides")
                self.assertEqual(record["published_sha256"], sha((DOCS / name).read_bytes()))

    def test_decks_use_local_katex_and_no_remote_media(self):
        for deck in DECKS:
            text = deck.read_text(encoding="utf-8")
            with self.subTest(deck=deck.name):
                self.assertIn("\n@katex ../../../katex/\n", "\n" + text)
                self.assertIsNone(re.search(r"!\[[^\]]*\]\(\s*(https?:)?//", text), "remote media")
                self.assertIsNone(re.search(r"^@(audio|video|vtt|poster)\s+(https?:)?//", text, re.M), "remote media")

    def test_built_decks_contain_no_unexpected_urls(self):
        for deck in DECKS:
            html = (DOCS / deck.parents[2].name / deck.parents[1].name / "slides" / (deck.stem + ".html")).read_text(encoding="utf-8")
            source = set(URL.findall(deck.read_text(encoding="utf-8")))
            extra = {u for u in URL.findall(html) if u not in source and not u.startswith(ENGINE_URLS)
                     and not u.startswith(("http://www.w3.org/", "https://www.w3.org/"))}
            with self.subTest(deck=deck.name):
                self.assertEqual(extra, set())

    def test_decks_carry_no_course_logistics(self):
        for deck in DECKS:
            text = deck.read_text(encoding="utf-8")
            with self.subTest(deck=deck.name):
                self.assertEqual(findings(text), [])
                self.assertEqual(LOGISTICS.findall(text), [])

    def test_deck_links_resolve(self):
        for deck in DECKS:
            page = DOCS / deck.parents[2].name / deck.parents[1].name / "slides" / (deck.stem + ".html")
            for href in re.findall(r"\]\(([^)\s]+)\)", deck.read_text(encoding="utf-8")):
                if href.startswith("#"): continue
                target = resolve(page, href)
                with self.subTest(deck=deck.name, href=href):
                    if target is None:  # a link the reader may follow; decks never load from other hosts
                        self.assertEqual(urlsplit(href).scheme, "https")
                    else:
                        self.assertTrue(target.is_file(), target)


class JavaTests(unittest.TestCase):
    @staticmethod
    def unmatched(block: str, lines: set[str]) -> list[str]:
        return [l.strip() for l in block.splitlines() if l.strip() and l.strip() != "// ..." and l.strip() not in lines]

    def test_java_blocks_are_excerpts_of_tested_code(self):
        for deck in DECKS:
            blocks = re.findall(r"^```java[^\n]*\n(.*?)^```", deck.read_text(encoding="utf-8"), re.M | re.S)
            if not blocks: continue
            package = deck.parents[1] / "java" / ("l" + deck.name[1:3])
            with self.subTest(deck=deck.name):
                self.assertTrue(package.is_dir(), f"{deck.name} has Java blocks but no {package.relative_to(ROOT)}")
                lines = {l.strip() for f in package.glob("*.java") for l in f.read_text(encoding="utf-8").splitlines()}
                for block in blocks:
                    self.assertLessEqual(len(block.splitlines()), 15, "code block longer than 15 lines")
                    self.assertEqual(self.unmatched(block, lines), [])

    def test_excerpt_check_catches_edits(self):
        source = {"int x = 1;", "return x;"}
        self.assertEqual(self.unmatched("int x = 1;\n// ...\nreturn x;\n", source), [])
        self.assertEqual(self.unmatched("int x = 2;\nreturn x;\n", source), ["int x = 2;"])

    def test_java_packages_compile_and_pass_their_checks(self):
        if not shutil.which("javac") or not shutil.which("java"):
            self.skipTest("javac/java not installed: Java checks NOT run")
        packages = sorted(p for p in SLIDES.glob("*/*/java/*") if p.is_dir())
        self.assertTrue(packages)
        with tempfile.TemporaryDirectory() as out:
            sources = [str(f) for p in packages for f in sorted(p.glob("*.java"))]
            run = subprocess.run(["javac", "-Xlint:all", "-Werror", "-d", out, *sources], capture_output=True, text=True, timeout=180)
            self.assertEqual(run.returncode, 0, run.stdout + run.stderr)
            for package in packages:
                with self.subTest(package=package.name):
                    run = subprocess.run(["java", "-cp", out, f"{package.name}.Checks"], capture_output=True, text=True, timeout=60)
                    self.assertEqual(run.returncode, 0, run.stdout + run.stderr)
                    self.assertEqual(run.stdout.strip().splitlines()[-1], f"{package.name} OK")


if __name__ == "__main__":
    unittest.main()
