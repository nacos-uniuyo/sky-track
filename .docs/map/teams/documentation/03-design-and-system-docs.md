# 03 — The Design and System Documents

Three root-level documents define the project before any code: `DESIGN.md`, `SYSTEM-DESIGN.md`, and `README.md`. This page explains each one and how they relate to the `.docs/` map.

## The three documents

| Document | Answers | Audience |
|----------|---------|----------|
| `DESIGN.md` | How does Sky Track **look**? | Design + frontend teams |
| `SYSTEM-DESIGN.md` | What does Sky Track **do**? | All teams |
| `README.md` | How do I **run** it? | Newcomers |

## DESIGN.md — the visual language

The single source of truth for everything visual. Contents:

1. **Overview** — premium, real-time weather tracking with a liquid morphing background and glassmorphism.
2. **Design principles** — the 4 non-negotiables (glass-first, liquid background, theme-driven palettes, consistency).
3. **Theme system** — 4 time-of-day themes with exact palettes:
   - Morning `#FF7E00` / `#FFD700` / `#4A90E2`
   - Afternoon `#1E88E5` / `#FFC107` / `#FFFFFF`
   - Evening `#D35400` / `#8E44AD` / `#2C3E50`
   - Night `#0B0F19` / `#1B263B` / `#E0E6ED`
4. **CSS tokens** — the `:root` + `.theme-*` variable blocks implemented in `tokens.css`.
5. **Typography** — Inter (metrics) + Playfair Display (script greetings) with the type scale.
6. **Glassmorphism recipe** — `.glass`, `.glass-card`, `.glass-input`, `.glass-button`.
7. **Dashboard components** — header, hero, liquid visualizer, hourly, 7-day.
8. **Implementation rules** — theme switching, no hardcoded colors, async fonts, reduced-motion, auth pages, mobile-first.

**Relationship to code:** `tokens.css` is the literal CSS transcription of the DESIGN.md token blocks; `app.css` implements the recipe; templates apply the classes.

## SYSTEM-DESIGN.md — the functional design

Defines what the app does and how data is structured. Contents:

1. **Overview & vision** — an active meteorological tracker, not a passive viewer.
2. **Feature inventory** — standard weather features + differentiators (delta "Shift", travel corridor, anomaly alerts, historical playback).
3. **User journey** — onboarding → tracked hub → deep dive; plus the personalized greeting + contextual city messages.
4. **Database schema** — the target schema evolving from `app_users`/`saved_locations` to tracked cities, snapshots, alerts, corridors.
5. **Dashboard layout** — the three zones (hero, favorites carousel, analytics/trends) + top bar.
6. **Frontend state flow** — the tap-to-switch `activeCityId` flow.
7. **Backend architecture** — module mapping, OpenWeatherMap consumption, snapshot ingestion, delta calculation.
8. **API endpoint map** — every REST and MVC endpoint.
9. **Design compliance** — frontend must follow DESIGN.md.

**Relationship to code:** it is the blueprint the backend implements (entities, repositories, services, controllers, REST API).

## README.md — getting started

The quick-start for anyone picking up the repo: what Sky Track is, the features list, Docker quick start (`docker compose up --build`), local dev setup (install core first), project layout, and configuration.

## How the three documents relate to `.docs/`

- `DESIGN.md` and `SYSTEM-DESIGN.md` are the **"why"** — captured in prose at the root.
- `.docs/map/` is the **"how"** — the code-level map with every file.
- The team guides sit between: each explains a role's slice using both the design/system docs and the code.

```
DESIGN.md ─── "how it looks" ────┐
                                 ├──→ .docs/map (the code reality)
SYSTEM-DESIGN.md ─ "what it does"─┤
                                 └──→ teams/* (role deep-dives)
README.md ─── "how to run it" ───┘
```

## Test your understanding

**Q1. Where would a frontend developer look to confirm a color is on-spec?**
**A.** `DESIGN.md` (theme palettes) and its code transcription `tokens.css`. Both must agree; the frontend must use the tokens.

**Q2. What is the difference between `SYSTEM-DESIGN.md`'s schema section and `.docs/map/03-database-schema.md`?**
**A.** `SYSTEM-DESIGN.md` describes the *target/intended* design; the map's `03-database-schema.md` documents the *actual implemented* schema (the 9 applied Flyway migrations). The map is ground truth for what exists now.

**Q3. Why is `README.md` separate from `.docs/`?**
**A.** `README.md` is the lightweight entry point shown at the repo root (how to run/build). `.docs/` is the deep study material. README links onward for anyone who wants more.

**Q4. If the design team changes the evening palette, which files must be updated?**
**A.** `DESIGN.md` (source of truth) and `tokens.css` (implementation). The `.docs` design team guide may also note the change so docs stay in sync.

**Q5. What is the relationship between the 3 dashboard zones and the code?**
**A.** They map to sections of `templates/index.html` (hero = Zone 1, carousel = Zone 2, forecasts/history = Zone 3), fed by `HomeController`/`DashboardService` — as described in both `SYSTEM-DESIGN.md` and the map.
