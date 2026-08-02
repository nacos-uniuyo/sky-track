# 06 — Testers Tough Questions (FAQ)

Cross-cutting questions spanning the testing material. Try to answer each before reading the answer.

## Q1. Explain the difference between unit tests and manual tests in this project.

**A.** Unit tests (JUnit/Mockito/MockRestServiceServer) verify individual classes in isolation, fast and offline — the core logic of services and API mapping. Manual tests run the real app and verify end-to-end behavior: login, page rendering, interactions, scheduler timing, and security. Unit tests are automated and repeatable; manual tests cover what automation does not yet.

## Q2. Why is `MockRestServiceServer` better than a real API call in `WeatherServiceTest`?

**A.** Tests must be deterministic, offline, and free. A real call depends on the network and an API key, and could vary or cost money. `MockRestServiceServer` intercepts the `RestClient`, asserts the exact URL, and returns canned JSON — fast and repeatable.

## Q3. How do you test the delta computation without a database?

**A.** Mock `WeatherSnapshotRepository` with `@Mock`, stub `findFirst...Desc` and `findFirst...Before...Desc` to return prepared `WeatherSnapshot` objects, then call `deltaService.deltaFor(1L)` and assert the `DeltaView`. No database is needed because the repository is faked.

## Q4. What is `ArgumentCaptor` and when would you use it?

**A.** It captures the argument passed to a mocked method so the test can inspect it. `SnapshotServiceTest` and `WeatherAlertServiceTest` use it to grab the `WeatherSnapshot`/`WeatherAlert` passed to `save(...)` and assert its fields.

## Q5. Why does `WeatherAlertServiceTest` assert with `verify(repo, never()).save(...)`?

**A.** For the negative cases (pressure stable, first snapshot), the service should not create an alert. `verify(..., never())` proves that `save` was never called, confirming no alert was created.

## Q6. What would happen if the `ddl-auto: validate` check failed at startup?

**A.** The app would fail to start — Hibernate verifies every entity matches the database schema. This is a safety net for the testers team: if entities and migrations drift, the app refuses to boot rather than silently corrupting data.

## Q7. How do you test the scheduled snapshot job?

**A.** Unit: `SnapshotServiceTest` tests `ingestCity` (one city). The scheduling itself is Spring infrastructure; in the running app you verify by checking `weather_snapshots` rows appear ~60s after startup and every 30 minutes.

## Q8. What is the CSRF token and why does every manual POST need it?

**A.** Spring Security's CSRF protection requires a valid `_csrf` token for state-changing requests to prevent cross-site request forgery. The token is embedded in rendered forms; curl tests must extract it and send it with the POST, or the request returns 403.

## Q9. What should you verify to confirm per-user scoping works?

**A.** Create two users, give each different cities, then confirm each session only sees its own cities via `/api/cities`, and that acting on another user's city id returns 404 (not found), not 200.

## Q10. If a test depends on `LocalTime.now()`, how is it made deterministic?

**A.** The service takes `LocalTime` as a parameter (`greeting(user, LocalTime.of(9, 0))`), so tests pass fixed times. There is no dependence on the system clock. (`themeClass` uses the same pattern.)

## Q11. How would you test the `forecastFor` grouping logic in `DashboardService`?

**A.** Mock `WeatherService.forecast(...)` to return a `ForecastResponse` with items spread across multiple days, then assert `forecastFor(city)` returns hourly items (first 24) and daily groups with correct min/max. The mock isolates it from real API data.

## Q12. What is the risk of testing only happy paths?

**A.** Edge cases (first snapshot, no previous, empty results, API failure, negative deltas) go unverified. The suite deliberately covers these (e.g. `deltaNullValuesWhenNoPrevious`, `noAlertWhenFirstSnapshot`, `weatherFailureYieldsNullReading`) to protect the important boundaries.

## Q13. Why does `WeatherServiceTest` set a fake key `"test-key"`?

**A.** So the test can assert the `appid` query parameter is present without embedding a real secret or depending on the environment. Tests must never rely on real credentials.

## Q14. What would you add to increase confidence in the auth flow?

**A.** A `@SpringBootTest` (or `@WebMvcTest` with security) verifying: `/` redirects to `/login` when unauthenticated, login with `email`/`password` succeeds, `/signup` auto-login lands on `/onboarding`, and logout invalidates the session.

## Q15. How does the testers team verify the frontend follows the design system?

**A.** Visually during manual testing — checking glassmorphism panels, theme colors, fonts, and that `prefers-reduced-motion` disables animations. This is a checklist item in Flow 2/Flow 7 rather than an automated assertion.
