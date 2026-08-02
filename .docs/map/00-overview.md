# 00 — Project Overview

## What is Sky Track?

Sky Track is a **weather tracking web application**. It is not a passive weather viewer — it actively records weather data over time and lets users track, compare, and analyze conditions across multiple cities.

Key behaviors:

- Users sign up and pick a **primary city**, then add more cities to track.
- The dashboard shows live conditions for the active city with a glassmorphism look.
- Weather is recorded into snapshots on a schedule, so Sky Track can show **"shift since last check"** deltas and historical charts.
- Sky Track watches for **weather anomalies** (pressure drops, wind surges) and raises alerts.
- Users can generate a **travel corridor**: a weather timeline between an origin and a destination city.

## Tech stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Build | Maven (multi-module) |
| Web rendering | Thymeleaf (server-side HTML) |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Persistence | Spring Data JPA (Hibernate) |
| Security | Spring Security, BCrypt |
| Weather data | OpenWeatherMap API |
| Frontend | HTML, CSS (custom design system), vanilla JavaScript |
| Deployment | Docker + Docker Compose |

## Repository layout

```
sky-track/                       (repo root)
├── pom.xml                      parent Maven POM — declares the two modules
├── sky-track-core/              JAR module — domain, persistence, weather API
├── sky-track-web/               JAR module — web layer: controllers, security, templates
├── Dockerfile                   builds the production image
├── docker-compose.yml           runs PostgreSQL + the app together
├── AGENTS.md                    rules agents must follow when changing code
├── DESIGN.md                    the design system (how it looks)
├── SYSTEM-DESIGN.md             the system design (what we build)
├── README.md                    quick-start and usage
└── .docs/                       this documentation (the map + team guides)
```

## The two Maven modules

The project is split into two modules so that **domain logic** stays separate from **web concerns**.

### `sky-track-core`

Holds everything that does not need to know about the web:

- `model/` — JPA entities (`AppUser`, `TrackedCity`, `WeatherSnapshot`, `WeatherAlert`, `CorridorRoute`)
- `repository/` — Spring Data repositories for database access
- `service/` — business logic: weather API calls, snapshot ingestion, delta calculation, anomaly detection, corridor generation, greeting and message services, authentication support
- `dto/` — data transfer objects (request and view models), including `dto/weather/` OpenWeatherMap response records
- `config/` — `WeatherApiProperties`, `WeatherApiConfig` (HTTP client), `SchedulingConfig`

`sky-track-web` depends on `sky-track-core`, so core code is reused by the web layer.

### `sky-track-web`

Holds everything that serves HTTP requests:

- `SkywatchWeatherApplication` — the Spring Boot entry point
- `controller/` — MVC controllers (pages) and REST controllers (JSON API)
- `config/` — `SecurityConfig`, `CurrentUserResolver`
- `resources/templates/` — Thymeleaf HTML pages and fragments
- `resources/static/` — CSS and JavaScript
- `resources/db/migration/` — Flyway SQL migrations
- `resources/application.yml` — configuration

## How a user's request reaches the database

```
Browser
  → HTTP request
  → Spring Security filter chain (SecurityConfig)
  → Controller (sky-track-web)
  → Service (sky-track-core)
  → Repository (sky-track-core)
  → PostgreSQL database
```

The reverse path returns the answer: the controller places data into a Thymeleaf model, and a template renders it into HTML for the browser.

## The external dependency

Sky Track calls the **OpenWeatherMap API** to get live and forecast weather. This is the only external service. The API key is read from the `OPENWEATHER_API_KEY` environment variable. `WeatherService` in core is the single class that talks to OpenWeatherMap.

## Design and system documents

- **[DESIGN.md](../../DESIGN.md)** — the visual language: glassmorphism, four time-of-day themes, typography, tokens. The frontend must follow it exactly.
- **[SYSTEM-DESIGN.md](../../SYSTEM-DESIGN.md)** — what the app is supposed to do: features, database schema, dashboard zones, API endpoints.

These two documents are the "why" behind the code. This map is the "how".

Next: [01-architecture.md](01-architecture.md)
