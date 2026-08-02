# 08 — File Index (Master Checklist)

Every file in the Sky Track repository, one line each. Use this as the master checklist: if a file is not listed here or not explained in the map, the documentation is incomplete. Paths are relative to the repo root.

---

## Root

| File | Purpose | Where explained |
|------|---------|-----------------|
| `pom.xml` | Parent Maven POM (modules, dependency management) | [07-infrastructure.md](07-infrastructure.md) |
| `Dockerfile` | Production image build | [07-infrastructure.md](07-infrastructure.md) |
| `docker-compose.yml` | db + app orchestration | [07-infrastructure.md](07-infrastructure.md) |
| `.dockerignore` | Build context exclusions | [07-infrastructure.md](07-infrastructure.md) |
| `.env.example` | Committed env template (placeholder key) | [07-infrastructure.md](07-infrastructure.md) |
| `.gitignore` | Ignored files (incl. `.env`, `target/`) | [07-infrastructure.md](07-infrastructure.md) |
| `README.md` | Project readme | — |
| `AGENTS.md` | Rules for agents changing the code | teams/documentation |
| `DESIGN.md` | Visual design system | teams/design |
| `SYSTEM-DESIGN.md` | System/feature design | teams/documentation |
| `skills-lock.json` | Skills metadata (agent tooling) | — |

---

## `sky-track-core/` — pom

| File | Purpose | Where explained |
|------|---------|-----------------|
| `sky-track-core/pom.xml` | Core module build + dependencies | [07-infrastructure.md](07-infrastructure.md) |

## `sky-track-core/src/main/java/com/skywatch/weather/config/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `WeatherApiProperties.java` | Binds `weather.api.*` config | [05-backend-map.md](05-backend-map.md) |
| `WeatherApiConfig.java` | Creates the `RestClient` bean | [05-backend-map.md](05-backend-map.md) |
| `SchedulingConfig.java` | Enables scheduling | [05-backend-map.md](05-backend-map.md) |

## `sky-track-core/src/main/java/com/skywatch/weather/model/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `AppUser.java` | User entity | [05-backend-map.md](05-backend-map.md) |
| `TrackedCity.java` | Tracked city entity | [05-backend-map.md](05-backend-map.md) |
| `WeatherSnapshot.java` | Recorded weather entity | [05-backend-map.md](05-backend-map.md) |
| `WeatherAlert.java` | Anomaly alert entity | [05-backend-map.md](05-backend-map.md) |
| `CorridorRoute.java` | Travel corridor entity | [05-backend-map.md](05-backend-map.md) |

## `sky-track-core/src/main/java/com/skywatch/weather/repository/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `AppUserRepository.java` | User queries | [05-backend-map.md](05-backend-map.md) |
| `TrackedCityRepository.java` | City queries + ordering | [05-backend-map.md](05-backend-map.md) |
| `WeatherSnapshotRepository.java` | Snapshot queries | [05-backend-map.md](05-backend-map.md) |
| `WeatherAlertRepository.java` | Alert queries | [05-backend-map.md](05-backend-map.md) |
| `CorridorRouteRepository.java` | Corridor queries | [05-backend-map.md](05-backend-map.md) |

## `sky-track-core/src/main/java/com/skywatch/weather/service/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `WeatherService.java` | OpenWeatherMap client | [05-backend-map.md](05-backend-map.md) |
| `SnapshotService.java` | Scheduled snapshot ingestion | [05-backend-map.md](05-backend-map.md) |
| `DeltaService.java` | "Shift since last check" math | [05-backend-map.md](05-backend-map.md) |
| `WeatherAlertService.java` | Anomaly detection | [05-backend-map.md](05-backend-map.md) |
| `TrackedCityService.java` | City CRUD + ordering | [05-backend-map.md](05-backend-map.md) |
| `DashboardService.java` | Dashboard composition | [05-backend-map.md](05-backend-map.md) |
| `CorridorService.java` | Travel corridor generation | [05-backend-map.md](05-backend-map.md) |
| `GreetingService.java` | Greeting + theme class | [05-backend-map.md](05-backend-map.md) |
| `ContextualMessageService.java` | Friendly city messages | [05-backend-map.md](05-backend-map.md) |
| `SignupService.java` | Registration + hashing | [05-backend-map.md](05-backend-map.md) |
| `AppUserDetailsService.java` | UserDetails for Spring Security | [05-backend-map.md](05-backend-map.md) |

