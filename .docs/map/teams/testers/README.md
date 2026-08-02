# Testers Team — Study Guide

This is the complete testing study material for the Sky Track project. It explains the automated test suite in `sky-track-core/src/test/`, the mocking patterns used, how to run the tests, what is and is not covered, and how to manually test the full app.

## Your role

The testers team verifies that Sky Track works correctly. Two kinds of work:

1. **Automated tests** — unit tests written with JUnit 5 + Mockito + MockRestServiceServer.
2. **Manual testing** — verifying real user flows against the running app (signup → onboarding → dashboard → corridor → alerts).

## What you own

| Area | Files | Read more |
|------|-------|-----------|
| Testing stack | JUnit 5, Mockito, MockRestServiceServer, AssertJ | [01-basics](01-testing-basics.md) |
| API client tests | `WeatherServiceTest` | [02-weather-service](02-weather-service-test.md) |
| Service tests | Greeting, Delta, Alert, Snapshot, Corridor | [03-service-tests](03-service-tests.md) |
| Manual testing | Real flows + curl scripts | [04-manual](04-manual-test-guide.md) |
| Coverage | What is tested vs not | [05-coverage](05-coverage-map.md) |
| Tough questions | Interview-style quiz | [06-faq](06-faq.md) |

## How the testers team connects to the others

- **Backend team** writes the code you test; your tests protect their logic (delta math, alert thresholds, snapshot mapping, greeting boundaries).
- **Frontend team** builds the pages you manually verify.
- **Design team** defines the look you visually confirm.
- **Documentation team** records how to run the tests.

## How to run the tests

```bash
# Run the core unit tests
mvn test -pl sky-track-core

# Build the whole project (also runs tests)
mvn package -pl sky-track-web -am
```

All tests live in `sky-track-core/src/test/java/com/skywatch/weather/service/`.

## Current test inventory

| Test class | What it covers | Test count |
|-----------|----------------|------------|
| `WeatherServiceTest` | OpenWeatherMap JSON → DTO mapping (mocked HTTP) | 3 |
| `GreetingServiceTest` | Greeting time boundaries | 5 |
| `DeltaServiceTest` | Delta computation against snapshots | 3 |
| `WeatherAlertServiceTest` | Anomaly detection thresholds | 3 |
| `SnapshotServiceTest` | Weather → snapshot mapping | 1 |
| `CorridorServiceTest` | Waypoint generation + failure fallback | 2 |
| **Total** | | **17** |

## What is NOT covered (manual testing needed)

- Controllers (MVC + REST) — no `@WebMvcTest` yet.
- Security filter chain behavior.
- Templates rendering.
- Database integration (`@DataJpaTest`).
- End-to-end flows — covered manually in [04-manual](04-manual-test-guide.md).

---

Begin with [01-testing-basics.md](01-testing-basics.md).
