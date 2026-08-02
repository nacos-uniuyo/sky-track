# System Design: Sky Track

## 1. Overview & Vision

Sky Track is an active meteorological intelligence tracker, not a passive weather viewer. It treats weather data as streams of trackable, persistent assets — giving users historical context, predictive tracking, and deep analytical insights over selected locations. Users build a personalized tracking hub of cities and monitor conditions across them with delta comparisons, trend lines, and anomaly alerts.

## 2. Feature Inventory

### Standard Weather Features (Baseline)

Every professional weather app provides these. Sky Track includes all of them as the foundation.

| Feature | Description |
|---------|-------------|
| Current Conditions | Temperature, feels-like, humidity, wind speed/direction, pressure, visibility, UV index |
| Hourly Forecast | 24-hour or 48-hour hour-by-hour breakdown (temp, condition, precipitation chance) |
| 7-Day Daily Forecast | Day-by-day high/low, condition icon, precipitation probability |
| City Search & Autocomplete | Search by city name + country; typeahead suggestions from a city database |
| Geolocation | Detect user location via browser Geolocation API; show weather by coordinates |
| Favorites / Saved Locations | Persist a list of tracked cities per user |
| Sunrise & Sunset | Local sunrise/sunset times for each tracked city |
| Weather Alerts | Basic severe weather warnings from OpenWeatherMap |
| Unit Toggle | Celsius / Fahrenheit, km/h / mph, millibars / inHg |
| Weather Icons & Condition Text | Animated icons mapped to OpenWeatherMap condition codes |

### Core Differentiators (Sky Track Unique)

These make Sky Track different from a generic weather app.

#### 2.1 Weather Delta Tracking (The "Shift" Metric)

Instead of just displaying the current reading, Sky Track calculates how conditions have shifted since the user's last visit for that city.

```
Example: "Temperature dropped 4C and wind speed increased 12 km/h since 2:00 PM"
```

The shift is computed by comparing the latest weather snapshot against the most recent snapshot prior to the current session. A small delta badge (arrow + value) appears on each city card and in the hero section.

#### 2.2 Tracked Route / Travel Weather Corridor

Users input a departure city and a destination city. Sky Track fetches current conditions at multiple waypoints along the route and generates a linear weather-tracking timeline. Ideal for road trips, flights, or multi-city itineraries.

- Backend computes intermediate coordinates between two lat/long pairs.
- Fetches current conditions at each waypoint.
- Frontend renders a horizontal timeline with weather cards at each stop.

#### 2.3 Severe Weather Anomaly Alerts ("Sky Watcher")

Proactive tracker notifications that monitor all tracked cities for sudden barometric pressure drops, rapid wind speed increases, or storm formations. Alerts warn users before conditions become hazardous.

- A background job polls weather snapshots at a configured interval (e.g., every 30 minutes).
- Compares the latest snapshot against a rolling baseline.
- If a threshold is exceeded (e.g., pressure drop > 5 hPa in 3 hours), a notification is generated and stored.
- Future: push notifications or email alerts.

#### 2.4 Historical Weather Playback

A timeline scrubber allowing users to slide back through 7 to 30 days of recorded data for any tracked city. Weather patterns are visualized like a stock chart with temperature, humidity, and wind overlaid.

- Data source: `weather_snapshots` table.
- Frontend renders a time-series chart with a draggable scrubber.
- The hero card updates to show conditions at the scrubbed timestamp.

## 3. Core User Journey & Data Flow

### A. Onboarding & Primary City Selection

1. User registers via `/signup` (first name, last name, email, password).
2. After signup, user is prompted to search for and select their **primary city**.
3. The selected city is persisted as `is_primary = true` in `tracked_cities`.
4. User is routed to the dashboard pre-loaded with live data for that city.

### B. Dynamic Tracking & Favorite Management

1. The **Tracked Hub** (Zone 1) displays full telemetry for the currently active city.
2. The **Favorites Carousel** (Zone 2) shows all user-tracked cities as glass cards.
3. Users can add cities via a search modal triggered by the "+ Add City" button in the carousel.
4. Tapping a city card switches the active city and updates Zone 1 and Zone 3.
5. Long-press or edit mode allows unpinning, reordering via drag-and-drop, or promoting a city to primary.

### C. Deep-Dive Telemetry View

Clicking any tracked city card opens Zone 3 with:
- Delta metrics ("Shift since last check").
- Historical timeline graph with scrubber.
- Sky Watcher alert history for that city.
- Full meteorological table (pressure, visibility, UV, sunrise/sunset).

### D. Personalized Dashboard Experience

Sky Track personalizes the dashboard with a real-time greeting and contextual city messages derived from the user's data and tracked city weather.

