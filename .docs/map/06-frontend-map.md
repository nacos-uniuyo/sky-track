# 06 — Frontend File Map

Every template, fragment, CSS file, and JavaScript file in the frontend, with its purpose and how it connects to the backend. Paths are relative to `sky-track-web/src/main/resources/`.

The frontend is **server-rendered with Thymeleaf** and enhanced with vanilla JavaScript. Pages are HTML skeletons; Thymeleaf fills them with data from controllers. JavaScript adds the liquid background, live clock, weather icons, city switching, and the history chart.

---

## Templates (`templates/`)

### `index.html` — the dashboard (main page)
- **Rendered by:** `HomeController.index` (`GET /`).
- **Data consumed:** `greeting`, `themeClass`, `cities`, `primaryCityView`, `cityMessage`, `forecast`, `history`, `searchResults`, `searchQuery`, `alerts`.
- **Structure (three zones from SYSTEM-DESIGN):**
  - Top bar (fragment) with greeting + live clock + Alerts link + logout.
  - **Zone 1 — hero:** city message, animated weather icon, city name, large temperature, condition, delta badge, high/low/feels-like, metric grid (humidity/wind/pressure).
  - **Zone 2 — carousel:** one `city-card` per tracked city with `th:data-*` attributes (used by `dashboard.js` for switching), plus an "+ Add city" card.
  - **Zone 3 — forecasts:** hourly snap-scroll, 7-day list, and the history chart canvas.
  - **Search panel:** search form + results to add a city.
- **Connections:** includes `head` + `topbar` fragments; loads `liquid.js`, `weather-icons.js`, `dashboard.js`, `history-chart.js`.

### `onboarding.html` — primary city selection after signup
- **Rendered by:** `OnboardingController` (`GET /onboarding`, `POST /onboarding/search`).
- **Data:** `greeting`, `themeClass`, `results`, `query`.
- **Connections:** search posts to `/onboarding/search`; selecting a result posts to `/onboarding/select`.

### `corridor.html` — travel weather corridor
- **Rendered by:** `CorridorController` (`GET/POST /corridor`).
- **Data:** `greeting`, `themeClass`, `corridor`, `originQuery`, `destinationQuery`.
- **Connections:** form posts origin + destination; result is a horizontal `corridor-point` timeline. Loads `weather-icons.js`.

### `alerts.html` — the alerts feed
- **Rendered by:** `AlertsController` (`GET /alerts`).
- **Data:** `greeting`, `themeClass`, `alerts`.
- **Connections:** each alert shows severity badge, type, title, description, time; unacknowledged alerts have an acknowledge button posting to `/alerts/{id}/acknowledge`.

### `auth/login.html`
- **Rendered by:** `AuthController.login`.
- **Data:** `themeClass`.
- **Connections:** posts to `/login` (Spring Security form login, username = email).

### `auth/signup.html`
- **Rendered by:** `AuthController.signup`.
- **Data:** `signupRequest`, `themeClass`.
- **Connections:** bound form (`th:object`, `th:field`) posting to `/signup`; on success the backend auto-logs-in and redirects to `/onboarding`.

---

## Fragments (`templates/fragments/`)

### `head.html`
- **Fragment:** `head(title)`.
- **What:** Common `<head>` for every page: viewport, page title, Google Fonts (Inter + Playfair Display), `tokens.css`, `app.css`, and the `liquid.js` script.
- **Connections:** `th:replace="~{fragments/head :: head('Title')}"` used by all templates.

### `topbar.html`
- **Fragment:** `topbar(greeting)`.
- **What:** The glass top bar: script-font greeting on the left, live clock, Alerts bell link, logout button on the right. Loads `clock.js`.
- **Connections:** used by `index.html`, `corridor.html`, `alerts.html`.

---

## CSS (`static/css/`)

### `tokens.css`
- **What:** The design system tokens (from DESIGN.md). Defines `:root` defaults and four theme overrides:
  - `.theme-morning`, `.theme-afternoon`, `.theme-evening`, `.theme-night`.
- **Tokens:** `--color-primary`, `--color-secondary`, `--color-accent`, `--color-surface`, `--color-text-primary/secondary`, `--glass-blur`, `--glass-radius`, `--font-metrics`, `--font-script`, plus per-theme `--color-background-start/end`.
- **Connections:** imported first by `head.html`; every component style references these variables.

### `app.css`
- **What:** The actual visual rules. Implements the glassmorphism recipe:
  - `.glass` (panel), `.glass-card`, `.glass-input`, `.glass-button` (+ `.danger`).
  - Layout: `.page`, `.topbar`, `.hero`, `.carousel`, `.city-card`, `.hourly-scroll`, `.daily-list`, `.chart-panel`, `.corridor-timeline`, `.alert-list`, `.auth-panel`.
  - Extras: `.weather-icon`, `.delta-badge` (`.up`/`.down`), `.metric-grid`, `.badge`, `.country-tag`.
  - `prefers-reduced-motion` disables animations.
- **Connections:** uses tokens from `tokens.css`; styles the markup produced by all templates.

---

## JavaScript (`static/js/`)

### `liquid.js`
- **What:** The animated liquid background. A fixed full-screen canvas.
- **How:** Reads `--color-background-start/end` from the body's computed style, draws a gradient plus three drifting wave layers with `requestAnimationFrame`. A `MutationObserver` re-reads colors when the body theme class changes. Respects `prefers-reduced-motion`.
- **Connections:** loaded by `head.html` on every page; uses the body's theme class set server-side.

### `clock.js`
- **What:** Live clock. Updates `#live-clock` every second with the current time.
- **Connections:** loaded by `topbar.html`.

### `weather-icons.js`
- **What:** Renders inline SVG weather icons keyed by OpenWeatherMap condition code groups (thunder / rain / snow / mist / sun / cloud).
- **How:** Looks for `[data-weather-icon]` elements and injects an SVG. Exposes `window.renderWeatherIcon` for dynamic re-render.
- **Connections:** used by `index.html`, `corridor.html`; called by `dashboard.js` when switching cities.

### `dashboard.js`
- **What:** Tap-to-switch city on the dashboard.
- **How:** Listens for clicks on `.city-card`. Reads the card's `data-*` attributes, updates the hero DOM (icon, name, temp, condition, high/low, feels-like, humidity, wind, pressure, delta), and toggles the `active` class. Clicks inside forms are ignored.
- **Connections:** loaded by `index.html`; uses `window.renderWeatherIcon` from `weather-icons.js`.

### `history-chart.js`
- **What:** The historical playback chart (Zone 3).
- **How:** Reads `#history-chart` canvas + its `data-city-id`, then `fetch`es `/api/weather/{cityId}/snapshots?days=7`. Draws a temperature line chart on canvas with a hover scrubber (vertical guide + tooltip). Handles the "not enough data" case.
- **Connections:** loaded by `index.html`; consumes the REST API from `WeatherApiController`.

---

## Data flow into the frontend

```
Controller adds objects to the Thymeleaf model
   → template expressions (th:text, th:each, th:if) inject them into HTML
   → CSS (tokens + app) styles them
   → JavaScript enhances behavior (background, icons, switching, chart)
   → data-* attributes + REST API keep live data flowing to JS
```

Next: [07-infrastructure.md](07-infrastructure.md)
