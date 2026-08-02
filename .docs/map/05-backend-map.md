# 05 — Backend File Map

Every Java file in the backend, with its purpose and its connections. Files are grouped by package. Paths are shown relative to `sky-track-core/src/main/java/com/skywatch/weather/` (core) or `sky-track-web/src/main/java/com/skywatch/weather/` (web).

---

## `sky-track-web` — entry point

### `SkywatchWeatherApplication.java`
- **What:** The Spring Boot entry point. `@SpringBootApplication` enables auto-configuration and component scanning over the `com.skywatch.weather` package (both modules, since web depends on core).
- **Connections:** Everything — it is the root that starts the whole context.

---

## `sky-track-core/config/`

### `WeatherApiProperties.java`
- **What:** Binds the `weather.api.*` settings (`base-url`, `key`) from `application.yml` into typed fields.
- **Connections:** Read by `WeatherApiConfig` and `WeatherService`. Configured via environment variables `OPENWEATHER_API_KEY`.

### `WeatherApiConfig.java`
- **What:** Registers the `WeatherApiProperties` and creates a `RestClient` bean pointed at the OpenWeatherMap base URL.
- **Connections:** `RestClient` is injected into `WeatherService`. This is the single HTTP client for the external API.

### `SchedulingConfig.java`
- **What:** Enables scheduling (`@EnableScheduling`), which lets `SnapshotService` run on a timer.
- **Connections:** Enables `SnapshotService.ingestAllCities()`.

---

## `sky-track-core/model/` (JPA entities)

### `AppUser.java`
- **What:** The user account. Fields: `id`, `firstName`, `lastName`, `email`, `passwordHash`, `role`, `enabled`, `createdAt`.
- **Connections:** Referenced by `TrackedCity.user`, `CorridorRoute.user`. Queried by `AppUserRepository`. Loaded by `CurrentUserResolver` and `AppUserDetailsService`. `firstName` feeds the greeting.

### `TrackedCity.java`
- **What:** A city a user tracks. Fields: `id`, `user`, `cityName`, `country`, `countryCode`, `latitude`, `longitude`, `primaryCity`, `sortOrder`, `createdAt`.
- **Connections:** Owned by `AppUser`. Parent of `WeatherSnapshot` and `WeatherAlert`. Saved via `TrackedCityRepository`. `latitude`/`longitude` drive every OpenWeatherMap call.

### `WeatherSnapshot.java`
- **What:** One recorded reading for a city. Fields: temp, feelsLike, humidity, windSpeed, windDirection, pressure, visibility, conditionCode, conditionText, sunrise, sunset, recordedAt.
- **Connections:** Belongs to `TrackedCity`. Created by `SnapshotService`. Read by `DeltaService` and `DashboardService.historyFor`.

### `WeatherAlert.java`
- **What:** A detected anomaly. Fields: `alertType`, `severity`, `title`, `description`, `detectedAt`, `acknowledged`.
- **Connections:** Belongs to `TrackedCity`. Created by `WeatherAlertService`. Listed on the alerts page/API.

### `CorridorRoute.java`
- **What:** A saved travel corridor between origin and destination cities.
- **Connections:** Owned by `AppUser`, references two `TrackedCity`. (Persisted but the current UI generates corridors on the fly via `CorridorService`.)

---

## `sky-track-core/repository/`

Spring Data JPA repositories — Spring generates implementations at runtime from method names.

### `AppUserRepository.java`
- Methods: `findByEmailIgnoreCase`, `existsByEmailIgnoreCase`.
- **Connections:** Used by `CurrentUserResolver`, `SignupService`, `AppUserDetailsService`.

### `TrackedCityRepository.java`
- Methods: `findByUserIdOrderBySortOrderAscIdAsc`, `findByUserIdAndId`, `findByUserIdAndPrimaryCityTrue`, `findFirstByUserIdAndPrimaryCityTrueOrderByCreatedAtAsc`, `existsByUserIdAndPrimaryCityTrue`, `countByUserId`.
- **Connections:** Used by `TrackedCityService`.

### `WeatherSnapshotRepository.java`
- Methods: `findFirstByTrackedCityIdOrderByRecordedAtDesc`, `findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc`, `findByTrackedCityIdOrderByRecordedAtAsc`, `findByTrackedCityIdAndRecordedAtBetweenOrderByRecordedAtAsc`.
- **Connections:** Used by `SnapshotService`, `DeltaService`, `DashboardService`, `WeatherAlertService`.

### `WeatherAlertRepository.java`
- Methods: `findByTrackedCityIdOrderByDetectedAtDesc`, `findAllByTrackedCityUserIdOrderByDetectedAtDesc`, `existsByIdAndTrackedCityUserId`.
- **Connections:** Used by `WeatherAlertService`, `AlertsController`, `AlertApiController`, `HomeController`.