#### D1. Time-of-Day Greeting

The dashboard header displays a greeting on the left side using the user's actual first name and current time of day:

```
Hi comrade {firstName}, Good {part}
```

The `{part}` is computed server-side from the current time:

| Time Range | Greeting Part |
|------------|---------------|
| 05:00 - 11:59 | Good morning |
| 12:00 - 16:59 | Good afternoon |
| 17:00 - 20:59 | Good evening |
| 21:00 - 04:59 | Good night |

The first name comes directly from `app_users.first_name` — the actual first name the user entered during signup. The greeting is rendered in the script typeface (`--font-script: Playfair Display`) per `DESIGN.md` typography rules.

#### D2. Contextual Favorite-City Messages

Below the greeting, Sky Track displays a friendly, weather-informed message that references the user's primary or currently active tracked city. The message is selected from a catalog keyed by weather condition and is designed to feel conversational and trip-oriented.

| Condition | Message Template |
|-----------|-----------------|
| Calm / no hazards | "Skies over {City} are calm and dry — safe to visit when you are ready." |
| Rain expected | "Rain is heading toward {City} — pack an umbrella if you are visiting." |
| Storm / severe | "Storm watch active over {City} — consider rescheduling your trip." |
| High heat | "It is hot over {City} today — stay hydrated if you head out." |
| Windy | "Wind is picking up in {City} — light layers recommended." |
| Cloudy | "Clouds are rolling over {City}, but no rain expected — a good day to visit." |
| No data available | "When are you visiting {City}? Weather updates are on the way." |

**Message selection logic:**

1. Retrieve the user's primary tracked city (`is_primary = true`).
2. Fetch the latest `WeatherSnapshot` for that city.
3. Map its `condition_code` or `condition_text` to the appropriate category.
4. Inject the city name into the template and deliver with the dashboard model.
5. If no snapshot exists yet, the fallback message is used.

When the user taps a different city in the Favorites Carousel, the greeting stays (tied to the user, not the city) but the contextual message updates to reflect the newly active city's latest conditions.

#### D3. Backend Placement

| Component | Module | Responsibility |
|-----------|--------|---------------|
| `GreetingService` | `sky-track-core` | Maps `LocalTime` → greeting part; builds full greeting string from `AppUser.firstName` |
| `ContextualMessageService` | `sky-track-core` | Holds the condition → message catalog; selects and formats the city message |
| `HomeController` | `sky-track-web` | Resolves authenticated user → loads primary city + latest snapshot → passes greeting and message to the template |
| `index.html` (Header) | `sky-track-web` | Renders greeting (left-aligned, script font) and city message (below greeting) following `DESIGN.md` glassmorphism and token system |

## 4. Database Schema (Target)

Evolving from the current `app_users` + `saved_locations` schema. Current V1/V2 migrations remain applied; additions come as new Flyway migrations (V3+).

### 4.1 `app_users` (Target — evolves from V2)

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | Auto-increment |
| first_name | VARCHAR(60) NOT NULL | User's real first name (greeting source) |
| last_name | VARCHAR(60) NOT NULL | User's last name |
| email | VARCHAR(180) UNIQUE NOT NULL | Login identifier |
| password_hash | VARCHAR(255) NOT NULL | BCrypt |
| role | VARCHAR(40) DEFAULT 'ROLE_USER' | Spring Security |
| enabled | BOOLEAN DEFAULT TRUE | Account status |
| created_at | TIMESTAMPTZ DEFAULT NOW() | |

V2 originally created `full_name VARCHAR(120)`. A later migration (V9) adds `first_name` and `last_name`, backfills them from `full_name`, then drops `full_name`.

### 4.2 `tracked_cities` (New — replaces `saved_locations`)

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| user_id | BIGINT NOT NULL FK → app_users.id | Scoped per user |
| city_name | VARCHAR(120) NOT NULL | |
| country | VARCHAR(120) | |
| country_code | VARCHAR(2) | ISO 3166-1 alpha-2 |
| latitude | DECIMAL(9,6) | |
| longitude | DECIMAL(9,6) | |
| is_primary | BOOLEAN DEFAULT FALSE | Only one per user |
| created_at | TIMESTAMPTZ DEFAULT NOW() | |

Index: `idx_tracked_cities_user` on (user_id).

