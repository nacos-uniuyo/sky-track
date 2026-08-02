# 05 — Coverage Map

This page maps what the automated tests cover and what still needs manual testing. Use it to know where to add tests and what to verify by hand.

## What the unit tests cover

| Test class | Code under test | Verified behavior |
|-----------|-----------------|-------------------|
| `WeatherServiceTest` | `WeatherService` | URL construction + JSON → DTO mapping for `/weather`, `/forecast`, `/geo/1.0/direct` |
| `GreetingServiceTest` | `GreetingService` | Time-of-day greeting boundaries (incl. 3 AM night case) |
| `DeltaServiceTest` | `DeltaService` | Latest-vs-previous delta math; empty/null edge cases |
| `WeatherAlertServiceTest` | `WeatherAlertService` | Pressure-drop threshold, wind threshold, first-snapshot no-op |
| `SnapshotServiceTest` | `SnapshotService.ingestCity` | Weather → snapshot field mapping |
| `CorridorServiceTest` | `CorridorService` | 6-waypoint generation; graceful failure on API error |

## What is NOT covered by automated tests

| Area | Why | How it is verified |
|------|-----|-------------------|
| Controllers (MVC) | No `@WebMvcTest`/`@SpringBootTest` yet | Manual testing (flows in 04) |
| Controllers (REST) | No slice tests | Manual/curl checks |
| Security filter chain | No `@SpringBootTest` with security | Manual security checks |
| Templates rendering | No template tests | Visual manual check |
| Repositories (SQL) | No `@DataJpaTest` | Manual + verified at boot (`ddl-auto: validate`) |
| Scheduler timing | Scheduler not invoked in unit tests | Manual (wait for ~60s) |
| End-to-end flows | No full-stack tests | Manual guide in 04 |

## Coverage by feature

| Feature | Unit tests? | Manual test? |
|---------|------------|--------------|
| Signup / auto-login | Signup logic via `SignupService` (partially) | Yes (Flow 1) |
| Onboarding city selection | — | Yes (Flow 1) |
| Live current weather | `WeatherServiceTest` (mapping) | Yes (Flow 2) |
| City CRUD + primary | — | Yes (Flow 2) |
| Forecasts | `WeatherServiceTest` (mapping) | Yes (Flow 3) |
| History chart data | — | Yes (Flow 3) |
| Delta / shift | `DeltaServiceTest` | Yes (Flow 4) |
| Corridor | `CorridorServiceTest` | Yes (Flow 5) |
| Alerts | `WeatherAlertServiceTest` | Yes (Flow 6) |
| Security / scoping | — | Yes (Flow 7) |
| Snapshot scheduler | `SnapshotServiceTest` (one city) | Yes (Flow 8) |

## How to add a new test

**For a pure-logic service (e.g. a new greeting rule):**
```java
class MyServiceTest {
    private final MyService service = new MyService();

    @Test
    void someBehavior() {
        assertThat(service.doSomething(input)).isEqualTo(expected);
    }
}
```

**For a service with dependencies (Mockito):**
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @Mock private SomeRepository repository;
    @InjectMocks private MyService service;

    @Test
    void someBehavior() {
        when(repository.findSomething()).thenReturn(...);
        assertThat(service.run()).isEqualTo(...);
    }
}
```

**For the HTTP client (MockRestServiceServer):** follow `WeatherServiceTest` — bind a fake server, expect a URL, return canned JSON, assert the mapped DTO.

**For a controller (future):** use `@WebMvcTest(SomeController.class)` with mocked services, or `@SpringBootTest` + `MockMvc` for full-stack.

## Running the tests

```bash
mvn test -pl sky-track-core          # unit tests
mvn package -pl sky-track-web -am    # full build incl. tests
```

## Test your understanding

**Q1. Which feature has the most test coverage, and why?**
**A.** Weather data handling (WeatherService + SnapshotService + DeltaService + AlertService + CorridorService) — the core tracking logic with thresholds and mapping that is easy to get wrong. The web layer is verified manually instead.

**Q2. Why is there no controller test yet?**
**A.** Controllers are thin and heavily dependent on services + Spring Security; testing them requires slice or full-context setup, which was deferred. Manual curl tests cover the endpoints for now.

**Q3. What would a `@WebMvcTest` for `HomeController` mock?**
**A.** It would mock `CurrentUserResolver`, `DashboardService`, `TrackedCityService`, `GreetingService`, `ContextualMessageService`, `WeatherService`, and `WeatherAlertRepository`, then assert the model attributes and view name.

**Q4. If you found a bug in the alert threshold logic, which test would you extend first?**
**A.** `WeatherAlertServiceTest` — add a boundary case (e.g. exactly 5 hPa drop) to lock in the intended behavior.

**Q5. What is the risk of relying only on manual tests for security?**
**A.** Manual checks can be missed during refactors. The recommended next step is a security slice test (e.g. `@SpringBootTest` verifying unauthenticated `/` redirects and that scoping queries hold).
