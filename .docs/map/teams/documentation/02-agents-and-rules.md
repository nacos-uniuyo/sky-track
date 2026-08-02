# 02 — Project Rules and AGENTS.md

`AGENTS.md` is the rulebook for everyone changing the Sky Track codebase — humans and agents alike. This page explains each rule and why it exists.

## AGENTS.md at a glance

`AGENTS.md` (repo root) covers:

1. **Commands** — how to build, run, and test.
2. **Local setup** — the environment variables and PostgreSQL requirement.
3. **Gotchas** — the traps that bite developers (Flyway, lazy loading, security, scoping).
4. **Layout** — where the code lives.
5. **Agent Rules** — progress logging, app name, code standards, design system, system design.

## The Agent Rules in detail

### Progress logging

> After every file change, feature addition, or bug fix, append an entry to `.agents/plans/PROGRESS.md` with the exact date and time. Newest entries go at the top.

```markdown
## 2026-08-02
- **10:45** — Description of what was done. Outcome.
```

- **Why:** a full history of the project's changes — useful for review, learning, and audits.
- **Format:** date heading, bullet entries with `HH:MM`, newest first.

### App name — SKY TRACK

> The application is named **SKY TRACK**. Never call it "Sky Watch", "Skywatch", or any other variation. Use "Sky Track" consistently in code, templates, strings, docs, and UI copy. The Java package (`com.skywatch.weather`), Maven artifact, and DB user are legacy and stay as-is.

- **Why:** the brand is Sky Track; older identifiers are legacy and must not be renamed (they are wired into Maven, DB, and code).

### Code standards (school project)

> - **No comments** — no `//`, `/* */`, or `/** */` Javadoc anywhere in the codebase.
> - **No emojis** — not in code, templates, strings, or assets.
> - **Generic, well-structured code** — plain and functional. No unnecessary abstractions or flair.

- **Why:** a consistent, clean, conventional codebase that is easy to read and grade. Documentation lives in `.docs/`, not in code comments.

### Design system

> `DESIGN.md` is the single source of truth for ALL frontend work. Every template, stylesheet, and theme must follow its glassmorphism spec, palettes, typography, and component patterns. Do not introduce off-spec colors, fonts, or layouts.

- **Why:** one design language everywhere; the design team defines it once in `DESIGN.md`.

### System design

> `SYSTEM-DESIGN.md` defines what we are building. Every feature, schema change, and component must conform to it. Read it before starting backend or frontend work.

- **Why:** the "what and why" of the project — features, schema, dashboard zones, and API — stays consistent.

## The Gotchas (things to never forget)

- **Flyway / `ddl-auto: validate`** — schema only changes via new migrations; never edit an applied one.
- **`open-in-view: false`** — no lazy loading outside transactions; use ownership queries to avoid `LazyInitializationException`.
- **Global security** — only `/signup`, `/login`, `/css/**`, `/js/**` are public; login uses `email`.
- **Per-user scoping** — tracked cities and alerts are scoped by the current user.
- **`created_at`** is DB-managed (`insertable=false, updatable=false`).
- **OpenWeatherMap** — One Call 3.0 needs a paid subscription; use the free `/forecast` endpoint.

## The progress log convention

`.agents/plans/PROGRESS.md` is the running changelog. Conventions:

- Newest entry at the top under a `## YYYY-MM-DD` heading.
- Each entry: `- **HH:MM** — What was done. Outcome.`
- Logged for **every** change, not just big features.

This is the first place to look when answering "what changed and when?"

## Test your understanding

**Q1. What three things does the "code standards" rule prohibit?**
**A.** Comments (all kinds), emojis, and unnecessary abstractions/flair. Code should be plain, well-structured, and conventional.

**Q2. Why must applied Flyway migrations never be edited?**
**A.** Applied migrations are checksummed. Editing one breaks the migration history on any environment that already ran it. Changes must be new `V<n>` files.

**Q3. What is the correct way to check alert ownership given `open-in-view: false`?**
**A.** Use the repository ownership query `existsByIdAndTrackedCityUserId(id, userId)` — it runs in SQL and avoids touching lazy proxies that would throw `LazyInitializationException`.

**Q4. What is the difference between `DESIGN.md` and `SYSTEM-DESIGN.md`?**
**A.** `DESIGN.md` is the visual system (how it looks — themes, glass, typography). `SYSTEM-DESIGN.md` is the functional design (what we build — features, schema, endpoints). Both are non-negotiable sources of truth.

**Q5. Where does the project keep its change history?**
**A.** In `.agents/plans/PROGRESS.md` — dated, time-stamped entries, newest first, updated after every change.
