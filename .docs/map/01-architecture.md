# 01 — Architecture

This page shows how the pieces of Sky Track fit together. It is the "big picture" that the rest of the map fills in.

## Layers

The application is organized in layers, from the browser down to the database.

```
┌─────────────────────────────────────────────────────────────┐
│  BROWSER                                                    │
│  renders HTML (Thymeleaf) + runs JS (liquid bg, switching)  │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP (GET/POST)
┌──────────────────────────────▼──────────────────────────────┐
│  WEB LAYER  (sky-track-web)                                 │
│  SecurityConfig — auth + public-path rules                   │
│  CurrentUserResolver — who is logged in                      │
│  Controllers — MVC pages + REST JSON APIs                    │
└──────────────────────────────┬──────────────────────────────┘
                               │ calls
┌──────────────────────────────▼──────────────────────────────┐
│  SERVICE LAYER  (sky-track-core)                            │
│  WeatherService → OpenWeatherMap                             │
│  SnapshotService / DeltaService / WeatherAlertService        │
│  TrackedCityService / CorridorService                        │
│  GreetingService / ContextualMessageService                  │
│  SignupService / AppUserDetailsService                       │
└──────────────────────────────┬──────────────────────────────┘
                               │ uses
┌──────────────────────────────▼──────────────────────────────┐
│  DATA LAYER  (sky-track-core)                               │
│  JPA Entities (model/)                                      │
│  Spring Data Repositories (repository/)                     │
└──────────────────────────────┬──────────────────────────────┘
                               │ SQL
┌──────────────────────────────▼──────────────────────────────┐
│  POSTGRESQL DATABASE (via Docker, Flyway migrations)        │
└─────────────────────────────────────────────────────────────┘
```

## Component diagram

```mermaid
flowchart TB
    subgraph Browser
        HTML[Thymeleaf pages]
        JS[JavaScript: liquid, icons, switching, chart]
    end

    subgraph Web["sky-track-web"]
        Sec[SecurityConfig]
        Curr[CurrentUserResolver]
        AuthC[AuthController]
        HomeC[HomeController]
        OnbC[OnboardingController]
        TrackC[TrackedCityController]
        CorrC[CorridorController]
        AlertsC[AlertsController]
        CityApi[CityApiController]
        WeatherApi[WeatherApiController]
        AlertApi[AlertApiController]
    end

    subgraph Core["sky-track-core"]
        WS[WeatherService]
        SS[SnapshotService]
        DS[DeltaService]
        WAS[WeatherAlertService]
        TCS[TrackedCityService]
        CS[CorridorService]
        GS[GreetingService]
        CMS[ContextualMessageService]
        SUp[SignupService]
        UDS[AppUserDetailsService]
        DashS[DashboardService]
    end

    subgraph Data["sky-track-core data"]
        Repos[Repositories]
        Entities[Entities]
    end

    subgraph External
        OWM[OpenWeatherMap API]
    end

    Browser --> Sec
    Sec --> Controllers[Controllers]
    Controllers --> Curr

    AuthC --> SUp
    AuthC --> UDS
    HomeC --> DashS
    DashS --> WS
    DashS --> TCS
    DashS --> DS
    HomeC --> GS
    HomeC --> CMS
    HomeC --> AlertsC

    TrackC --> TCS
    OnbC --> TCS
    OnbC --> WS
    CorrC --> CS
    CS --> WS
    AlertsC --> WAS
    WeatherApi --> DashS
    WeatherApi --> DS
    AlertApi --> WAS

    SS --> WS
    SS --> WAS
    SS --> Repos

    WS --> OWM

    Services[Services] --> Repos
    Repos --> Entities
```

## Module dependency

```mermaid
graph LR
    Web[sky-track-web] --> Core[sky-track-core]
    Core --> Spring[Spring Boot starters]
    Web --> Spring
    Spring --> DB[(PostgreSQL)]
```

`sky-track-core` knows nothing about the web. `sky-track-web` depends on core and adds controllers, security, templates, and static assets.

## Dependency injection

Spring Boot wires the whole graph with **constructor injection**. Every service and controller declares its dependencies as `private final` fields and receives them through the constructor. For example:

```java
public class HomeController {
    private final CurrentUserResolver currentUserResolver;
    private final DashboardService dashboardService;

    public HomeController(CurrentUserResolver currentUserResolver, DashboardService dashboardService) {
        this.currentUserResolver = currentUserResolver;
        this.dashboardService = dashboardService;
    }
}
```

Spring finds the beans by type and injects them. This is why the whole chain can be followed by reading constructors.

## Security posture

- Only `/signup`, `/login`, `/css/**`, `/js/**` are public.
- Everything else requires an authenticated session.
- Login uses `email` as the username parameter.
- Passwords are BCrypt-hashed in `app_users.password_hash`.
- Data is scoped per user: tracked cities and alerts always resolve the current user.

## Configuration flow

`application.yml` (and `application-prod.yml`) provide values that Spring binds to `@ConfigurationProperties` (e.g. `WeatherApiProperties`), then services read via injection. Secrets come from environment variables, never from committed files.

Next: [02-request-lifecycle.md](02-request-lifecycle.md)