### `CorridorRouteRepository.java`
- Methods: `findByUserIdOrderByCreatedAtDesc`.
- **Connections:** Used for corridor persistence.

---

## `sky-track-core/service/`

### `WeatherService.java`
- **What:** The only class that talks to OpenWeatherMap. Uses `RestClient`.
- Methods: `currentWeather(lat, lon)`, `forecast(lat, lon)`, `oneCall(lat, lon)`, `searchCities(query, limit)`.
- Endpoints: `/data/2.5/weather`, `/data/2.5/forecast`, `/data/3.0/onecall` (paid), `/geo/1.0/direct`.
- **Connections:** Injected into `DashboardService`, `OnboardingController`, `CorridorService`, `SnapshotService`, `CityApiController`, `CorridorController`.

### `SnapshotService.java`
- **What:** Scheduled ingestion. `@Scheduled(fixedRateString = "${weather.snapshot.fixed-rate-ms:1800000}")` runs every 30 minutes by default.
- Methods: `ingestAllCities()` (all cities), `ingestCity(city)` (one city: call API, save snapshot, evaluate alerts).
- **Connections:** Uses `WeatherService`, `WeatherSnapshotRepository`, `TrackedCityRepository`, `WeatherAlertService`.

### `DeltaService.java`
- **What:** Computes the "shift" between the latest and previous snapshot for a city.
- Method: `deltaFor(trackedCityId)` → `Optional<DeltaView>`.
- **Connections:** Uses `WeatherSnapshotRepository`. Consumed by `DashboardService` and `WeatherApiController`.

### `WeatherAlertService.java`
- **What:** Anomaly detection. Compares a new snapshot to the previous one.
- Thresholds: pressure drop ≥ 5 hPa → `PRESSURE_DROP` (HIGH); wind surge ≥ 12 m/s → `WIND_SURGE` (MEDIUM).
- Method: `evaluate(city, snapshot)`.
- **Connections:** Uses `WeatherAlertRepository` + `WeatherSnapshotRepository`. Called by `SnapshotService`.

### `TrackedCityService.java`
- **What:** CRUD + ordering for a user's cities.
- Methods: `listCities`, `addCity`, `removeCity`, `setPrimary`, `reorder`, `savePrimaryCity`, `findPrimaryCity`, `findCity`, `countByUser`.
- **Connections:** Uses `TrackedCityRepository`. Consumed by `HomeController`, `TrackedCityController`, `CityApiController`, `OnboardingController`, `DashboardService`.

### `DashboardService.java`
- **What:** Composes everything the dashboard needs: city views with live weather + delta, forecasts, and history.
- Methods: `cityViews`, `primaryCityView`, `forecastFor`, `historyFor`.
- **Connections:** Uses `TrackedCityService`, `WeatherService`, `DeltaService`, `WeatherSnapshotRepository`.

### `CorridorService.java`
- **What:** Generates a weather timeline between two coordinates by interpolating 6 waypoints and fetching current weather at each.
- Method: `generate(originName, originLat, originLon, destinationName, destinationLat, destinationLon)` → `CorridorBundle`.
- **Connections:** Uses `WeatherService`.

### `GreetingService.java`
- **What:** Time-of-day greeting and theme.
- Methods: `greeting(user, now)` → `"Hi comrade {firstName}, Good morning"`; `partOfDay(now)`; `themeClass(now)` → `theme-morning/-afternoon/-evening/-night`.
- **Connections:** Used by `HomeController`, `OnboardingController`, `AuthController`.

### `ContextualMessageService.java`
- **What:** Picks a friendly message about a city based on condition code, temperature, and wind.
- Method: `messageFor(city, conditionCode, temperature, windSpeed)`.
- **Connections:** Used by `HomeController`.

### `SignupService.java`
- **What:** Registers users. Normalizes email, rejects duplicates, BCrypt-hashes password, saves `AppUser`.
- Method: `createUser(SignupRequest)`; inner exception `EmailAlreadyRegisteredException`.
- **Connections:** Uses `AppUserRepository`, `PasswordEncoder`. Called by `AuthController`.

### `AppUserDetailsService.java`
- **What:** Bridges `AppUser` to Spring Security's `UserDetails` for login.
- Method: `loadUserByUsername(email)`.
- **Connections:** Uses `AppUserRepository`. Wired into `DaoAuthenticationProvider` in `SecurityConfig`.

---

## `sky-track-core/dto/`

### `SignupRequest.java`
Form fields for signup: `firstName`, `lastName`, `email`, `password`, with Bean Validation annotations.

### `TrackedCityRequest.java`
Record: `cityName`, `country`, `countryCode`, `latitude`, `longitude`. Sent when adding a city.

### `TrackedCityView.java`
Record combining a `TrackedCity` with live weather and delta. Static `from(city, weather)` builds it; `withDelta(...)` adds delta fields. Returned by dashboard and `/api/cities`.

