# 03 — Service Tests

This page covers the five Mockito-based service test classes: Greeting, Delta, WeatherAlert, Snapshot, and Corridor. Together they exercise the core business logic.

## GreetingServiceTest — time boundaries (5 tests)

Tests that the greeting matches the time of day. A user with first name "John":

```java
@Test
void morningGreeting() {
    assertThat(greetingService.greeting(user(), LocalTime.of(9, 0)))
            .isEqualTo("Hi comrade John, Good morning");
}

@Test
void afternoonGreeting() {
    assertThat(greetingService.greeting(user(), LocalTime.of(14, 0)))
            .isEqualTo("Hi comrade John, Good afternoon");
}

@Test
void eveningGreeting() {
    assertThat(greetingService.greeting(user(), LocalTime.of(19, 0)))
            .isEqualTo("Hi comrade John, Good evening");
}

@Test
void nightGreeting() {
    assertThat(greetingService.greeting(user(), LocalTime.of(22, 0)))
            .isEqualTo("Hi comrade John, Good night");
}

@Test
void earlyMorningNightGreeting() {
    assertThat(greetingService.greeting(user(), LocalTime.of(3, 0)))
            .isEqualTo("Hi comrade John, Good night");
}
```

**What it proves:** the time→greeting mapping (05–11 morning, 12–16 afternoon, 17–20 evening, 21–04 night), including the wrap-around night case (3 AM).

## DeltaServiceTest — the "shift" math (3 tests)

Uses `@Mock` repository + `@InjectMocks` service.

**Test 1 — computes deltas between latest and previous:**
```java
WeatherSnapshot latest = snapshot(new BigDecimal("24.0"), new BigDecimal("5.0"), 1012, 68);
WeatherSnapshot previous = snapshot(new BigDecimal("28.0"), new BigDecimal("2.0"), 1020, 60);
previous.setRecordedAt(latest.getRecordedAt().minusHours(3));

when(weatherSnapshotRepository.findFirstByTrackedCityIdOrderByRecordedAtDesc(1L))
        .thenReturn(Optional.of(latest));
when(weatherSnapshotRepository.findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(1L, latest.getRecordedAt()))
        .thenReturn(Optional.of(previous));

Optional<DeltaView> delta = deltaService.deltaFor(1L);

assertThat(delta.get().temperatureDelta()).isEqualByComparingTo("-4.0");  // 24 - 28
assertThat(delta.get().windSpeedDelta()).isEqualByComparingTo("3.0");     // 5 - 2
assertThat(delta.get().pressureDelta()).isEqualTo(-8);                    // 1012 - 1020
assertThat(delta.get().humidityDelta()).isEqualTo(8);                     // 68 - 60
```

**Test 2 — empty when no snapshots:** `findFirst...Desc` returns `Optional.empty()` → `deltaFor` returns empty.

**Test 3 — null deltas when no previous:** latest exists but no earlier snapshot → `temperatureDelta` is `null`, `currentTemperature` is present.

## WeatherAlertServiceTest — anomaly thresholds (3 tests)

Tests that alerts fire (or not) based on the pressure/wind thresholds.

**Test 1 — pressure drop alert:**
```java
WeatherSnapshot latest = snapshot(1010, BigDecimal.valueOf(4));   // pressure 1010
WeatherSnapshot previous = snapshot(1020, BigDecimal.valueOf(4)); // pressure 1020
when(weatherSnapshotRepository.findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(isNull(), any()))
        .thenReturn(Optional.of(previous));

weatherAlertService.evaluate(city, latest);

ArgumentCaptor<WeatherAlert> captor = ArgumentCaptor.forClass(WeatherAlert.class);
verify(weatherAlertRepository).save(captor.capture());
assertThat(captor.getValue().getAlertType()).isEqualTo("PRESSURE_DROP");
assertThat(captor.getValue().getSeverity()).isEqualTo("HIGH");
```
Drop = 1020 − 1010 = 10 ≥ 5 → alert.

