# Documentation Team — Study Guide

This is the complete study material for the documentation team. It explains the Sky Track documentation system: the codebase map, the per-team guides, the project rules in `AGENTS.md`, the design and system documents, and how to keep everything in sync.

## Your role

The documentation team owns the **project's written knowledge**: the `.docs/` documentation, the project guides (`AGENTS.md`, `DESIGN.md`, `SYSTEM-DESIGN.md`, `README.md`), and the progress log. Your job is to make sure every team can learn how the project works and that docs never drift from the code.

## What you own

| Area | Files | Read more |
|------|-------|-----------|
| Documentation structure | `.docs/`, `.docs/map/` | [01-doc-structure](01-doc-structure.md) |
| Project rules | `AGENTS.md` + `.agents/plans/PROGRESS.md` | [02-agents-and-rules](02-agents-and-rules.md) |
| Design + system docs | `DESIGN.md`, `SYSTEM-DESIGN.md`, `README.md` | [03-design-and-system-docs](03-design-and-system-docs.md) |
| Maintenance workflow | How to keep docs current | [04-keeping-docs-in-sync](04-keeping-docs-in-sync.md) |
| Tough questions | Interview-style quiz | [05-faq](05-faq.md) |

## How documentation connects to the other teams

- **Backend team** — you document their entities, services, security, and REST API; they tell you when the data model changes.
- **Frontend team** — you document their templates and JavaScript; they tell you when pages/behavior change.
- **Design team** — you record their `DESIGN.md` as the source of truth for visuals.
- **Testers team** — you document how to run tests and the manual test flows they own.

## The documentation hierarchy

```
Sky Track
├── AGENTS.md          ← rules for anyone (incl. agents) changing the code
├── README.md          ← quick start + project layout for newcomers
├── DESIGN.md          ← the design system (how it looks) — source of truth for frontend
├── SYSTEM-DESIGN.md   ← what we build (features, schema, endpoints)
└── .docs/             ← this documentation
    └── map/           ← the codebase map (every file)
        ├── 00–08      ← overview, architecture, request lifecycle, schema, data flows, file maps, infra, index
        └── teams/     ← per-team study guides
            ├── backend/
            ├── frontend/
            ├── design/
            ├── testers/
            └── documentation/   ← this guide
```

## Writing standards to follow

- App name is **Sky Track** — never "Sky Watch" / "Skywatch" (except legacy identifiers).
- No emojis, no comments in code, generic well-structured writing.
- Every doc uses the same section format: **explanation → real code snippet → data flow → Q&A**.
- Link, don't duplicate — reference the map and other team docs instead of copying content.

---

Begin with [01-doc-structure.md](01-doc-structure.md).
