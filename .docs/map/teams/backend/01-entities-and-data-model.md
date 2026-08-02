# 01 — Entities and Data Model

The data model is the foundation of the whole app. It lives in `sky-track-core/src/main/java/com/skywatch/weather/model/`. There are five JPA entities, one per database table.

## The five entities

| Entity | Table | One-line description |
|--------|-------|----------------------|
| `AppUser` | `app_users` | A registered user account |
| `TrackedCity` | `tracked_cities` | A city a user chooses to track |
| `WeatherSnapshot` | `weather_snapshots` | One recorded weather reading |
| `WeatherAlert` | `weather_alerts` | A detected anomaly |
| `CorridorRoute` | `corridor_routes` | A saved travel corridor |

## Relationships

```
AppUser 1 ──── n TrackedCity
TrackedCity 1 ──── n WeatherSnapshot
TrackedCity 1 ──── n WeatherAlert
AppUser 1 ──── n CorridorRoute (origin/destination → TrackedCity)
```

## AppUser — the user account

```java
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 40)
    private String role = "ROLE_USER";

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
```

Key points:
- **`first_name` / `last_name`** — split from the original single `full_name` by migration V8. `firstName` feeds the greeting *"Hi comrade {firstName}, Good {part}"*.
- **`email` is unique** — it is also the Spring Security username.
- **`password_hash`** — stores a BCrypt hash, never the plain password.
- **`created_at`** is `insertable = false, updatable = false`: the database sets it via `DEFAULT NOW()`, JPA never writes it.
- Getters/setters follow JavaBean conventions; there is no `setId` (id is generated).

## TrackedCity — a user's tracked city

```java
@Entity
@Table(name = "tracked_cities")
public class TrackedCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "city_name", nullable = false, length = 120)
    private String cityName;

    @Column(length = 120)
    private String country;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryCity;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
```

Key points:
- **`@ManyToOne(fetch = FetchType.LAZY)` user** — owns the relationship to `AppUser`. Lazy means the `AppUser` is a proxy until accessed inside a transaction.
- **`latitude` / `longitude`** (DECIMAL(9,6)) are the coordinates sent to OpenWeatherMap.
- **`is_primary`** — exactly one primary city per user, chosen during onboarding. The name `primaryCity` maps to column `is_primary`.
- **`sort_order`** — controls carousel order (added in V9).

## WeatherSnapshot — the recorded reading

```java
@Entity
@Table(name = "weather_snapshots")
public class WeatherSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracked_city_id", nullable = false)
    private TrackedCity trackedCity;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "feels_like", precision = 5, scale = 2)
    private BigDecimal feelsLike;

    @Column
    private Integer humidity;

    @Column(name = "wind_speed", precision = 5, scale = 2)
    private BigDecimal windSpeed;

    @Column(name = "wind_direction")
    private Integer windDirection;

    @Column
    private Integer pressure;

    @Column
    private Integer visibility;

    @Column(name = "condition_code")
    private Integer conditionCode;

    @Column(name = "condition_text", length = 120)
    private String conditionText;

    @Column
    private OffsetDateTime sunrise;

    @Column
    private OffsetDateTime sunset;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;
}
```

Key points:
- **`recorded_at`** is the timestamp that drives ordering for delta and history. It is set by `SnapshotService` (not a DB default).
- **`condition_code`** is the OpenWeatherMap weather id (e.g. 800 = clear, 804 = overcast). The frontend maps it to an SVG icon.
- Snapshots accumulate over time — that is what makes "tracking" possible.

## WeatherAlert — an anomaly

```java
@Entity
@Table(name = "weather_alerts")
public class WeatherAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracked_city_id", nullable = false)
    private TrackedCity trackedCity;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "detected_at", nullable = false)
    private OffsetDateTime detectedAt;

    @Column(nullable = false)
    private boolean acknowledged;
}
```

Key points:
- `alertType` values: `PRESSURE_DROP`, `WIND_SURGE`.
- `severity` values: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- `acknowledged` lets a user dismiss an alert.

## CorridorRoute — a travel corridor

```java
@Entity
@Table(name = "corridor_routes")
public class CorridorRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_city_id")
    private TrackedCity originCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_city_id")
    private TrackedCity destinationCity;

    @Column(name = "waypoint_count", nullable = false)
    private int waypointCount;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
```

Key points: it is **owned by a user** and references two tracked cities. The current UI generates corridors on the fly (via `CorridorService`), so this entity is available for persistence but not strictly required for display.

## How entities map to the database

- Table names come from `@Table(name = "...")`.
- Column names default to the field name unless `@Column(name = "...")` says otherwise (e.g. `primaryCity` → `is_primary`).
- Hibernate runs `ddl-auto: validate` — it verifies the entity matches the table and fails startup if they disagree. **Never edit applied migrations**; add new ones.

## Test your understanding

**Q1. Why is `created_at` marked `insertable = false, updatable = false`?**
**A.** The database sets it with `DEFAULT NOW()`. Marking it read-only prevents JPA from writing null or stale values, keeping timestamps consistent.

**Q2. Why is `TrackedCity.user` a lazy `@ManyToOne` and not `EAGER`?**
**A.** Eager loading fetches the user every time a city is read, which is wasteful. Lazy keeps it a proxy; but because `open-in-view: false`, code must access it inside a transaction or it throws `LazyInitializationException`. That is why services are `@Transactional` and ownership is checked via repository queries instead of navigating `alert.getTrackedCity().getUser()`.

**Q3. What is the difference between `is_primary` and `sort_order`?**
**A.** `is_primary` is a boolean marking the single city shown as the hero; `sort_order` is an integer ordering the favorites carousel.

**Q4. What would break if a developer renamed the Java field `primaryCity` but not the column `is_primary`?**
**A.** Validation would fail at startup because Hibernate expects a column named after the field (`primary_city` default naming) unless `@Column(name = "is_primary")` is present. The `@Column` annotation keeps them decoupled.

**Q5. Why are latitude/longitude `DECIMAL(9,6)` rather than `double`?**
**A.** DECIMAL gives exact, deterministic storage for coordinates with up to 6 decimal places (~0.1 m precision) and avoids floating-point drift. It is stored as `BigDecimal` in Java.
