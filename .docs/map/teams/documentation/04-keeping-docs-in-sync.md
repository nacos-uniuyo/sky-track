# 04 — Keeping the Docs in Sync

Documentation decays if it is not maintained alongside the code. This page defines the documentation team's workflow to keep everything current.

## The golden rule

> **Link, don't duplicate.** The map and team guides reference each other and the code; never copy the same explanation into several files, or it will drift.

## When docs must be updated

Update documentation when **any** of these change:

| Change | Docs to update |
|--------|----------------|
| New entity / migration | `03-database-schema.md`, `08-file-index.md`, backend `01-entities`, `02-repositories` |
| New service / method | `05-backend-map.md`, backend `03-services` |
| New controller / endpoint | `05-backend-map.md`, `06-controllers-and-rest.md`, `04-data-flows.md` |
| New template / JS / CSS | `06-frontend-map.md`, frontend `02-templates-map`, `03-javascript`, `04-css` |
| Design change | `DESIGN.md`, `tokens.css`, design team docs |
| Test change | testers `02/03/05`, the test inventory in testers `README` |
| Build / deploy change | `07-infrastructure.md`, `README.md` |
| New file added anywhere | `08-file-index.md` (the completeness checklist) |

## The maintenance workflow

1. **Code changes land first.** Docs describe reality; never write docs for code that does not exist.
2. **Update `PROGRESS.md`** with the change (per AGENTS.md rule).
3. **Update the map** — the file index (if new files), the relevant `0X-*.md` section.
4. **Update the affected team guide** section.
5. **Cross-check links** — ensure references still resolve (relative paths, anchors).
6. **No duplicates** — if a fact appears in two places, link to the single source instead.

## Consistency checks to run

- **File coverage:** does `08-file-index.md` list every file currently in the repo?
- **Names:** is the app called "Sky Track" everywhere (no "Sky Watch"/"Skywatch")?
- **Test counts:** do the docs' test inventories match `mvn test` output?
- **Schema:** do the docs' tables/columns match the latest migrations?
- **Endpoints:** do the documented endpoints exist in the controllers?
- **Format:** does every section follow explanation → snippet → data flow → Q&A?

## Tooling tips

- **Mermaid diagrams** render on GitHub — keep them in the docs so diagrams version with the code.
- **Relative links** (`../map/03-database-schema.md`) work on GitHub; avoid absolute paths.
- **One fact, one home.** E.g. the OpenWeatherMap free-vs-paid note lives in the map's data flows and is linked, not repeated, elsewhere.

## A typical update example

A backend developer adds `GET /api/weather/{id}/latest`:

1. `PROGRESS.md` gets an entry.
2. `05-backend-map.md` + `08-file-index.md` note the new method (no new file).
3. `04-data-flows.md` gains the new flow.
4. backend `06-controllers-and-rest.md` endpoint table gains the row.
5. testers `05-coverage-map.md` notes the new endpoint is manually tested.
6. Links re-checked.

## Test your understanding

**Q1. What is the single most important rule for avoiding doc drift?**
**A.** Link, don't duplicate — keep one home for each fact and reference it elsewhere. Duplication is what causes docs to disagree with each other.

**Q2. Why must code land before docs?**
**A.** Docs describe reality. Writing docs for code that doesn't exist yet creates inaccurate study material and confusion.

**Q3. What does `08-file-index.md` protect against?**
**A.** Files that are never documented. As the completeness checklist, it guarantees every repo file has a documented home.

**Q4. If a test is added, which doc must be updated?**
**A.** The testers `README.md` inventory (test count/coverage) and `05-coverage-map.md`, so the documented numbers stay accurate.

**Q5. How do you verify the docs are currently accurate?**
**A.** Run the consistency checks: compare the file index to the repo, the test inventory to `mvn test`, the schema docs to the migrations, and the endpoint tables to the controllers.
