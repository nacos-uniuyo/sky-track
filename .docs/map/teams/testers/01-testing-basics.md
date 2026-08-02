# 01 — Testing Basics

Sky Track's automated tests use three main tools: **JUnit 5** (the test framework), **Mockito** (mocking dependencies), and **MockRestServiceServer** (mocking HTTP calls). Assertions use **AssertJ**.

## JUnit 5

Tests are plain classes with `@Test` methods:

```java
package com.skywatch.weather.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingServiceTest {

    private final GreetingService greetingService = new GreetingService();

    @Test
    void morningGreeting() {
        assertThat(greetingService.greeting(user(), LocalTime.of(9, 0)))
                .isEqualTo("Hi comrade John, Good morning");
    }
}
```

- No `public` modifier required on test methods (JUnit 5 is package-friendly).
- Test classes live in the same package as the code they test.

## Mockito — mocking dependencies

When a service depends on a repository or another service, Mockito creates a fake instead of the real one. This isolates the code under test.

```java
@ExtendWith(MockitoExtension.class)
class DeltaServiceTest {

    @Mock
    private WeatherSnapshotRepository weatherSnapshotRepository;

    @InjectMocks
    private DeltaService deltaService;

    @Test
    void deltaComparesLatestAgainstPrevious() {
        // arrange: tell the mock what to return
        when(weatherSnapshotRepository.findFirstByTrackedCityIdOrderByRecordedAtDesc(1L))
                .thenReturn(Optional.of(latest));

        // act
        Optional<DeltaView> delta = deltaService.deltaFor(1L);

        // assert
        assertThat(delta).isPresent();
    }
}
```

| Mockito feature | What it does |
|-----------------|--------------|
| `@Mock` | Creates a fake object for the field |
| `@InjectMocks` | Injects the mocks into the object under test |
| `@ExtendWith(MockitoExtension.class)` | Enables the annotations per test |
| `when(x).thenReturn(y)` | Stub: when `x` is called, return `y` |
| `verify(x).method(...)` | Assert `x.method(...)` was called |
| `verify(x, never()).method(...)` | Assert `x.method(...)` was NOT called |
| `ArgumentCaptor` | Capture the argument passed to a method |

## AssertJ — fluent assertions

```java
assertThat(delta).isPresent();
assertThat(delta.get().temperatureDelta()).isEqualByComparingTo("-4.0");
assertThat(response.name()).isEqualTo("Lagos");
assertThat(response.list()).hasSize(1);
```

- `isEqualByComparingTo` compares `BigDecimal` by numeric value (not scale), avoiding `0.0` vs `0.00` issues.
- `hasSize`, `isPresent`, `isEmpty`, `isNull` are all readable AssertJ methods.

## MockRestServiceServer — mocking HTTP

`WeatherServiceTest` tests the OpenWeatherMap client without making real network calls. It binds a fake HTTP server to the `RestClient`:

```java
@BeforeEach
void setUp() {
    RestClient.Builder builder = RestClient.builder()
            .baseUrl("https://api.openweathermap.org");
    server = MockRestServiceServer.bindTo(builder).build();
    RestClient restClient = builder.build();

    WeatherApiProperties properties = new WeatherApiProperties();
    properties.setBaseUrl("https://api.openweathermap.org");
    properties.setKey("test-key");

    weatherService = new WeatherService(restClient, properties);
}
```

Then a test declares an expected request and a canned response:

```java
server.expect(requestTo("https://api.openweathermap.org/data/2.5/weather?lat=6.5244&lon=3.3792&units=metric&appid=test-key"))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

CurrentWeatherResponse response = weatherService.currentWeather(
        new BigDecimal("6.5244"), new BigDecimal("3.3792"));
```

- `requestTo(...)` asserts the exact URL the service must call (validates query params!).
- `withSuccess(json, MediaType.APPLICATION_JSON)` returns the fake response body.

## The Arrange-Act-Assert shape

Every test follows the same three-part structure:

1. **Arrange** — build inputs, stub mocks (`when`/`server.expect`).
2. **Act** — call the method under test.
3. **Assert** — verify output (`assertThat`) or interactions (`verify`).

## Test your understanding

**Q1. What is the difference between `when(...).thenReturn(...)` and `verify(...)`?**
**A.** `when` stubs behavior (what a mock returns when called); `verify` asserts behavior (that a mock method was actually invoked, and how many times).

**Q2. Why use `isEqualByComparingTo` for `BigDecimal` instead of `isEqualTo`?**
**A.** `BigDecimal` equality includes scale, so `new BigDecimal("4.0")` and `new BigDecimal("4.00")` are not `equals`. `isEqualByComparingTo` compares numeric value, which is what matters for temperatures.

**Q3. What does `@InjectMocks` do?**
**A.** It creates the class under test and injects the `@Mock` fields into its constructor. Mockito wires the mocks automatically.

**Q4. Why does `WeatherServiceTest` not make real HTTP calls?**
**A.** Tests must be fast, deterministic, and free — they cannot depend on a live external API or a network key. `MockRestServiceServer` intercepts the `RestClient` and returns canned JSON.

**Q5. What is the purpose of `requestTo(...)` in the mock server test?**
**A.** It asserts the exact URL (including query parameters like `units=metric` and `appid`) the service builds, so a regression in URL construction is caught immediately.
