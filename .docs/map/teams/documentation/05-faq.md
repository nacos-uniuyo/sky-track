# 05 — Documentation Tough Questions (FAQ)

Cross-cutting questions spanning the documentation material. Try to answer each before reading the answer.

## Q1. What is the documentation team responsible for?

**A.** The project's written knowledge: the `.docs/` map + team guides, `AGENTS.md`, `DESIGN.md`, `SYSTEM-DESIGN.md`, `README.md`, and the progress log — and keeping them in sync with the code.

## Q2. Why is the documentation structured as a "map" plus "teams"?

**A.** A map gives anyone a complete, connected overview of the whole codebase (every file, every relationship). Team guides then deep-dive each role. This serves both "understand the whole project" and "master my team's area."

## Q3. What is the difference between `AGENTS.md`, `DESIGN.md`, and `SYSTEM-DESIGN.md`?

**A.** `AGENTS.md` = rules for changing the code (commands, gotchas, standards). `DESIGN.md` = how it looks (themes, glass, typography). `SYSTEM-DESIGN.md` = what it does (features, schema, endpoints). All three are sources of truth for their scope.

## Q4. Why does the app use "SKY TRACK" but keep the `com.skywatch.weather` package?

**A.** The brand is Sky Track; the package name is a legacy identifier wired into Maven, the DB, and the codebase. Renaming it would be a large, risky change for no functional benefit — so the rule is: use "Sky Track" everywhere visible, leave legacy identifiers as-is.

## Q5. How would a new backend team member use this documentation to learn the codebase?

**A.** Read `map/00`–`04` for the big picture, then `teams/backend/README.md` and sections `01`–`08`, finishing with the FAQ. Follow file links in `05-backend-map.md` and `08-file-index.md` to jump into the actual code.

## Q6. What makes a section consistent across all team guides?

**A.** The four-part format: explanation, real code snippet, data-flow walkthrough, and "Test Your Understanding" Q&A. Consistent structure makes the whole doc set predictable and skimmable.

## Q7. How do you know the documentation is complete?

**A.** Run the completeness checks: `08-file-index.md` lists every file; the test inventory matches `mvn test`; the schema docs match the migrations; the endpoint tables match the controllers; and no file in the repo is unlinked.

## Q8. Why are diagrams written in Mermaid rather than images?

**A.** Mermaid is text-based Markdown, so diagrams version with the code, render on GitHub without extra tooling, and are easy to update in a normal diff/review.

## Q9. What should you do when the schema changes?

**A.** A new Flyway migration is added (never edit applied ones). Then update: `03-database-schema.md`, `08-file-index.md`, the backend entities/repositories guides, and `PROGRESS.md`. Docs must match the applied migrations.

## Q10. Why is there a progress log and what format does it use?

**A.** It is the running change history (`PROGRESS.md`), newest first: `## YYYY-MM-DD` then `- **HH:MM** — Description. Outcome.`. It answers "what changed and when" and satisfies the AGENTS.md progress-logging rule.

## Q11. What is the relationship between `DESIGN.md` and `tokens.css`?

**A.** `DESIGN.md` is the source of truth; `tokens.css` is its exact CSS transcription (the `:root` + `.theme-*` token blocks). Frontend work must use the tokens, so the two must always agree.

## Q12. Where would you document a brand-new REST endpoint?

**A.** In `05-backend-map.md` (the controller), `04-data-flows.md` (the flow), the backend `06-controllers-and-rest.md` endpoint table, and `08-file-index.md` if a new file exists. Testers `05-coverage-map.md` records it as manually tested until automated.

## Q13. Why should documentation "link, don't duplicate"?

**A.** Duplicated facts drift: one copy gets updated and another doesn't, and students get contradictory answers. Linking to a single home keeps every doc accurate.

## Q14. What does `08-file-index.md` do for a complete beginner?

**A.** It acts as the master checklist and lookup table: every file in the repo with a one-line purpose and a link to its detailed entry, so nothing is skipped and anything can be looked up quickly.

## Q15. What is the recommended order to keep docs in sync after a code change?

**A.** 1) Update `PROGRESS.md`; 2) update the map (file index + relevant `0X` section); 3) update the affected team guide; 4) re-check links; 5) never introduce duplicates.
