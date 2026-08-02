# Sky Track — Codebase Map

This directory is a **map of the entire Sky Track codebase**. Like a real-world map, every file is a place, every import and dependency is a road, and every data flow is a route. Read it in order and you will understand the whole project — even with zero prior knowledge.

## How to read this map

1. Start with **[00-overview.md](00-overview.md)** — what the app is and what stack it uses.
2. Follow with **[01-architecture.md](01-architecture.md)** — how the pieces fit together.
3. Then follow a single request through **[02-request-lifecycle.md](02-request-lifecycle.md)**.
4. Learn the storage layer in **[03-database-schema.md](03-database-schema.md)**.
5. Trace the moving data in **[04-data-flows.md](04-data-flows.md)**.
6. Open the detailed per-file maps: **[05-backend-map.md](05-backend-map.md)** and **[06-frontend-map.md](06-frontend-map.md)**.
7. Understand build and deployment in **[07-infrastructure.md](07-infrastructure.md)**.
8. Use **[08-file-index.md](08-file-index.md)** as the master index — every single file is listed there.

## Legend

- **File map entry** — explains one file: purpose, key pieces, and its connections.
- **Road** — a relationship between files: `imports`, `reads from`, `writes to`, `calls`, `renders`, `uses`.
- **Route** — a sequence of roads showing how data travels across the system.
- **Team docs** — deep study material per team lives in [`teams/`](teams/).

## Map sections

| File | What it covers |
|------|----------------|
| [00-overview.md](00-overview.md) | The app, the stack, the two Maven modules |
| [01-architecture.md](01-architecture.md) | Layered architecture, component diagram, dependencies |
| [02-request-lifecycle.md](02-request-lifecycle.md) | One HTTP request from browser to database and back |
| [03-database-schema.md](03-database-schema.md) | All 9 Flyway migrations, entities, relationships |
| [04-data-flows.md](04-data-flows.md) | Snapshot ingestion, delta, alerts, corridor, city switching |
| [05-backend-map.md](05-backend-map.md) | Every Java file in `sky-track-core` and `sky-track-web` |
| [06-frontend-map.md](06-frontend-map.md) | Every template, fragment, CSS and JS asset |
| [07-infrastructure.md](07-infrastructure.md) | Maven, Dockerfile, docker-compose, configuration |
| [08-file-index.md](08-file-index.md) | Master alphabetical index of every file |

## Team deep-dives

Each team directory contains full study material: explanations, real code snippets, data flows, and tough questions with answers.

| Team | Scope |
|------|-------|
| [backend/](teams/backend/) | Entities, repositories, services, security, controllers, REST API |
| [frontend/](teams/frontend/) | Thymeleaf templates, fragments, CSS, JavaScript |
| [design/](teams/design/) | DESIGN.md system, themes, glassmorphism, typography |
| [testers/](teams/testers/) | Unit tests, mocking, how to run and write tests |
| [documentation/](teams/documentation/) | How this documentation is structured and maintained |
