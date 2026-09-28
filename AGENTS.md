# AGENTS.md - Teaching site

Public course material for the instructor's Oakland University courses, published by GitHub Pages from `docs/` in the public repository tianlema-teaching/tianlema-teaching.github.io.

- Read `README.md` first. Edit `src/`, `slides/` and `static/`, never `docs/`; rebuild with `tools/build.py` (add `--slides` after deck edits).
- This repository is public. Never add student information, grades, assignments, solutions, quiz keys, meeting links, rooms, staff contact details, credentials or local file paths. Moodle is authoritative for course logistics.
- Every Java block in a deck must stay a verbatim excerpt of tested code in `slides/<course>/<term>/java/`. Traces on slides come from running that code.
- Before committing, run the tests, `tools/browser_check.py` and, for deck changes, `tools/check_slides.py`. Do not loosen a test to make it pass.
- No push, Pages setting change, DNS change or license decision without the owner's explicit approval. Keep drafts labeled as drafts; do not claim human review that has not happened.
- The instructor's decks (L04, L05) are published unchanged except for the KaTeX path; do not edit them without the owner.
