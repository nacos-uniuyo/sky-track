# 08 — Backend Tough Questions (FAQ)

A roundup of the hardest questions you might be asked about the backend — spanning all topics. Try to answer each one before reading the answer.

## Q1. Walk through what happens when a logged-in user opens the dashboard `/`.

**A.**
1. Browser GET `/` → Spring Security filter chain → session is authenticated → `HomeController.index` runs.
2. `CurrentUserResolver.currentUser(authentication)` loads the `AppUser` by email from `app_users`.
3. `DashboardService.cityViews(user)` iterates the user's tracked cities; for each it:
   - loads the city rows (`TrackedCityRepository`),
   - calls OpenWeatherMap `/data/2.5/weather` for live conditions,
   - computes the delta from the two latest snapshots (`DeltaService`),
   - builds a `TrackedCityView`.
4. `forecastFor(city)` calls `/data/2.5/forecast` and builds hourly + daily views.
5. `historyFor(city, 7)` reads snapshots from the database for the chart.
6. All objects are added to the Thymeleaf `Model`, and `templates/index.html` renders HTML.
7. The browser loads CSS/JS; `history-chart.js` calls `GET /api/weather/{id}/snapshots` to draw the chart.

## Q2. Why is the app split into `sky-track-core` and `sky-track-web`? What would break if core depended on web?

**A.** Separation of concerns. Core holds domain, persistence, and external API logic with no web knowledge; web adds controllers, security, and templates. If core depended on web, you'd have a circular dependency (web → core → web), compile failures, and you could never reuse core (e.g. in a batch job or another UI). Core compiles and tests standalone.

## Q3. Explain the `LazyInitializationException` problem and the exact fix used in this project.

**A.** `spring.jpa.open-in-view=false` means Hibernate closes the persistence context when the request's transaction ends. Accessing a lazy relationship (e.g. `alert.getTrackedCity().getUser()`) afterwards throws `LazyInitializationException`. The fix used here is **ownership queries pushed into SQL**: `WeatherAlertRepository.existsByIdAndTrackedCityUserId(id, userId)` generates `WHERE id = ? AND tracked_city.user_id = ?`, so the join happens in the database and no proxy is ever touched.

## Q4. What is IDOR and how does Sky Track prevent it?

**A.** Insecure Direct Object Reference: accessing another user's record by guessing an id (e.g. `DELETE /api/cities/5` where 5 belongs to someone else). Sky Track prevents it by:
- resolving the current user from the session (`CurrentUserResolver`), never from the request,
- scoping every query by `userId` (`findByUserIdAndId`, `existsByIdAndTrackedCityUserId`),
- returning `404` (not the data) when the resource is not owned.

## Q5. Why `ddl-auto: validate` and why Flyway migrations instead of letting Hibernate create tables?

**A.** `validate` makes Hibernate check the schema matches the entities and fail fast if not. Flyway is the single source of truth for schema evolution, applied in version order, so every environment ends up identical. `create`/`update` would silently diverge and lose data. Rule: never edit an applied migration — add a new `V<n>`.

## Q6. How is the "Shift since last check" delta actually computed?

**A.** `DeltaService.deltaFor(cityId)`:
1. `findFirstByTrackedCityIdOrderByRecordedAtDesc` → the latest snapshot.
2. `findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(cityId, latest.recordedAt)` → the previous snapshot.
3. `latest.temperature - previous.temperature` (and wind/pressure/humidity).
4. Returns `DeltaView` with the deltas and `since = previous.recordedAt`.
If no previous exists, deltas are `null` and the badge is omitted.

## Q7. What is the purpose of the two internal modules' `dependencyManagement` in the parent POM?

**A.** The parent POM declares `sky-track-core` and `sky-track-web` in `<dependencyManagement>` so modules can depend on each other with a consistent version (`${project.version}`). `sky-track-web` declares core as a dependency without repeating the version.

## Q8. How would you add a new REST endpoint that returns the latest snapshot for a city?

**A.**
1. Add a repository method if needed: `findFirstByTrackedCityIdOrderByRecordedAtDesc` already exists.
2. Add a service method (e.g. in `DashboardService` or a snapshot service) returning the latest `SnapshotView`.
3. Add a controller method:
```java
@GetMapping("/{cityId}/snapshots/latest")
public ResponseEntity<SnapshotView> latest(Authentication authentication, @PathVariable Long cityId) {
    AppUser user = currentUserResolver.currentUser(authentication);
    if (trackedCityService.findCity(user, cityId).isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    return dashboardService.latestSnapshot(cityId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
```
4. The endpoint is automatically authenticated because `anyRequest().authenticated()`.

## Q9. Why does `DashboardService` catch `RuntimeException` around external API calls but let internal calls fail?

**A.** The OpenWeatherMap calls are an external dependency with no guarantee of success (network, quota, key). The dashboard must still render, so failures degrade to empty data. Internal database/repository calls are expected to succeed — if they fail, that's a real bug that should surface loudly (and roll back the transaction).

## Q10. What does `saveContext` do in the signup flow and why is it required?

**A.** After `authenticationManager.authenticate(...)`, the `SecurityContext` is set in `SecurityContextHolder` (thread-local) — but that alone does not survive the redirect. `securityContextRepository.saveContext(context, request, response)` writes the context into the HTTP session, so the follow-up `GET /onboarding` request is already authenticated.

## Q11. If two users both track London, why don't their snapshots mix?

**A.** Snapshots belong to a `TrackedCity`, and each `TrackedCity` belongs to exactly one `AppUser` (FK `user_id`). Every query is scoped through the user's id: `findByTrackedCityId...` is only ever called with city ids the user owns (validated by `trackedCityService.findCity(user, id)`).

## Q12. How do you test the backend without a real OpenWeatherMap key?

**A.** Unit tests mock the HTTP layer:
- `WeatherServiceTest` uses `MockRestServiceServer.bindTo(RestClient.Builder)` to stub responses and verify request URLs and JSON mapping.
- `SnapshotServiceTest` mocks `WeatherService` and asserts the mapping into a `WeatherSnapshot`.
- `CorridorServiceTest`, `DeltaServiceTest`, `WeatherAlertServiceTest`, `GreetingServiceTest` mock repositories/services with Mockito.

Run them with `mvn test -pl sky-track-core`.

## Q13. What is the difference between `@Scheduled`, `@EnableScheduling`, and the `@Transactional` on the scheduled method?

**A.**
- `@EnableScheduling` (on `SchedulingConfig`) activates Spring's scheduling infrastructure.
- `@Scheduled` (on `SnapshotService.ingestAllCities`) registers that method to run on a timer.
- `@Transactional` (on the same method) wraps each run in a database transaction so saves and alert checks are atomic.

## Q14. Why does the frontend switch cities without a page reload?

**A.** The dashboard is fully server-rendered with **all** cities' data embedded as `th:data-*` attributes on each card. `dashboard.js` reads those attributes and swaps the hero DOM client-side. The only backend call is `history-chart.js` fetching snapshots for the chart. This keeps interactions instant while staying server-rendered.

## Q15. What would you change to make snapshots configurable per user?

**A.** Move the interval from a global property to per-city/per-user configuration, e.g. add a `snapshot_interval` column to `tracked_cities`, and have `SnapshotService` use a dynamic scheduling approach (compute next run per city instead of one `@Scheduled` rate). Also handle each city's run independently so one slow API call does not block the batch.
