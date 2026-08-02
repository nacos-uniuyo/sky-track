# 03 — Services

Services contain all business logic. They are annotated `@Service`, injected via constructor, and never know about the web layer. They live in `sky-track-core/src/main/java/com/skywatch/weather/service/`.

## Service map

| Service | Responsibility |
|---------|----------------|
| `WeatherService` | Talks to OpenWeatherMap (covered in [04](04-weather-api-integration.md)) |
| `TrackedCityService` | CRUD + ordering of a user's cities |
| `DashboardService` | Composes the dashboard's data |
| `DeltaService` | "Shift since last check" math |
| `SnapshotService` | Scheduled recording of weather (covered in [07](07-scheduling-and-alerts.md)) |
| `WeatherAlertService` | Anomaly detection (covered in [07](07-scheduling-and-alerts.md)) |
| `CorridorService` | Travel corridor waypoints |
| `GreetingService` | Time-of-day greeting + theme class |
| `ContextualMessageService` | Friendly city messages |
| `SignupService` | Registration |
| `AppUserDetailsService` | Bridges user → Spring Security (covered in [05](05-auth-and-security.md)) |

## TrackedCityService — the CRUD core

```java
@Service
public class TrackedCityService {

    private final TrackedCityRepository trackedCityRepository;

    public TrackedCityService(TrackedCityRepository trackedCityRepository) {
        this.trackedCityRepository = trackedCityRepository;
    }
```

**listCities** — ordered by sort order:
```java
public List<TrackedCity> listCities(AppUser user) {
    return trackedCityRepository.findByUserIdOrderBySortOrderAscIdAsc(user.getId());
}
```

**addCity** — the first city becomes primary automatically:
```java
@Transactional
public TrackedCity addCity(AppUser user, TrackedCityRequest request) {
    boolean hasPrimary = trackedCityRepository.existsByUserIdAndPrimaryCityTrue(user.getId());
    int nextOrder = (int) trackedCityRepository.countByUserId(user.getId()) + 1;

    TrackedCity city = new TrackedCity();
    city.setUser(user);
    city.setCityName(request.cityName());
    city.setCountry(request.country());
    city.setCountryCode(request.countryCode());
    city.setLatitude(request.latitude());
    city.setLongitude(request.longitude());
    city.setPrimaryCity(!hasPrimary);
    city.setSortOrder(nextOrder);
    return trackedCityRepository.save(city);
}
```

**removeCity** — if the primary is removed, promote the next city:
```java
@Transactional
public void removeCity(AppUser user, Long cityId) {
    Optional<TrackedCity> target = trackedCityRepository.findByUserIdAndId(user.getId(), cityId);
    if (target.isEmpty()) {
        return;
    }
    boolean wasPrimary = target.get().isPrimaryCity();
    trackedCityRepository.delete(target.get());
    if (wasPrimary) {
        trackedCityRepository.findByUserIdOrderBySortOrderAscIdAsc(user.getId())
                .stream()
                .findFirst()
                .ifPresent(city -> city.setPrimaryCity(true));
    }
}
```

**setPrimary** — demotes the current primary, promotes the target:
```java
@Transactional
public void setPrimary(AppUser user, Long cityId) {
    Optional<TrackedCity> target = trackedCityRepository.findByUserIdAndId(user.getId(), cityId);
    if (target.isEmpty()) {
        return;
    }
    trackedCityRepository.findByUserIdAndPrimaryCityTrue(user.getId())
            .filter(city -> !city.getId().equals(cityId))
            .ifPresent(city -> city.setPrimaryCity(false));
    target.get().setPrimaryCity(true);
    trackedCityRepository.flush();
}
```

**savePrimaryCity** — used by onboarding to force a primary:
```java
@Transactional
public TrackedCity savePrimaryCity(AppUser user, TrackedCityRequest request) {
    trackedCityRepository.findByUserIdAndPrimaryCityTrue(user.getId())
            .ifPresent(city -> city.setPrimaryCity(false));
    ...
    city.setPrimaryCity(true);
    ...
}
```

**Why `@Transactional` matters:** `open-in-view: false` means lazy relations need an open session. `@Transactional` wraps the method so the session stays open until it returns, letting entity mutations be flushed and lazy proxies resolve.

## DashboardService — composing the dashboard

```java
@Service
public class DashboardService {

    private final TrackedCityService trackedCityService;
    private final WeatherService weatherService;
    private final DeltaService deltaService;
    private final WeatherSnapshotRepository weatherSnapshotRepository;
```

