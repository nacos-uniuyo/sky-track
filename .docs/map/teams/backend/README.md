# Backend Team — Study Guide

This is the complete backend study material for the Sky Track project. Everything in the backend is explained from scratch: entities, repositories, services, weather API integration, authentication, controllers, scheduling, and the REST API — with real code snippets, data flows, and tough questions with answers.

## Your role

The backend team owns everything that is **not visual**: the data model, the business logic, the weather API integration, authentication and security, and the HTTP endpoints. The frontend team builds pages that call your controllers; the design team defines how those pages look; the testers team tests what you build.

## What you own

| Area | Files | Read more |
|------|-------|-----------|
| Data model | `model/` — 5 JPA entities | [01-entities](01-entities-and-data-model.md) |
| Data access | `repository/` — 5 Spring Data repositories | [02-repositories](02-repositories.md) |
| Business logic | `service/` — 11 services | [03-services](03-services.md) |
| External API | `WeatherService` + OpenWeatherMap DTOs | [04-weather-api](04-weather-api-integration.md) |
| Authentication | `SecurityConfig`, `SignupService`, `AppUserDetailsService` | [05-auth](05-auth-and-security.md) |
| HTTP layer | 9 controllers (MVC + REST) | [06-controllers](06-controllers-and-rest.md) |
| Background jobs | `SnapshotService`, `WeatherAlertService` | [07-scheduling](07-scheduling-and-alerts.md) |
| Tough questions | Mixed quiz for interview prep | [08-faq](08-faq.md) |

## How the backend connects to the other teams

- **Frontend team** renders the pages produced by our MVC controllers (`HomeController`, `OnboardingController`, `CorridorController`, `AlertsController`, `AuthController`) and calls our REST API (`/api/...`).
- **Design team** defines `DESIGN.md`; we pass `themeClass` and data into templates so the design can apply.
- **Testers team** runs the unit tests in `sky-track-core/src/test/` and manually tests the flows we built.
- **Documentation team** maintains `.docs/`; our data model is documented in the map's `03-database-schema.md`.

## Architecture rule you must know

```
Controllers  →  Services  →  Repositories / WeatherService  →  Database / OpenWeatherMap
```

- Controllers never touch the database directly.
- Services contain all business logic.
- The current user is resolved once (`CurrentUserResolver`) and passed to services as an `AppUser`.
- `sky-track-core` has no web code; `sky-track-web` depends on it.

## Non-negotiable project rules

- App name is **Sky Track** (never "Sky Watch" / "Skywatch").
- No comments, no emojis, generic well-structured code.
- `ddl-auto: validate` — schema changes only via new Flyway migrations.
- `open-in-view: false` — no lazy loading outside transactions; use ownership queries.

---

Begin with [01-entities-and-data-model.md](01-entities-and-data-model.md).