## `sky-track-core/src/main/java/com/skywatch/weather/dto/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `SignupRequest.java` | Signup form DTO | [05-backend-map.md](05-backend-map.md) |
| `TrackedCityRequest.java` | Add-city request DTO | [05-backend-map.md](05-backend-map.md) |
| `TrackedCityView.java` | City + weather + delta view | [05-backend-map.md](05-backend-map.md) |
| `DeltaView.java` | Delta values view | [05-backend-map.md](05-backend-map.md) |
| `ForecastBundle.java` | Hourly + daily forecasts | [05-backend-map.md](05-backend-map.md) |
| `HourlyView.java` | One hourly forecast item | [05-backend-map.md](05-backend-map.md) |
| `DailyView.java` | One daily forecast item | [05-backend-map.md](05-backend-map.md) |
| `SnapshotView.java` | History point for the chart | [05-backend-map.md](05-backend-map.md) |
| `CorridorPoint.java` | One corridor waypoint | [05-backend-map.md](05-backend-map.md) |
| `CorridorBundle.java` | Full corridor result | [05-backend-map.md](05-backend-map.md) |

## `sky-track-core/src/main/java/com/skywatch/weather/dto/weather/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `CurrentWeatherResponse.java` | `/data/2.5/weather` JSON | [05-backend-map.md](05-backend-map.md) |
| `ForecastResponse.java` | `/data/2.5/forecast` JSON | [05-backend-map.md](05-backend-map.md) |
| `OneCallResponse.java` | `/data/3.0/onecall` JSON | [05-backend-map.md](05-backend-map.md) |
| `GeoCityResponse.java` | Geocoding result JSON | [05-backend-map.md](05-backend-map.md) |

## `sky-track-core/src/test/java/com/skywatch/weather/service/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `WeatherServiceTest.java` | Mocked RestClient API tests | teams/testers |
| `GreetingServiceTest.java` | Greeting time-boundary tests | teams/testers |
| `DeltaServiceTest.java` | Delta computation tests | teams/testers |
| `WeatherAlertServiceTest.java` | Anomaly detection tests | teams/testers |
| `SnapshotServiceTest.java` | Weather-to-snapshot mapping test | teams/testers |
| `CorridorServiceTest.java` | Waypoint generation tests | teams/testers |

---

## `sky-track-web/` — pom

| File | Purpose | Where explained |
|------|---------|-----------------|
| `sky-track-web/pom.xml` | Web module build + dependencies | [07-infrastructure.md](07-infrastructure.md) |

## `sky-track-web/src/main/java/com/skywatch/weather/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `SkywatchWeatherApplication.java` | Spring Boot entry point | [05-backend-map.md](05-backend-map.md) |

## `sky-track-web/src/main/java/com/skywatch/weather/config/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `SecurityConfig.java` | Security filter chain + beans | [05-backend-map.md](05-backend-map.md) |
| `CurrentUserResolver.java` | Resolves the logged-in `AppUser` | [05-backend-map.md](05-backend-map.md) |

## `sky-track-web/src/main/java/com/skywatch/weather/controller/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `AuthController.java` | Login/signup pages + auto-login | [05-backend-map.md](05-backend-map.md) |
| `HomeController.java` | Dashboard page | [05-backend-map.md](05-backend-map.md) |
| `OnboardingController.java` | Primary city selection | [05-backend-map.md](05-backend-map.md) |
| `TrackedCityController.java` | City add/remove/primary forms | [05-backend-map.md](05-backend-map.md) |
| `CorridorController.java` | Corridor page + generation | [05-backend-map.md](05-backend-map.md) |
| `AlertsController.java` | Alerts page + acknowledge | [05-backend-map.md](05-backend-map.md) |
| `CityApiController.java` | REST `/api/cities` | [05-backend-map.md](05-backend-map.md) |
| `WeatherApiController.java` | REST `/api/weather/...` | [05-backend-map.md](05-backend-map.md) |
| `AlertApiController.java` | REST `/api/alerts` | [05-backend-map.md](05-backend-map.md) |

