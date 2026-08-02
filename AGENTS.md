# AGENTS.md

Java 21 + Spring Boot 3.3.5 web app (Maven, artifact `com.skywatch:weather`). Thymeleaf server-rendered pages, PostgreSQL, Flyway, JPA, Spring Security.

## Commands

Run all commands from the repo root (`sky-track/`).

```bash
mvn install -DskipTests -pl sky-track-core -am   # install core to local repo (required before running web standalone)
mvn spring-boot:run -pl sky-track-web            # dev server -> http://localhost:8080
mvn package -pl sky-track-web -am                # build boot jar
mvn test                                         # tests in sky-track-core
docker compose up --build                        # Dockerized full stack (db + app)
docker compose down -v                           # tear down containers and volumes
```

`spring-boot:run -pl sky-track-web` resolves `sky-track-core` from `~/.m2`, so run `mvn install -pl sky-track-core` first after changing core code.

## Local setup required

App will not start without a running PostgreSQL. The README documents setup; config lives in `sky-track-web/src/main/resources/application.yml`:

```yaml
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/skytrack}
spring.datasource.username=${DB_USERNAME:skytrack_user}
spring.datasource.password=${DB_PASSWORD:skywatch99}
weather.api.key=${OPENWEATHER_API_KEY:}
```

Override via env vars rather than editing config.

## Gotchas

- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate will not create/alter schema. All schema changes MUST be new Flyway files in `sky-track-web/src/main/resources/db/migration/` named `V<next#>__<description>.sql`; never edit an applied migration.
- `spring.jpa.open-in-view=false` — no lazy loading outside a transaction. Fetch relations eagerly or wrap service methods in `@Transactional`. Lazy `TrackedCity` proxies on `WeatherAlert` will throw `LazyInitializationException`; use repository ownership queries (`existsByIdAndTrackedCityUserId`) instead.
- Security is global: only `/signup`, `/login`, `/css/**`, `/js/**` are public; everything else requires auth. Form login username parameter is `email` (not `username`). Passwords are BCrypt-hashed in `app_users.password_hash`.
- Tracked cities and alerts ARE scoped per user via `tracked_cities.user_id` / `weather_alerts.tracked_city.user_id`. Always resolve the current user and scope queries by it.
- `created_at` columns are `insertable = false, updatable = false` — set by DB default, not by JPA.
- OpenWeatherMap One Call 3.0 requires a paid subscription — use the free `/data/2.5/forecast` endpoint. `WeatherService` exposes `currentWeather`, `forecast`, `searchCities` (geo), and `oneCall` (may 401 on free keys).

## Layout

- `sky-track-core` (`com.skywatch.weather`): `model/`, `repository/`, `service/`, `dto/`, `config/` — domain, persistence, weather API integration.
- `sky-track-web` (`com.skywatch.weather`): `controller/`, `config/` (security, current-user resolver), `SkywatchWeatherApplication`.
- `sky-track-web/src/main/resources/templates/` — Thymeleaf; `static/` holds `tokens.css`, `app.css`, and JS (liquid background, weather icons, dashboard switching, history chart). Frontend follows `DESIGN.md`.
- Weather API (OpenWeatherMap) is consumed via `WeatherService`; snapshots ingested on a schedule by `SnapshotService`.

## Agent Rules

### Progress logging

After every file change, feature addition, or bug fix, append an entry to [`.agents/plans/PROGRESS.md`](.agents/plans/PROGRESS.md) with the exact date and time. Newest entries go at the top (descending order). Format:

```markdown
## YYYY-MM-DD
- **HH:MM** — Description of what was done. Outcome.
```

### App name

The application is named **SKY TRACK**. Never call it or label it "Sky Watch", "Skywatch", or any other variation. Use "Sky Track" consistently in code, templates, strings, docs, and UI copy. The Java package (`com.skywatch.weather`), Maven artifact, and DB user are legacy and stay as-is.

### Code standards (school project)

- **No comments** — no `//`, `/* */`, or `/** */` Javadoc anywhere in the codebase.
- **No emojis** — not in code, templates, strings, or assets.
- **Generic, well-structured code** — plain and functional. No unnecessary abstractions or flair. Use conventional package/class layout, standard naming, and keep things simple.

### Design system

[`DESIGN.md`](DESIGN.md) is the single source of truth for ALL frontend work. Every template, stylesheet, and theme must follow its glassmorphism spec, palettes, typography, and component patterns. Do not introduce off-spec colors, fonts, or layouts. Before any frontend change, review DESIGN.md and ensure compliance.

### System design

[`SYSTEM-DESIGN.md`](SYSTEM-DESIGN.md) defines what we are building. Every feature, schema change, and component must conform to it. Read it before starting backend or frontend work.