### 4.3 `weather_snapshots` (New)

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| tracked_city_id | BIGINT NOT NULL FK → tracked_cities.id | |
| temperature | DECIMAL(5,2) | Celsius |
| feels_like | DECIMAL(5,2) | |
| humidity | INTEGER | Percent 0-100 |
| wind_speed | DECIMAL(5,2) | m/s |
| wind_direction | INTEGER | Degrees 0-360 |
| pressure | INTEGER | hPa |
| visibility | INTEGER | Meters |
| condition_code | INTEGER | OpenWeatherMap code |
| condition_text | VARCHAR(120) | e.g. "scattered clouds" |
| sunrise | TIMESTAMPTZ | |
| sunset | TIMESTAMPTZ | |
| recorded_at | TIMESTAMPTZ NOT NULL | Time of observation |

Index: `idx_snapshots_city_time` on (tracked_city_id, recorded_at).

### 4.4 `weather_alerts` (New — for Sky Watcher)

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| tracked_city_id | BIGINT NOT NULL FK → tracked_cities.id | |
| alert_type | VARCHAR(50) NOT NULL | PRESSURE_DROP, WIND_SURGE, STORM_FORMATION |
| severity | VARCHAR(20) | LOW, MEDIUM, HIGH, CRITICAL |
| title | VARCHAR(255) | Human-readable |
| description | TEXT | |
| detected_at | TIMESTAMPTZ NOT NULL | |
| acknowledged | BOOLEAN DEFAULT FALSE | |

### 4.5 `corridor_routes` (New — for Travel Weather Corridor)

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| user_id | BIGINT NOT NULL FK → app_users.id | |
| origin_city_id | BIGINT FK → tracked_cities.id | |
| destination_city_id | BIGINT FK → tracked_cities.id | |
| waypoint_count | INTEGER | |
| created_at | TIMESTAMPTZ DEFAULT NOW() | |

### 4.6 Migration Path

| Step | Flyway File | Action |
|------|-------------|--------|
| Current | V1, V2 | Applied. `app_users` and `saved_locations` exist. |
| V3 | `V3__create_tracked_cities.sql` | Create `tracked_cities` with user_id FK, lat/long, is_primary |
| V4 | `V4__create_weather_snapshots.sql` | Create `weather_snapshots` |
| V5 | `V5__create_weather_alerts.sql` | Create `weather_alerts` |
| V6 | `V6__create_corridor_routes.sql` | Create `corridor_routes` |
| V7 | `V7__migrate_saved_to_tracked.sql` | Optional — migrate existing `saved_locations` → `tracked_cities` with a default user |
| V8 | `V8__drop_saved_locations.sql` | Drop `saved_locations` after migration |
| V9 | `V9__alter_app_users_first_last_name.sql` | Add `first_name` + `last_name`, backfill from `full_name`, drop `full_name` |

## 5. Dashboard Structure & Layout Hierarchy

```
+-----------------------------------------------------------------+
| [Top Bar]                                                       |
|  - Greeting (left): "Hi comrade {first}, Good {part}"           |
|    (script typeface, left-aligned)                              |
|  - Live Clock | Notification Bell | Profile (right)             |
+-----------------------------------------------------------------+
|                                                             |
| [Zone 1: Primary Tracked Hub (Hero Section)]                |
|  - Contextual city message: "Skies over {City} are calm..."  |
|  - Full meteorological telemetry for active city            |
|  - Liquid background animation (theme per time-of-day)      |
|  - Delta badge: "Shift since last check"                    |
|  - Large temperature, condition icon, feels-like, high/low  |
|                                                             |
+-------------------------------------------------------------+
| [Zone 2: Favorite Cities Quick-Access Carousel]             |
|  - Horizontal scrollable glass-card row                     |
|  - Each card: city name, country badge, icon, temp, delta   |
|  - Primary city pinned first (badged "Primary")             |
|  - Last card: "+ Add City" button                           |
|  - Long-press: reorder, unpin, set primary                  |
+-------------------------------------------------------------+
| [Zone 3: Traceable Analytics & Trend Scrubber]              |
|  - Weather Delta metrics detail                             |
|  - Historical timeline graph (7-30 day) with scrubber       |
|  - Sky Watcher alert history for active city                |
|  - Full meteorological data table                           |
+-------------------------------------------------------------+
```

## 6. Frontend Component State Flow

```
[User taps Favorite City Card]
       |
       v
[Set activeCityId state]
       |
       +---> [Load cached snapshot for cityId]
       +---> [Update liquid background theme (Morning/Afternoon/Evening/Night)]
       +---> [Re-render Zone 1: Hero telemetry with delta]
       +---> [Re-render Zone 3: Analytics, history chart, alerts]
```

A city object holds:

```json
{
  "id": 1,
  "cityName": "Lagos",
  "country": "Nigeria",
  "countryCode": "NG",
  "latitude": 6.5244,
  "longitude": 3.3792,
  "isPrimary": true,
  "currentWeather": { ... },
  "delta": { "temperature": -1.5, "windSpeed": 3.2, "since": "2026-08-02T14:00:00Z" }
}
```

