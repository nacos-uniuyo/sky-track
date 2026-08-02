# 04 — Weather API Integration

Sky Track gets live and forecast weather from **OpenWeatherMap**. The only class that talks to it is `WeatherService` in `sky-track-core`. HTTP is handled by Spring's `RestClient`.

## Configuration

`application.yml`:
```yaml
weather:
  api:
    base-url: https://api.openweathermap.org
    key: ${OPENWEATHER_API_KEY:}
  snapshot:
    fixed-rate-ms: ${SNAPSHOT_FIXED_RATE_MS:1800000}
```

These values bind to `WeatherApiProperties`:
```java
@ConfigurationProperties(prefix = "weather.api")
public class WeatherApiProperties {
    private String baseUrl;
    private String key;
}
```

`WeatherApiConfig` creates the single HTTP client:
```java
@Configuration
@EnableConfigurationProperties(WeatherApiProperties.class)
public class WeatherApiConfig {

    @Bean
    RestClient weatherRestClient(WeatherApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
```

## WeatherService — the client

```java
@Service
public class WeatherService {

    private static final String UNITS_METRIC = "metric";

    private final RestClient restClient;
    private final WeatherApiProperties properties;

    public CurrentWeatherResponse currentWeather(BigDecimal lat, BigDecimal lon) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("units", UNITS_METRIC)
                        .queryParam("appid", properties.getKey())
                        .build())
                .retrieve()
                .body(CurrentWeatherResponse.class);
    }
```

The pattern for every method is the same:
1. `restClient.get()` — build a GET request.
2. `.uri(...)` — path + query params (`lat`, `lon`, `units=metric`, `appid=<key>`).
3. `.retrieve()` — send and get the response.
4. `.body(SomeResponse.class)` — Jackson deserializes the JSON into a record.

## Endpoints used

| Method | Path | Purpose |
|--------|------|---------|
| `currentWeather` | `/data/2.5/weather` | Live conditions by coordinates |
| `forecast` | `/data/2.5/forecast` | 5-day / 3-hour forecast (free tier) |
| `oneCall` | `/data/3.0/onecall` | Current + hourly + daily (paid — see note) |
| `searchCities` | `/geo/1.0/direct` | Geocoding: query → lat/lon/country |

`searchCities` needs a generic list:
```java
public List<GeoCityResponse> searchCities(String query, int limit) {
    return restClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/geo/1.0/direct")
                    .queryParam("q", query)
                    .queryParam("limit", limit)
                    .queryParam("appid", properties.getKey())
                    .build())
            .retrieve()
            .body(new ParameterizedTypeReference<List<GeoCityResponse>>() {});
}
```

`ParameterizedTypeReference` tells Jackson the JSON is a `List<GeoCityResponse>`.

## Response DTOs (records)

Jackson maps JSON to Java records by matching field names. Records give immutable, readable DTOs.

```java
public record CurrentWeatherResponse(
        Coord coord,
        List<WeatherInfo> weather,
        Main main,
        Integer visibility,
        Wind wind,
        Sys sys,
        String name,
        Long dt
) {
    public record Main(BigDecimal temp, BigDecimal feels_like,
                       BigDecimal temp_min, BigDecimal temp_max,
                       Integer pressure, Integer humidity) { }
    public record WeatherInfo(Integer id, String main, String description, String icon) { }
    public record Wind(BigDecimal speed, Integer deg, BigDecimal gust) { }
    public record Sys(Long sunrise, Long sunset) { }
}
```

Notice: Jackson maps JSON `feels_like` → record component `feels_like` directly (snake_case component names are allowed). Nested records mirror the JSON nesting.

The other DTOs:
- `ForecastResponse` — `list[]` of forecast items + `city`. Each item has `dt`, `main`, `weather[]`, `wind`, `clouds`.
- `OneCallResponse` — `current`, `hourly[]`, `daily[]`, `alerts[]`.
- `GeoCityResponse` — `name`, `lat`, `lon`, `country`, `state`.

## Why forecasts use `/data/2.5/forecast` and not One Call

OpenWeatherMap's **One Call 3.0** (`/data/3.0/onecall`) requires a **separate paid subscription**. The free key returns:
```json
{"cod": 401, "message": "Please note that using One Call 3.0 requires a separate subscription..."}
```

So Sky Track uses the free `/data/2.5/forecast` endpoint and derives views from its 3-hour list:

```java
List<HourlyView> hourly = response.list().stream().limit(24).map(HourlyView::from).toList();

Map<LocalDate, List<ForecastResponse.ForecastItem>> grouped = response.list().stream()
        .collect(Collectors.groupingBy(
                item -> Instant.ofEpochSecond(item.dt()).atZone(ZoneOffset.UTC).toLocalDate()));
List<DailyView> daily = grouped.entrySet().stream().limit(7)
        .map(entry -> DailyView.fromGroup(entry.getKey(), entry.getValue()))
        .toList();
```

- **Hourly:** first 24 entries (8 per day × 3 days).
- **Daily:** group all entries by calendar day, then take min/max temperature per day.

## Where WeatherService is used

| Caller | Why |
|--------|-----|
| `DashboardService` | live conditions + forecast for the dashboard |
| `OnboardingController` | city search during onboarding |
| `CorridorController` / `CorridorService` | geocode origin/destination + weather at waypoints |
| `SnapshotService` | periodic snapshot recording |
| `CityApiController` | REST city search |

## Error handling

Callers that must not crash the page wrap external calls in `try/catch` and degrade gracefully (empty forecast, null weather). `WeatherService` itself lets exceptions propagate; the caller decides.

## Test your understanding

**Q1. What would happen if you called `oneCall` with the free API key?**
**A.** OpenWeatherMap returns `401` with a message about the paid One Call subscription. The call fails, so `DashboardService` never uses `oneCall` for display — it uses the free `/forecast` endpoint.

**Q2. Why is the geocoding path `/geo/1.0/direct` different from the weather paths?**
**A.** Geocoding lives under the `/geo/1.0` namespace, while weather lives under `/data/2.5`. That is why `base-url` is set to the root `https://api.openweathermap.org` and each method declares its full path.

**Q3. Why use records for the response DTOs?**
**A.** Records are immutable, compact, and match JSON structure naturally. Jackson deserializes them without boilerplate, and they are ideal read-only data carriers.

**Q4. What is `ParameterizedTypeReference` for?**
**A.** `retrieve().body(...)` needs a concrete type for Jackson. A raw `List.class` loses the element type, so `new ParameterizedTypeReference<List<GeoCityResponse>>() {}` preserves the full generic type at runtime.

**Q5. `units=metric` — what does it do?**
**A.** It tells OpenWeatherMap to return temperatures in Celsius and wind in m/s instead of the default Kelvin. Every call passes it, so all downstream math and display assume metric values.

**Q6. How is the API key kept secret?**
**A.** It is read from the `OPENWEATHER_API_KEY` environment variable (default empty) and injected via `WeatherApiProperties`. The real key lives in the gitignored `.env`; the committed `.env.example` contains only a placeholder.
