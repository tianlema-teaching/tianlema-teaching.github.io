# Teaching site

Source for the public course sites of the Oakland University courses taught by Tianle Ma: course overviews, lecture slides and interactive algorithm visualizations. Served by GitHub Pages from `docs/`.

This is the instructor's own site, not an official Oakland University site. Enrolled students get the syllabus, schedule, assignments, grades and announcements on Moodle, which is authoritative.

## Layout

| Path | What it is |
|---|---|
| `src/**/*.md` | Page sources, for example `src/csi3620/2026-fall/index.md`. `index.md` becomes the folder's page; `name.md` becomes `name/`. |
| `slides/<course>/<term>/decks/*.md` | Lecture decks in the slides-engine Markdown format. |
| `slides/<course>/<term>/java/lNN/` | Tested Java for lecture NN. `Checks.java` exercises every class and prints `lNN OK`. Every `java` block in a deck must be a verbatim excerpt of these files. |
| `static/` | Files copied unchanged: KaTeX 0.16.11 (MIT, self-hosted), the algorithm visualizations, the stylesheet and the icon. |
| `provenance.json` | Where every published visualization, KaTeX file and deck comes from, with SHA-256 digests. |
| `docs/` | The built site. Generated; do not edit by hand. |
| `tools/`, `tests/` | Build and check tools. |

Each term has its own folder (`2026-fall`), so past terms stay online at permanent addresses. `/csi3620/` and `/csi4130-5130/` point to the current term (`CURRENT` in `tools/build.py`).

## Build and check

Setup once: `python3 -m venv .venv && .venv/bin/pip install -r requirements.txt`. The deck build needs Node and the slides engine (default `~/tools/teaching/slides`, or set `SLIDES_ENGINE`). The Java checks need a JDK.

1. `.venv/bin/python tools/build.py`, or `tools/build.py --slides` after editing a deck.
2. `.venv/bin/python -m unittest discover -s tests` runs these checks:
   - the build is current and every deck matches its source;
   - no logistics, contact details or local paths appear;
   - nothing loads from another host;
   - every same-site link resolves;
   - the visualization and KaTeX files match their recorded digests;
   - the Java in every deck is quoted verbatim, and every package compiles with `-Xlint:all -Werror` and passes its `Checks`.
3. `.venv/bin/python tools/browser_check.py` loads every page and deck at a laptop and a phone size.
4. `.venv/bin/python tools/check_slides.py --root docs docs/csi3620/2026-fall/slides/*.html` renders every slide at projector and phone size.

On a Mac left idle, headless Chrome can stall; run the browser checks under `caffeinate -dimsu`.

## Status and limits

- The lecture decks are drafts. Every deck the instructor did not write had a separate AI review, and the findings were fixed. None has had an independent human review. L04 and L05 are the instructor's in-class decks, published unchanged except for the KaTeX path; they have small recorded overflows at projector size.
- Pages are marked `noindex`, and `robots.txt` disallows crawling, while the material is in draft.
- A public license has not been chosen yet. Until one is, all rights are reserved.