### `DeltaView.java`
Record: `temperatureDelta`, `windSpeedDelta`, `pressureDelta`, `humidityDelta`, `since`, `currentTemperature`, `currentWindSpeed`.

### `ForecastBundle.java`, `HourlyView.java`, `DailyView.java`, `SnapshotView.java`
View models for forecasts and history. `HourlyView`/`DailyView` have `from(...)` factories mapping OpenWeatherMap response items.

### `CorridorPoint.java`, `CorridorBundle.java`
Corridor view models: a bundle of labeled points with weather at each.

### `dto/weather/CurrentWeatherResponse.java`
Record matching `/data/2.5/weather` JSON: `coord`, `weather[]`, `main`, `visibility`, `wind`, `sys`, `name`, `dt`.

### `dto/weather/ForecastResponse.java`
Record matching `/data/2.5/forecast` JSON: `list[]` of forecast items + `city`.

### `dto/weather/OneCallResponse.java`
Record matching `/data/3.0/onecall` JSON (current, hourly, daily, alerts). Not used for display because the free key lacks the One Call subscription.

### `dto/weather/GeoCityResponse.java`
Record matching the geocoding result: `name`, `lat`, `lon`, `country`, `state`.

---

## `sky-track-web/controller/`

### `AuthController.java`
- `GET /login`, `GET /signup`, `POST /signup`.
- `POST /signup` auto-logs the user in and redirects to `/onboarding`.
- **Connections:** `SignupService`, `AuthenticationManager`, `HttpSessionSecurityContextRepository`, `GreetingService`.

### `HomeController.java`
- `GET /` — the dashboard. Builds greeting, theme, city views, primary city view, forecast, history, city message, search results, alerts.
- **Connections:** `CurrentUserResolver`, `DashboardService`, `TrackedCityService`, `GreetingService`, `ContextualMessageService`, `WeatherService`, `WeatherAlertRepository`.

### `OnboardingController.java`
- `GET /onboarding`, `POST /onboarding/search`, `POST /onboarding/select`.
- Search calls `WeatherService.searchCities`; select saves the primary city.
- **Connections:** `TrackedCityService`, `WeatherService`, `GreetingService`, `CurrentUserResolver`.

### `TrackedCityController.java`
- `POST /cities` (add), `POST /cities/{id}/remove`, `POST /cities/{id}/primary`.
- **Connections:** `TrackedCityService`, `CurrentUserResolver`.

### `CorridorController.java`
- `GET /corridor`, `POST /corridor` (generate from origin + destination names).
- **Connections:** `CorridorService`, `WeatherService`, `GreetingService`, `CurrentUserResolver`.

### `AlertsController.java`
- `GET /alerts`, `POST /alerts/{id}/acknowledge`.
- **Connections:** `WeatherAlertRepository`, `GreetingService`, `CurrentUserResolver`.

### `CityApiController.java` (REST)
- `GET /api/cities`, `GET /api/cities/search?q=`, `POST /api/cities`, `DELETE /api/cities/{id}`, `POST /api/cities/{id}/primary`, `PUT /api/cities/reorder`.
- **Connections:** `DashboardService`, `TrackedCityService`, `WeatherService`, `CurrentUserResolver`.

### `WeatherApiController.java` (REST)
- `GET /api/weather/{cityId}/delta`, `GET /api/weather/{cityId}/forecast`, `GET /api/weather/{cityId}/snapshots?days=`.
- **Connections:** `DeltaService`, `DashboardService`, `TrackedCityService`, `CurrentUserResolver`.

### `AlertApiController.java` (REST)
- `GET /api/alerts`, `GET /api/alerts/{cityId}`, `PATCH /api/alerts/{id}/acknowledge`.
- **Connections:** `WeatherAlertRepository`, `TrackedCityService`, `CurrentUserResolver`.

---

## `sky-track-web/config/`

### `SecurityConfig.java`
- **What:** The security filter chain.
- Public paths: `/signup`, `/login`, `/css/**`, `/js/**`. Everything else requires auth.
- Form login uses `email` as the username parameter.
- Session fixation `changeSessionId`; logout invalidates the session and deletes the cookie.
- Beans: `AuthenticationManager`, `DaoAuthenticationProvider`, `PasswordEncoder` (BCrypt).
- **Connections:** Uses `AppUserDetailsService`. Guards every controller.

### `CurrentUserResolver.java`
- **What:** Resolves the authenticated user to an `AppUser` entity.
- Method: `currentUser(Authentication)` → `AppUserRepository.findByEmailIgnoreCase(authentication.getName())`.
- **Connections:** Injected into every controller that needs the current user.

---

## Dependency summary (who uses what)

```
Controllers  →  Services  →  Repositories / WeatherService  →  Database / OpenWeatherMap
                 ↑
CurrentUserResolver (AppUser) feeds every controller
```

Next: [06-frontend-map.md](06-frontend-map.md)