## `sky-track-web/src/main/resources/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `application.yml` | Base configuration | [07-infrastructure.md](07-infrastructure.md) |
| `application-prod.yml` | Production profile | [07-infrastructure.md](07-infrastructure.md) |

## `sky-track-web/src/main/resources/db/migration/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `V1__create_saved_locations.sql` | Old global locations table | [03-database-schema.md](03-database-schema.md) |
| `V2__create_app_users.sql` | Users table (full_name) | [03-database-schema.md](03-database-schema.md) |
| `V3__create_tracked_cities.sql` | Per-user tracked cities | [03-database-schema.md](03-database-schema.md) |
| `V4__create_weather_snapshots.sql` | Weather snapshots | [03-database-schema.md](03-database-schema.md) |
| `V5__create_weather_alerts.sql` | Weather alerts | [03-database-schema.md](03-database-schema.md) |
| `V6__create_corridor_routes.sql` | Corridor routes | [03-database-schema.md](03-database-schema.md) |
| `V7__drop_saved_locations.sql` | Drops saved_locations | [03-database-schema.md](03-database-schema.md) |
| `V8__alter_app_users_first_last_name.sql` | Splits full_name | [03-database-schema.md](03-database-schema.md) |
| `V9__add_sort_order_to_tracked_cities.sql` | Carousel ordering | [03-database-schema.md](03-database-schema.md) |

## `sky-track-web/src/main/resources/templates/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `index.html` | Dashboard (Zones 1-3 + search) | [06-frontend-map.md](06-frontend-map.md) |
| `onboarding.html` | Primary city selection | [06-frontend-map.md](06-frontend-map.md) |
| `corridor.html` | Travel corridor page | [06-frontend-map.md](06-frontend-map.md) |
| `alerts.html` | Alerts feed | [06-frontend-map.md](06-frontend-map.md) |
| `auth/login.html` | Login form | [06-frontend-map.md](06-frontend-map.md) |
| `auth/signup.html` | Signup form | [06-frontend-map.md](06-frontend-map.md) |
| `fragments/head.html` | Shared head fragment | [06-frontend-map.md](06-frontend-map.md) |
| `fragments/topbar.html` | Shared top bar fragment | [06-frontend-map.md](06-frontend-map.md) |

## `sky-track-web/src/main/resources/static/css/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `tokens.css` | Design tokens + themes | [06-frontend-map.md](06-frontend-map.md) |
| `app.css` | Component + layout styles | [06-frontend-map.md](06-frontend-map.md) |

## `sky-track-web/src/main/resources/static/js/`

| File | Purpose | Where explained |
|------|---------|-----------------|
| `liquid.js` | Animated liquid background | [06-frontend-map.md](06-frontend-map.md) |
| `clock.js` | Live clock | [06-frontend-map.md](06-frontend-map.md) |
| `weather-icons.js` | SVG weather icons | [06-frontend-map.md](06-frontend-map.md) |
| `dashboard.js` | Tap-to-switch city | [06-frontend-map.md](06-frontend-map.md) |
| `history-chart.js` | History chart + scrubber | [06-frontend-map.md](06-frontend-map.md) |

---

## `.docs/` — this documentation

| File | Purpose |
|------|---------|
| `.docs/map/README.md` | Map home / how to read |
| `.docs/map/00-overview.md` | Project overview |
| `.docs/map/01-architecture.md` | Architecture + diagrams |
| `.docs/map/02-request-lifecycle.md` | Request flow |
| `.docs/map/03-database-schema.md` | Schema + migrations |
| `.docs/map/04-data-flows.md` | Data pipelines |
| `.docs/map/05-backend-map.md` | Backend file map |
| `.docs/map/06-frontend-map.md` | Frontend file map |
| `.docs/map/07-infrastructure.md` | Build/deploy |
| `.docs/map/08-file-index.md` | This index |
| `.docs/map/teams/*/` | Per-team study guides |

---

*If a file is missing from this index, the map is incomplete — add it.*