## 7. Backend Architecture

### 7.1 Module Mapping

| Module | Responsibility |
|--------|---------------|
| `sky-track-core` | `model/` — AppUser, TrackedCity, WeatherSnapshot, WeatherAlert, CorridorRoute |
| | `repository/` — JPA repositories for all entities |
| | `service/` — WeatherService (OpenWeatherMap), SnapshotService (ingestion), DeltaService (shift calc), SkyWatcherService (anomaly detection), CorridorService (route waypoints), GreetingService (time-of-day greeting), ContextualMessageService (condition-based messages), SignupService, AppUserDetailsService |
| | `dto/` — request/response objects |
| `sky-track-web` | `controller/` — rest + mvc controllers for cities, weather, alerts, corridor, auth |
| | `config/` — security, weather API config properties, scheduling |
| | `templates/` — Thymeleaf server-rendered pages |
| | `static/` — CSS, JS, assets |

### 7.2 Weather API Consumption

OpenWeatherMap is the primary data source. Configured in `application.yml`:

```yaml
weather:
  api:
    base-url: https://api.openweathermap.org/data/2.5
    key: ${OPENWEATHER_API_KEY:}
```

Endpoints consumed:

| OpenWeatherMap Endpoint | Maps To |
|--------------------------|---------|
| `/weather` | Current conditions for a city/by coordinates |
| `/forecast` | 5-day / 3-hour forecast |
| `/onecall` | Current + hourly + daily + alerts in one call |
| `/geo/1.0/direct` | City search / geocoding |
| `/geo/1.0/reverse` | Reverse geocoding (coordinates → city) |

### 7.3 Snapshot Ingestion

A scheduled task (`@Scheduled`) runs at a configured fixed rate (e.g., every 30 minutes). For each tracked city:

1. Call OpenWeatherMap `/weather` by lat/long.
2. Map response to a `WeatherSnapshot` entity.
3. Persist to `weather_snapshots`.
4. Run Sky Watcher anomaly check comparing latest against prior snapshots.
5. If anomaly detected, create a `WeatherAlert`.

### 7.4 Delta Calculation

When a user loads the dashboard for a city:

1. Query the latest `WeatherSnapshot` (from scheduled ingestion).
2. Query the most recent snapshot from **before** the current user session start.
3. Compute deltas: `latest.temperature - previous.temperature`, etc.
4. Return both the current weather and the shift metric in the city DTO.

## 8. API Endpoint Map

### 8.1 Authentication (Existing)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/login` | Login form |
| POST | `/login` | Authenticate (Spring Security form login) |
| GET | `/signup` | Registration form |
| POST | `/signup` | Create account |
| POST | `/logout` | Logout |

### 8.2 Dashboard & Pages

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Dashboard home (Zone 1 + Zone 2 + Zone 3 for primary city) |
| GET | `/cities/{id}` | Deep-dive view for a specific tracked city |

### 8.3 Tracked Cities (REST)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/cities` | List all tracked cities for authenticated user (with current weather + delta) |
| GET | `/api/cities/search?q={query}` | Search cities via OpenWeatherMap geocoding |
| POST | `/api/cities` | Add a city to the user's tracking list |
| PATCH | `/api/cities/{id}` | Update city (set/clear primary, rename) |
| DELETE | `/api/cities/{id}` | Remove a city from tracking |
| PUT | `/api/cities/reorder` | Reorder favorite cities (accepts ordered ID list) |

### 8.4 Weather Data

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/weather/{cityId}` | Current conditions for a tracked city |
| GET | `/api/weather/{cityId}/forecast` | Hourly + 7-day forecast |
| GET | `/api/weather/{cityId}/snapshots?from=&to=` | Historical snapshots for playback chart |
| GET | `/api/weather/{cityId}/delta` | Shift metric since last user session |

### 8.5 Sky Watcher

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/alerts` | All alerts for user's tracked cities |
| GET | `/api/alerts/{cityId}` | Alerts for a specific city |
| PATCH | `/api/alerts/{id}/acknowledge` | Mark an alert as acknowledged |

### 8.6 Travel Corridor

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/corridor` | Generate a weather timeline between origin and destination |
| GET | `/api/corridor/{id}` | Retrieve a previously generated corridor |

## 9. Design Compliance

All frontend work (Thymeleaf templates, CSS, JavaScript, theme switching, animations) must follow `DESIGN.md`. The glassmorphism spec, token palette, typography, and component patterns defined there are non-negotiable. This document defines what to build; `DESIGN.md` defines how it looks and feels.
