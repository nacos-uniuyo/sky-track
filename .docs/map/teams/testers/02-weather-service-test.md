# 02 — WeatherServiceTest

`WeatherServiceTest` verifies that `WeatherService` correctly calls OpenWeatherMap and maps the JSON responses into DTOs. It is the only test that mocks the HTTP layer (via `MockRestServiceServer`).

## Setup — bind a fake server to the RestClient

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

The fake key `"test-key"` is used so no real secret is involved.

## Test 1 — current weather parsing

```java
@Test
void currentWeatherParsesResponse() {
    String json = """
            {
              "coord": {"lon": 3.3792, "lat": 6.5244},
              "weather": [{"id": 800, "main": "Clear", "description": "clear sky", "icon": "01d"}],
              "main": {
                "temp": 28.4,
                "feels_like": 29.1,
                "temp_min": 27.5,
                "temp_max": 29.3,
                "pressure": 1012,
                "humidity": 68
              },
              "visibility": 10000,
              "wind": {"speed": 3.6, "deg": 180},
              "sys": {"sunrise": 1754160000, "sunset": 1754200000},
              "name": "Lagos",
              "dt": 1754200000
            }
            """;

    server.expect(requestTo("https://api.openweathermap.org/data/2.5/weather?lat=6.5244&lon=3.3792&units=metric&appid=test-key"))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

    CurrentWeatherResponse response = weatherService.currentWeather(
            new BigDecimal("6.5244"), new BigDecimal("3.3792"));

    assertThat(response.name()).isEqualTo("Lagos");
    assertThat(response.main().temp()).isEqualByComparingTo("28.4");
    assertThat(response.main().humidity()).isEqualTo(68);
    assertThat(response.weather().get(0).description()).isEqualTo("clear sky");
    assertThat(response.wind().speed()).isEqualByComparingTo("3.6");
}
```

**What it proves:**
- The URL is built correctly (path + `lat`/`lon`/`units=metric`/`appid`).
- The JSON maps into the `CurrentWeatherResponse` record: top-level, nested `main`, nested `weather[]`, nested `wind`.

## Test 2 — forecast parsing

```java
@Test
void forecastParsesResponse() {
    String json = """
            {
              "list": [
                {
                  "dt": 1754200000,
                  "main": {"temp": 28.4, "feels_like": 29.1, "humidity": 68, "pressure": 1012},
                  "weather": [{"id": 800, "main": "Clear", "description": "clear sky", "icon": "01d"}],
                  "wind": {"speed": 3.6, "deg": 180},
                  "clouds": {"all": 5}
                }
              ],
              "city": {"id": 2332459, "name": "Lagos", "country": "NG"}
            }
            """;

    server.expect(requestTo("https://api.openweathermap.org/data/2.5/forecast?lat=6.5244&lon=3.3792&units=metric&appid=test-key"))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

    ForecastResponse response = weatherService.forecast(
            new BigDecimal("6.5244"), new BigDecimal("3.3792"));

    assertThat(response.city().name()).isEqualTo("Lagos");
    assertThat(response.list()).hasSize(1);
    assertThat(response.list().get(0).main().temp()).isEqualByComparingTo("28.4");
}
```

**What it proves:** the `/forecast` URL and the `ForecastResponse.list`/`city` mapping work.

## Test 3 — city search parsing

```java
@Test
void searchCitiesParsesResponse() {
    String json = """
            [
              {"name": "Lagos", "lat": 6.5244, "lon": 3.3792, "country": "NG", "state": "Lagos"}
            ]
            """;

    server.expect(requestTo("https://api.openweathermap.org/geo/1.0/direct?q=Lagos&limit=5&appid=test-key"))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

    List<GeoCityResponse> response = weatherService.searchCities("Lagos", 5);

    assertThat(response).hasSize(1);
    assertThat(response.get(0).name()).isEqualTo("Lagos");
    assertThat(response.get(0).country()).isEqualTo("NG");
    assertThat(response.get(0).lat()).isEqualByComparingTo("6.5244");
}
```

**What it proves:** the `/geo/1.0/direct` URL and the generic `List<GeoCityResponse>` deserialization (via `ParameterizedTypeReference`) work.

## Why these tests matter

- They protect the **contract with OpenWeatherMap**: if the URL or field mapping breaks, these tests fail even though the code compiles.
- They catch **naming mismatches** (e.g. Jackson can't map `feels_like`), which would otherwise appear only at runtime.
- They run **offline and free** — no API key or network needed.

## Test your understanding

**Q1. Why does each test embed a full JSON string instead of a fixture file?**
**A.** Inline text blocks keep each test self-contained and readable. The JSON mirrors the real OpenWeatherMap response shape, so it doubles as documentation of the API contract.

**Q2. What happens if the service builds the wrong URL?**
**A.** `requestTo(...)` expects an exact URL; a mismatch fails the test immediately, catching regressions in path or query-parameter construction.

**Q3. Why is `"appid=test-key"` in the expected URL but not a real key?**
**A.** The test uses a fake key injected through `WeatherApiProperties`, so it can assert the parameter is present without exposing or depending on a real secret.

**Q4. How would you add a test for `oneCall`?**
**A.** Add a test with a canned `/data/3.0/onecall` JSON response, `server.expect(requestTo(".../data/3.0/onecall?lat=...&lon=...&units=metric&exclude=minutely&appid=test-key"))`, call `weatherService.oneCall(...)`, and assert on `current`, `hourly`, `daily`.

**Q5. What does this test class NOT verify?**
**A.** It does not verify that `DashboardService` uses the parsed data correctly, nor real network behavior. Those are covered by other unit tests and manual testing.
