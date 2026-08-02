# 04 — Manual Test Guide

The automated tests cover core logic, but the full user experience must be verified manually. This guide walks through every major flow and provides curl scripts to test the backend end-to-end.

## Prerequisites

```bash
# From the repo root — start the stack
docker compose up --build
```

The app runs at http://localhost:8080. The first launch applies Flyway migrations and starts the snapshot scheduler (initial delay 60s).

## Flow 1 — Signup and onboarding

**Expected behavior:**
1. `GET /signup` shows the glass signup form (first name, last name, email, password).
2. Submitting creates the account, auto-logs-in, and redirects to `/onboarding`.
3. Search a city, select it → the city becomes the primary tracked city.
4. Dashboard loads with the greeting + city weather.

**Automated check (curl):**
```bash
# 1. Get a CSRF token
curl -s -c /tmp/cj.txt http://localhost:8080/signup > /tmp/signup.html
CSRF=$(grep -o 'name="_csrf" value="[^"]*"' /tmp/signup.html | cut -d'"' -f4)

# 2. Sign up
curl -s -b /tmp/cj.txt -c /tmp/cj.txt -o /dev/null -w "%{http_code} -> %{redirect_url}\n" \
  -X POST http://localhost:8080/signup \
  -d "_csrf=$CSRF&firstName=Test&lastName=User&email=test@example.com&password=password123"
# Expect: 302 -> http://localhost:8080/onboarding

# 3. Onboarding search
curl -s -b /tmp/cj.txt http://localhost:8080/onboarding > /tmp/onb.html
CSRF=$(grep -o 'name="_csrf" value="[^"]*"' /tmp/onb.html | cut -d'"' -f4)
curl -s -b /tmp/cj.txt -X POST http://localhost:8080/onboarding/search \
  -d "_csrf=$CSRF&query=Lagos" | grep -o 'name="cityName" value="Lagos"'
# Expect: a matching result row

# 4. Select the city
curl -s -b /tmp/cj.txt -c /tmp/cj.txt -o /dev/null -w "%{http_code} -> %{redirect_url}\n" \
  -X POST http://localhost:8080/onboarding/select \
  -d "_csrf=$CSRF&cityName=Lagos&country=NG&countryCode=NG&latitude=6.5244&longitude=3.3792"
# Expect: 302 -> http://localhost:8080/
```

## Flow 2 — Dashboard and city management

**Expected behavior:**
- Hero shows the primary city's temperature, condition, metrics, and delta badge.
- Carousel lists tracked cities; the primary is badged and `active`.
- "+ Add city" scrolls to the search panel; searching and tracking adds a card.
- Tapping a city card switches the hero without reloading.
- Set primary / Remove buttons work.

**Automated check:**
```bash
# Dashboard renders
curl -s -b /tmp/cj.txt http://localhost:8080/ | grep -oE 'hero-temp|city-card|delta-badge'
# REST list
curl -s -b /tmp/cj.txt http://localhost:8080/api/cities
# REST search
curl -s -b /tmp/cj.txt "http://localhost:8080/api/cities/search?q=Paris"
```

## Flow 3 — Forecasts and history chart

**Expected behavior:**
- Zone 3 shows hourly forecast (horizontal snap scroll) and a 7-day list.
- The history chart draws a line; hovering shows a tooltip; with <2 snapshots it shows "Not enough historical data yet".

**Automated check:**
```bash
curl -s -b /tmp/cj.txt http://localhost:8080/api/weather/1/forecast | head -c 200
curl -s -b /tmp/cj.txt "http://localhost:8080/api/weather/1/snapshots?days=7" | head -c 200
```

## Flow 4 — Delta ("Shift since last check")

**Expected behavior:** the hero delta badge shows a value once at least two snapshots exist; positive = green `up`, negative = red `down`.