**Test 2 — no alert when stable:** pressure 1020 → 1018 (drop 2 < 5) → `verify(repo, never()).save(...)`.

**Test 3 — no alert on first snapshot:** no previous → returns early → `never().save`.

Note: `isNull()` matches the city id argument because the test city has no id — the mock matches by matcher rather than a literal value.

## SnapshotServiceTest — weather → snapshot mapping (1 test)

Verifies `ingestCity` maps a `CurrentWeatherResponse` into a saved `WeatherSnapshot`:

```java
when(weatherService.currentWeather(city.getLatitude(), city.getLongitude())).thenReturn(weather);
when(weatherSnapshotRepository.save(any(WeatherSnapshot.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));   // return the saved arg

snapshotService.ingestCity(city);

ArgumentCaptor<WeatherSnapshot> captor = ArgumentCaptor.forClass(WeatherSnapshot.class);
verify(weatherSnapshotRepository).save(captor.capture());
WeatherSnapshot snapshot = captor.getValue();

assertThat(snapshot.getTemperature()).isEqualByComparingTo("24.57");
assertThat(snapshot.getHumidity()).isEqualTo(85);
assertThat(snapshot.getPressure()).isEqualTo(1012);
assertThat(snapshot.getConditionCode()).isEqualTo(804);
assertThat(snapshot.getConditionText()).isEqualTo("overcast clouds");
assertThat(snapshot.getSunrise()).isNotNull();
```

Key technique: `thenAnswer(invocation -> invocation.getArgument(0))` makes `save` return its own argument, so the code can continue with the "saved" object.

## CorridorServiceTest — waypoint generation (2 tests)

**Test 1 — six waypoints with weather:**
```java
when(weatherService.currentWeather(any(BigDecimal.class), any(BigDecimal.class)))
        .thenReturn(weather);

CorridorBundle bundle = corridorService.generate(
        "Lagos", new BigDecimal("6.5244"), new BigDecimal("3.3792"),
        "London", new BigDecimal("51.5072"), new BigDecimal("-0.1276"));

assertThat(bundle.points()).hasSize(6);
assertThat(bundle.points().get(0).label()).isEqualTo("Lagos");
assertThat(bundle.points().get(5).label()).isEqualTo("London");
assertThat(bundle.points().get(0).temperature()).isEqualByComparingTo("25.0");
```

**Test 2 — graceful degradation on API failure:**
```java
when(weatherService.currentWeather(any(BigDecimal.class), any(BigDecimal.class)))
        .thenThrow(new RuntimeException("api down"));

CorridorBundle bundle = corridorService.generate("A", ..., "B", ...);

assertThat(bundle.points()).hasSize(6);       // still 6 points
assertThat(bundle.points().get(0).temperature()).isNull();  // weather omitted, no crash
```

## Test your understanding

**Q1. Why does GreetingServiceTest need no mocks at all?**
**A.** `GreetingService` has no dependencies — it is pure logic on inputs (`AppUser`, `LocalTime`), so a plain `new GreetingService()` is enough.

**Q2. Why does `WeatherAlertServiceTest` use `isNull()` for the first matcher?**
**A.** The test city has no `id` set, so `city.getId()` is `null`. `isNull()` matches that, while `any()` matches the timestamp. Literal `1L` would not match `null`.

**Q3. What does `thenAnswer(invocation -> invocation.getArgument(0))` do in SnapshotServiceTest?**
**A.** It makes the mocked `save(...)` return the exact object that was passed in — simulating a successful JPA save so the service can continue and the test can capture the object.

**Q4. What is the delta computed when previous is 28°C and latest is 24°C?**
**A.** `24 − 28 = -4.0` — `temperatureDelta` is `"-4.0"`, which the frontend shows as a red `down` badge.

**Q5. CorridorServiceTest's failure test — why is 6 points still expected when the API fails?**
**A.** `resolveWeather` catches the exception and returns a `CorridorPoint` with null weather. The corridor shape (6 waypoints) is preserved even if weather data is missing — exactly the graceful degradation designed into the service.
