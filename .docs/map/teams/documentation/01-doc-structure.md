# 01 — Documentation Structure

The documentation is organized like a real-world map: an overview of the whole territory, detailed maps of each area, and per-team study guides.

## The `.docs/` tree

```
.docs/
└── map/                      ← the "map" — how the whole codebase fits together
    ├── README.md             ← map home: how to read, legend, reading order
    ├── 00-overview.md        ← what Sky Track is, the stack, the two modules
    ├── 01-architecture.md    ← layered architecture + component diagram
    ├── 02-request-lifecycle.md ← one HTTP request end to end
    ├── 03-database-schema.md ← all 9 migrations + ER diagram
    ├── 04-data-flows.md      ← snapshot, delta, alerts, corridor, switching
    ├── 05-backend-map.md     ← every Java file, with its connections
    ├── 06-frontend-map.md    ← every template, CSS, and JS file
    ├── 07-infrastructure.md  ← Maven, Docker, compose, config
    ├── 08-file-index.md      ← master checklist of every file
    └── teams/                ← per-team study material
        ├── backend/
        ├── frontend/
        ├── design/
        ├── testers/
        └── documentation/
```

## The two layers

### 1. The map (`map/00–08`)

The map is the **whole-project overview**. It explains every file and the connections between them. A beginner reads it top to bottom and understands the entire system.

Key properties:
- **Every file has a home.** The file index (`08-file-index.md`) lists every file in the repo, so nothing is undocumented.
- **Files link to each other.** Each entry names what it `imports`, `reads`, `writes`, `calls`, or `renders` — the "roads" of the map.
- **Diagrams included.** Mermaid ER/sequence/component diagrams visualize the relationships.

### 2. The teams (`map/teams/*`)

Each team gets a folder with deep study material. The format is consistent across teams:

```
teams/<team>/
├── README.md        ← team role + how they connect to other teams + index
├── 01-*.md          ← section 1: explanation + code + data flow + Q&A
├── 02-*.md
├── ...
└── NN-faq.md        ← tough cross-cutting questions with answers
```

## The consistent section format

Every section follows the same four parts (set during planning):

1. **What it is / why it exists** (Explanation)
2. **Real code snippet** from the repo (Reference)
3. **How data flows through it** (Explanation)
4. **Test Your Understanding** — 3–6 tough Q&A with answers

This uniform format makes the docs predictable: a student knows what to expect in every section of every team guide.

## Reading orders

**New to the project (any role):**
`map/00-overview` → `01-architecture` → `02-request-lifecycle` → `03-database-schema` → `04-data-flows` → then their team's folder.

**Studying for your team's role:** start with your team's `README.md`, read each numbered section, then drill the FAQ.

**Looking up one file:** open `08-file-index.md`, find the file, follow the link to its detailed entry.

## Why this structure works

- **Beginner-friendly:** the map gives the big picture before any code is read.
- **Complete:** the file index guarantees coverage of every file.
- **Role-focused:** each team studies exactly what they own, plus how it interconnects.
- **Consistent:** one format everywhere makes the docs easy to skim and to maintain.

## Test your understanding

**Q1. What is the difference between the map files (`00–08`) and the team folders?**
**A.** The map files describe the whole project — every file and its connections, readable by anyone. Team folders are deep dives for each role (backend, frontend, design, testers, documentation), with code snippets and Q&A per section.

**Q2. How does the file index guarantee completeness?**
**A.** `08-file-index.md` lists every file in the repo in one place. If a file is not there (or not explained in the map), the documentation is known to be incomplete.

**Q3. Where would a tester start studying?**
**A.** Their team's `teams/testers/README.md` (role + test inventory), then the numbered sections and the manual test guide.

**Q4. What four parts does every section contain?**
**A.** Explanation, real code snippet, data-flow walkthrough, and tough Q&A with answers.

**Q5. Why keep diagrams as Mermaid?**
**A.** Mermaid is text-based and renders on GitHub/GitLab, so diagrams live with the docs, stay versionable, and can be updated in the same review as the code they describe.