**Automated check (simulate a second snapshot via SQL in the db container):**
```bash
docker exec sky-track-db-1 psql -U skytrack_user -d skytrack -c \
  "INSERT INTO weather_snapshots (tracked_city_id, temperature, pressure, wind_speed, recorded_at)
   VALUES (1, 20.10, 1005, 18.00, NOW());"
curl -s -b /tmp/cj.txt http://localhost:8080/api/weather/1/delta
# Expect temperatureDelta ≈ -4.47, pressureDelta = -7, windSpeedDelta ≈ 12.27
```

## Flow 5 — Travel corridor

**Expected behavior:** `/corridor` accepts origin + destination, and renders a timeline of 6 waypoints with icons and temperatures.

**Automated check:**
```bash
curl -s -b /tmp/cj.txt http://localhost:8080/corridor > /tmp/corr.html
CSRF=$(grep -o 'name="_csrf" value="[^"]*"' /tmp/corr.html | cut -d'"' -f4)
curl -s -b /tmp/cj.txt -X POST http://localhost:8080/corridor \
  -d "_csrf=$CSRF&origin=Lagos&destination=London" | grep -c 'corridor-point'
# Expect: 6
```

## Flow 6 — Alerts

**Expected behavior:** `/alerts` lists alerts; unacknowledged ones show an Acknowledge button; acknowledging hides it.

**Automated check (insert a test alert, then acknowledge):**
```bash
docker exec sky-track-db-1 psql -U skytrack_user -d skytrack -c \
  "INSERT INTO weather_alerts (tracked_city_id, alert_type, severity, title, description, detected_at, acknowledged)
   VALUES (1, 'PRESSURE_DROP', 'HIGH', 'Rapid pressure drop', 'Pressure fell 6 hPa for Lagos', NOW(), FALSE);"

curl -s -b /tmp/cj.txt http://localhost:8080/alerts > /tmp/alerts.html
ALERT_ID=$(docker exec sky-track-db-1 psql -U skytrack_user -d skytrack -t -c "SELECT id FROM weather_alerts LIMIT 1;" | tr -d ' ')
CSRF=$(grep -o 'name="_csrf" value="[^"]*"' /tmp/alerts.html | cut -d'"' -f4)
curl -s -b /tmp/cj.txt -c /tmp/cj.txt -o /dev/null -w "%{http_code}\n" \
  -X POST http://localhost:8080/alerts/$ALERT_ID/acknowledge -d "_csrf=$CSRF"
# Expect: 302; re-loading /alerts shows "Acknowledged"
```

## Flow 7 — Security checks

- Unauthenticated access to `/` redirects to `/login` (302 → `/login`).
- Static assets: `/css/**`, `/js/**` are public (200 without login).
- REST endpoints require auth (302 → `/login` without a session).
- A user cannot see another user's cities (each request is scoped to their session).
- Logout invalidates the session: after logout, `/` redirects to `/login`.

## Flow 8 — Snapshot scheduler

- After ~60s the first snapshot for each tracked city appears in `weather_snapshots`.
- It runs every 30 minutes by default (configurable via `SNAPSHOT_FIXED_RATE_MS`).

## Test your understanding

**Q1. Why must every manual POST include a `_csrf` token?**
**A.** Spring Security enables CSRF protection by default for state-changing requests. The token comes from the rendered form and is validated server-side; omitting it returns 403.

**Q2. How do you verify the delta without waiting 30 minutes?**
**A.** Manually insert a second snapshot via SQL (Flow 4), then call `/api/weather/{id}/delta`. The delta computes from whatever snapshots exist.

**Q3. What indicates the scheduler is working?**
**A.** A `weather_snapshots` row appears ~60s after startup for each tracked city, and again every 30 minutes. Check with `SELECT * FROM weather_snapshots;`.

**Q4. Why does the alerts flow need a manual SQL insert?**
**A.** Alerts are created automatically only when a snapshot triggers an anomaly (pressure drop ≥ 5 hPa / wind surge ≥ 12 m/s), which is rare in real weather. Inserting one manually exercises the alerts UI deterministically.

**Q5. What is the correct response when accessing `/` logged out?**
**A.** A `302` redirect to `/login`. After login the user is sent to `/` (via `defaultSuccessUrl`).