**cityViews** — every city + its live weather + delta:
```java
public List<TrackedCityView> cityViews(AppUser user) {
    return trackedCityService.listCities(user).stream()
            .map(city -> enrich(city, currentWeather(city)))
            .toList();
}
```

**forecastFor** — 24 hourly items + daily items grouped by calendar day:
```java
public ForecastBundle forecastFor(TrackedCity city) {
    try {
        var response = weatherService.forecast(city.getLatitude(), city.getLongitude());
        List<HourlyView> hourly = response.list().stream().limit(24).map(HourlyView::from).toList();

        Map<LocalDate, List<ForecastResponse.ForecastItem>> grouped = response.list().stream()
                .collect(Collectors.groupingBy(
                        item -> Instant.ofEpochSecond(item.dt()).atZone(ZoneOffset.UTC).toLocalDate()));

        List<DailyView> daily = grouped.entrySet().stream()
                .limit(7)
                .map(entry -> DailyView.fromGroup(entry.getKey(), entry.getValue()))
                .toList();

        return new ForecastBundle(hourly, daily);
    } catch (RuntimeException exception) {
        return new ForecastBundle(List.of(), List.of());
    }
}
```

**historyFor** — snapshots from the database for the chart:
```java
public List<SnapshotView> historyFor(TrackedCity city, int days) {
    OffsetDateTime from = OffsetDateTime.now().minusDays(days);
    return weatherSnapshotRepository
            .findByTrackedCityIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                    city.getId(), from, OffsetDateTime.now())
            .stream()
            .map(snapshot -> new SnapshotView(snapshot.getRecordedAt(), snapshot.getTemperature()))
            .toList();
}
```

**enrich** — adds the delta to a city view:
```java
private TrackedCityView enrich(TrackedCity city, CurrentWeatherResponse weather) {
    TrackedCityView view = TrackedCityView.from(city, weather);
    return deltaService.deltaFor(city.getId())
            .map(delta -> view.withDelta(delta.temperatureDelta(), delta.since()))
            .orElse(view);
}
```

Note the defensive `try/catch` around external calls: a failed API call yields empty results instead of breaking the whole page.

## DeltaService — the "Shift" math

```java
public Optional<DeltaView> deltaFor(Long trackedCityId) {
    Optional<WeatherSnapshot> latestOpt = weatherSnapshotRepository
            .findFirstByTrackedCityIdOrderByRecordedAtDesc(trackedCityId);
    if (latestOpt.isEmpty()) {
        return Optional.empty();
    }

    WeatherSnapshot latest = latestOpt.get();
    Optional<WeatherSnapshot> previousOpt = weatherSnapshotRepository
            .findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(
                    trackedCityId, latest.getRecordedAt());

    BigDecimal tempDelta = null;
    OffsetDateTime since = latest.getRecordedAt();

    if (previousOpt.isPresent()) {
        WeatherSnapshot previous = previousOpt.get();
        tempDelta = subtract(latest.getTemperature(), previous.getTemperature());
        // ... wind, pressure, humidity deltas ...
        since = previous.getRecordedAt();
    }

    return Optional.of(new DeltaView(tempDelta, ..., since, latest.getTemperature(), latest.getWindSpeed()));
}
```

Data flow: **database** (two snapshots) → **DeltaService** → `DeltaView` → view DTO → template badge (`+0.00 C` green up / `-4.47 C` red down).

## CorridorService — travel corridor waypoints

```java
public CorridorBundle generate(
        String originName, BigDecimal originLat, BigDecimal originLon,
        String destinationName, BigDecimal destinationLat, BigDecimal destinationLon) {
    List<CorridorPoint> points = new ArrayList<>();

    for (int i = 0; i <= DEFAULT_WAYPOINTS; i++) {
        double fraction = (double) i / DEFAULT_WAYPOINTS;
        BigDecimal lat = originLat.add(destinationLat.subtract(originLat).multiply(BigDecimal.valueOf(fraction)));
        BigDecimal lon = originLon.add(destinationLon.subtract(originLon).multiply(BigDecimal.valueOf(fraction)));

        String label = i == 0 ? originName : i == DEFAULT_WAYPOINTS ? destinationName : "Stop " + i;
        points.add(resolveWeather(label, lat, lon));
    }

    return new CorridorBundle(originName, destinationName, points);
}
```

`DEFAULT_WAYPOINTS = 5` produces 6 points (0%, 20%, ..., 100%). Each waypoint fetches current weather.

## GreetingService — time-of-day greeting + theme

