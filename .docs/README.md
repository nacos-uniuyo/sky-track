# Sky Track — Project Documentation

Welcome to the Sky Track study documentation. This is the single place to learn how the entire project works — for every role on the team.

## Start here

1. Read the **[Codebase Map](map/README.md)** — the whole project explained as a connected map. Start with `00-overview.md` and follow the reading order.
2. Then open the guide for **your team** below.

## Team guides

Each guide explains that team's slice of the project from scratch, with real code snippets, data flows, and tough Q&A per section.

| Team | Guide |
|------|-------|
| Backend | [teams/backend/](map/teams/backend/) — entities, repositories, services, security, controllers, REST |
| Frontend | [teams/frontend/](map/teams/frontend/) — Thymeleaf, templates, CSS, JavaScript |
| Design | [teams/design/](map/teams/design/) — DESIGN.md, themes, glassmorphism, typography |
| Testers | [teams/testers/](map/teams/testers/) — unit tests, mocking, manual test flows |
| Documentation | [teams/documentation/](map/teams/documentation/) — this documentation system and how to maintain it |

## Source-of-truth documents (repo root)

| Document | What it defines |
|----------|-----------------|
| [`DESIGN.md`](../DESIGN.md) | The visual design system (how it looks) |
| [`SYSTEM-DESIGN.md`](../SYSTEM-DESIGN.md) | The system/feature design (what we build) |
| [`AGENTS.md`](../AGENTS.md) | Rules for changing the code (commands, gotchas, standards) |
| [`README.md`](../README.md) | How to run and build the app |

## Map contents

| File | Covers |
|------|--------|
| [00-overview.md](map/00-overview.md) | Project overview + stack + modules |
| [01-architecture.md](map/01-architecture.md) | Architecture + diagrams |
| [02-request-lifecycle.md](map/02-request-lifecycle.md) | One request end to end |
| [03-database-schema.md](map/03-database-schema.md) | Migrations + ER diagram |
| [04-data-flows.md](map/04-data-flows.md) | The data pipelines |
| [05-backend-map.md](map/05-backend-map.md) | Every Java file |
| [06-frontend-map.md](map/06-frontend-map.md) | Every template/CSS/JS file |
| [07-infrastructure.md](map/07-infrastructure.md) | Maven, Docker, config |
| [08-file-index.md](map/08-file-index.md) | Master file checklist |

## Suggested study path

```
1. map/README.md          ← how to read the docs
2. map/00 → map/04        ← understand the whole system
3. your team's README     ← your role + how it interconnects
4. your team's sections   ← deep dive with code + Q&A
5. your team's FAQ        ← test yourself
```