```java
public String greeting(AppUser user, LocalTime now) {
    return "Hi comrade " + user.getFirstName() + ", " + partOfDay(now);
}

public String partOfDay(LocalTime now) {
    if (now.isAfter(LocalTime.of(21, 0)) || now.isBefore(LocalTime.of(5, 0))) return "Good night";
    if (now.isBefore(LocalTime.of(12, 0))) return "Good morning";
    if (now.isBefore(LocalTime.of(17, 0))) return "Good afternoon";
    return "Good evening";
}

public String themeClass(LocalTime now) {
    if (now.isAfter(LocalTime.of(21, 0)) || now.isBefore(LocalTime.of(5, 0))) return "theme-night";
    if (now.isBefore(LocalTime.of(12, 0))) return "theme-morning";
    if (now.isBefore(LocalTime.of(17, 0))) return "theme-afternoon";
    return "theme-evening";
}
```

- `greeting` powers the personalized header.
- `themeClass` sets the CSS theme on the `<body>` — this is the single hook the design team's token system needs.

## ContextualMessageService — friendly messages

```java
public String messageFor(String city, Integer conditionCode, BigDecimal temperature, BigDecimal windSpeed) {
    Category category = categorize(conditionCode, temperature, windSpeed);
    return switch (category) {
        case RAIN -> "Rain is heading toward " + city + " - pack an umbrella if you are visiting.";
        case STORM -> "Storm watch active over " + city + " - consider rescheduling your trip.";
        case HEAT -> "It is hot over " + city + " today - stay hydrated if you head out.";
        case WINDY -> "Wind is picking up in " + city + " - light layers recommended.";
        case CLOUDY -> "Clouds are rolling over " + city + ", but no rain expected - a good day to visit.";
        case CLEAR -> "Skies over " + city + " are calm and dry - safe to visit when you are ready.";
        case UNKNOWN -> "When are you visiting " + city + "? Weather updates are on the way.";
    };
}
```

`categorize` maps OpenWeatherMap condition codes (2xx=storm, 3xx/5xx/6xx=rain, 800=clear, 8xx=clouds, temperature ≥ 32 → heat, wind ≥ 12 → windy).

## SignupService — registration

```java
@Transactional
public void createUser(SignupRequest request) {
    String normalizedEmail = request.getEmail().trim().toLowerCase();
    if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
        throw new EmailAlreadyRegisteredException();
    }

    AppUser user = new AppUser();
    user.setFirstName(request.getFirstName().trim());
    user.setLastName(request.getLastName().trim());
    user.setEmail(normalizedEmail);
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole("ROLE_USER");
    appUserRepository.save(user);
}

public static class EmailAlreadyRegisteredException extends RuntimeException {
}
```

- Normalizes email before saving (trim + lowercase).
- Rejects duplicates with a dedicated exception caught by `AuthController`.
- Uses `PasswordEncoder` (BCrypt) — never stores plain text.

## Design principle: services are stateless

Services hold no mutable state between calls — only injected dependencies (`private final` fields). This makes them safe to share across requests and trivial to unit test with mocks.

## Test your understanding

**Q1. Why is `addCity` `@Transactional` even though it only calls `save`?**
**A.** Writing changes and the internal reads (`existsBy`, `countBy`) should be consistent and atomic. `@Transactional` also guarantees lazy relations (like `city.user`) resolve and changes flush cleanly.

**Q2. In `removeCity`, what happens if the removed city is the primary?**
**A.** The service re-queries the user's remaining cities ordered by sort order and promotes the first one to primary, so the user always has a primary city when any remain.

**Q3. Why does `DashboardService.forecastFor` catch `RuntimeException` and return empty lists?**
**A.** The OpenWeatherMap call is an external dependency that can fail (network, quota, bad key). The dashboard must still render; an empty forecast is better than a 500 error.

**Q4. What is the purpose of `enrich` in `DashboardService`?**
**A.** It composes a `TrackedCityView` from the city + live weather, then attaches the delta (from `DeltaService`) so the template and REST clients get one complete object instead of three separate lookups.

**Q5. How does `themeClass` relate to the design system?**
**A.** `GreetingService.themeClass(now)` returns `theme-morning/-afternoon/-evening/-night`, which the templates put on `<body>`. `tokens.css` swaps all color variables based on that class — the backend picks the theme, the CSS applies it.

**Q6. Why does `SignupService` normalize the email?**
**A.** So the same user cannot register twice with different casing (`User@x.com` vs `user@x.com`) and so login lookup is predictable. `findByEmailIgnoreCase` + normalization keeps both sides consistent.
