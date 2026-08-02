# 02 — Templates and Fragments

Every template and fragment in `sky-track-web/src/main/resources/templates/`, with what renders it, what data it uses, and how it is structured.

## Fragments (`fragments/`)

### `fragments/head.html`
- **Fragment:** `head(title)`
- **Purpose:** the common `<head>` for every page.
- **Content:**
  - meta charset + viewport,
  - page `<title>` (parameterized),
  - Google Fonts preconnect + stylesheet (Inter + Playfair Display),
  - `tokens.css`,
  - `app.css`,
  - `liquid.js` (deferred).
- **Used by:** every template via `th:replace="~{fragments/head :: head('Title')}"`.

```html
<head th:fragment="head(title)">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${title}">Sky Track</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,600;1,400&display=swap" rel="stylesheet">
    <link rel="stylesheet" th:href="@{/css/tokens.css}">
    <link rel="stylesheet" th:href="@{/css/app.css}">
    <script th:src="@{/js/liquid.js}" defer></script>
</head>
```

### `fragments/topbar.html`
- **Fragment:** `topbar(greeting)`
- **Purpose:** the glass top bar with the script-font greeting, live clock, Alerts bell, and logout.
- **Data:** `greeting` (parameter).
- **Loads:** `clock.js` (deferred).

```html
<header th:fragment="topbar(greeting)" class="topbar glass">
    <div>
        <p class="greeting" th:text="${greeting}">Hi comrade, Good morning</p>
    </div>
    <div class="topbar-right">
        <span id="live-clock" class="live-clock"></span>
        <a th:href="@{/alerts}" class="bell-link glass-button" title="Alerts">Alerts</a>
        <form method="post" th:action="@{/logout}">
            <button type="submit" class="glass-button">Log out</button>
        </form>
    </div>
    <script th:src="@{/js/clock.js}" defer></script>
</header>
```

## `index.html` — the dashboard

**Rendered by:** `HomeController.index` (`GET /`).
**Data:** `greeting`, `themeClass`, `firstName`, `alerts`, `cities`, `primaryCityView`, `cityMessage`, `forecast`, `history`, `searchResults`, `searchQuery`.
**Loads:** `liquid.js` (via head), `clock.js` (via topbar), `weather-icons.js`, `dashboard.js`, `history-chart.js`.

Structure (the three dashboard zones from SYSTEM-DESIGN):

### Zone 1 — the hero (primary city)
```html
<section th:if="${primaryCityView}" id="hero" class="hero glass">
    <p class="city-message" th:if="${cityMessage}" th:text="${cityMessage}">Skies are calm.</p>
    <div id="hero-icon" class="hero-icon" data-weather-icon="${primaryCityView.conditionCode}"></div>
    <h2 id="hero-city" class="city-name" th:text="${primaryCityView.cityName}">Lagos</h2>
    <p id="hero-temp" class="hero-temp" th:text="${primaryCityView.temperature} != null ? ${primaryCityView.temperature} + ' C' : '--'">28 C</p>
    <p id="hero-condition" class="condition" th:text="${primaryCityView.conditionText}">clear sky</p>
    <span id="hero-delta" class="delta-badge" th:if="${primaryCityView.temperatureDelta != null}" ...>Shift +1.2 C</span>
    <div class="hero-meta"> H / L / Feels like </div>
    <div class="metric-grid"> Humidity / Wind / Pressure </div>
</section>
```
- The empty-state hero (`th:if="${primaryCityView == null}"`) shows the "Choose your primary city" prompt.
- Elements carry `id`s that `dashboard.js` updates on city switch.

### Zone 2 — the favorites carousel
```html
<article th:each="city : ${cities}"
         class="glass-card city-card"
         th:data-name="${city.cityName}"
         th:data-temp="${city.temperature}"
         th:data-condition="${city.conditionText}"
         th:data-icon="${city.conditionCode}"
         th:data-high="${city.tempMax}"
         th:data-low="${city.tempMin}"
         th:data-feels="${city.feelsLike}"
         th:data-humidity="${city.humidity}"
         th:data-wind="${city.windSpeed}"
         th:data-pressure="${city.pressure}"
         th:data-delta="${city.temperatureDelta}"
         th:classappend="${city.primaryCity} ? ' active'">
    <span class="badge" th:if="${city.primaryCity}">Primary</span>
    <div class="city-icon" data-weather-icon="${city.conditionCode}"></div>
    <h4 class="city-name" th:text="${city.cityName}">Lagos</h4>
    <span class="country-tag" th:if="${city.countryCode}" th:text="${city.countryCode}">NG</span>
    <p class="city-temp" th:text="...">28 C</p>
    <span class="delta-badge" th:if="${city.temperatureDelta != null}" ...>+1.2 C</span>
    <div class="card-actions"> Set primary / Remove forms </div>
</article>
<a href="#search" class="glass-card add-card">+ Add city</a>
```
- Each card embeds its full data via `th:data-*`.
- The trailing `+ Add city` card links to the search panel.

### Zone 3 — forecasts and history
```html
<section th:if="${forecast != null && !forecast.hourly.isEmpty()}" class="forecast-section">
    <h3 class="section-title">Hourly forecast</h3>
    <div class="hourly-scroll">
        <article th:each="hour : ${forecast.hourly}" class="glass-card hourly-card">
            <span class="hourly-time" th:text="${hour.time}">14:00</span>
            <div class="hourly-icon" th:attr="data-weather-icon=${hour.conditionCode}"></div>
            <span class="hourly-temp" th:text="${hour.temperature} != null ? ${hour.temperature} + ' C' : '--'">28 C</span>
        </article>
    </div>
</section>

<section th:if="${forecast != null && !forecast.daily.isEmpty()}" class="forecast-section">
    <h3 class="section-title">7-day forecast</h3>
    <div class="daily-list">
        <article th:each="day : ${forecast.daily}" class="glass-card daily-row">
            <span class="daily-day" th:text="${day.day}">MONDAY</span>
            <div class="daily-icon" th:attr="data-weather-icon=${day.conditionCode}"></div>
            <span class="daily-pop" th:if="${day.precipitation != null}" ...>20%</span>
            <span class="daily-temps"><span class="daily-max">30 C</span><span class="daily-min">26 C</span></span>
        </article>
    </div>
</section>

<section th:if="${!history.isEmpty()}" class="forecast-section">
    <h3 class="section-title">Historical playback</h3>
    <div class="glass chart-panel">
        <canvas id="history-chart" class="history-canvas" th:attr="data-city-id=${primaryCityView.id}"></canvas>
    </div>
</section>
```
- The chart canvas gets `data-city-id`, which `history-chart.js` uses to fetch snapshots.

### Search panel
```html
<section id="search" class="search-panel glass">
    <form method="get" th:action="@{/}" class="search-form">
        <input type="text" name="q" class="glass-input" ... th:value="${searchQuery}">
        <button type="submit" class="glass-button">Search</button>
    </form>
    <ul th:if="${searchResults != null && !searchResults.isEmpty()}" class="search-results">
        <li th:each="city : ${searchResults}" class="glass-card search-result">
            <span th:text="${city.name}">Lagos</span>
            <form method="post" th:action="@{/cities}">
                <input type="hidden" name="cityName" th:value="${city.name}">
                <input type="hidden" name="country" th:value="${city.country}">
                <input type="hidden" name="latitude" th:value="${city.lat}">
                <input type="hidden" name="longitude" th:value="${city.lon}">
                <button type="submit" class="glass-button">Track</button>
            </form>
        </li>
    </ul>
</section>
```
- GET search posts to the same page (`?q=...`); the controller re-renders with results.
- Each result's "Track" form posts the hidden city details to `/cities`.

## `onboarding.html` — primary city selection

**Rendered by:** `OnboardingController` (`GET /onboarding`, `POST /onboarding/search`).
**Data:** `greeting`, `themeClass`, `results`, `query`.

- Greeting + title + city search form (`POST /onboarding/search`).
- Results list with a "Select" form per city (`POST /onboarding/select`).
- "Skip for now" link to `/`.

```html
<form method="post" th:action="@{/onboarding/search}" class="auth-form">
    <input type="text" name="query" class="glass-input" ... th:value="${query}">
    <button type="submit" class="glass-button">Search</button>
</form>
<ul th:if="${!results.isEmpty()}" class="search-results">
    <li th:each="city : ${results}" class="glass-card search-result">
        <span th:text="${city.name}">Lagos</span>
        <form method="post" th:action="@{/onboarding/select}">
            <input type="hidden" name="cityName" th:value="${city.name}">
            ...
            <button type="submit" class="glass-button">Select</button>
        </form>
    </li>
</ul>
```

## `corridor.html` — travel weather corridor

**Rendered by:** `CorridorController` (`GET/POST /corridor`).
**Data:** `greeting`, `themeClass`, `corridor`, `originQuery`, `destinationQuery`.
**Loads:** `weather-icons.js`.

- Two city inputs (origin, destination) → `POST /corridor`.
- Result: a horizontal `corridor-point` timeline (origin → stops → destination), each with a weather icon + temperature.
- Empty-points fallback message if a city was not found.

```html
<section th:if="${corridor != null && !corridor.points.isEmpty()}" class="glass search-panel">
    <h3 class="section-title" th:text="${corridor.origin} + ' to ' + ${corridor.destination}">Lagos to London</h3>
    <div class="corridor-timeline">
        <article th:each="point : ${corridor.points}" class="glass-card corridor-point">
            <div class="corridor-icon" th:attr="data-weather-icon=${point.conditionCode}"></div>
            <div class="corridor-info">
                <span class="corridor-label" th:text="${point.label}">Stop 1</span>
                <span class="corridor-temp" th:text="${point.temperature} != null ? ${point.temperature} + ' C' : '--'">28 C</span>
            </div>
        </article>
    </div>
</section>
```

## `alerts.html` — the alerts feed

**Rendered by:** `AlertsController` (`GET /alerts`).
**Data:** `greeting`, `themeClass`, `alerts`.

- Empty state: "No active alerts for your tracked cities."
- Each alert card: severity badge, type, title, description, detection time, and an Acknowledge button (hidden once acknowledged).

```html
<ul class="alert-list">
    <li th:each="alert : ${alerts}" class="glass-card alert-item">
        <div>
            <span class="badge" th:text="${alert.severity}">HIGH</span>
            <span class="alert-type" th:text="${alert.alertType}">PRESSURE_DROP</span>
            <h4 th:text="${alert.title}">Rapid pressure drop</h4>
            <p th:text="${alert.description}">Pressure fell 6 hPa for Lagos</p>
            <span class="alert-time" th:text="${alert.detectedAt}">2026-08-02 05:00</span>
        </div>
        <form th:unless="${alert.acknowledged}" method="post" th:action="@{/alerts/{id}/acknowledge(id=${alert.id})}">
            <button type="submit" class="glass-button">Acknowledge</button>
        </form>
        <span th:if="${alert.acknowledged}" class="acknowledged">Acknowledged</span>
    </li>
</ul>
```

## `auth/login.html`

**Rendered by:** `AuthController.login`.
**Data:** `themeClass`.

- Glass auth panel with email + password fields.
- Posts to `/login` (Spring Security form login; username parameter is `email`).
- Success/error banners via `th:if="${param.error}"` / `param.logout` / `param.registered`.

```html
<form method="post" th:action="@{/login}" class="auth-form">
    <label>Email <input type="email" name="email" class="glass-input" required autocomplete="email"></label>
    <label>Password <input type="password" name="password" class="glass-input" required autocomplete="current-password"></label>
    <button type="submit" class="glass-button">Log in</button>
</form>
```

## `auth/signup.html`

**Rendered by:** `AuthController.signup`.
**Data:** `signupRequest`, `themeClass`.

- Bound form (`th:object="${signupRequest}"`) with first name, last name, email, password.
- Validation messages via `th:errors`.
- Posts to `/signup`; backend auto-logs-in and redirects to `/onboarding`.

```html
<form method="post" th:action="@{/signup}" th:object="${signupRequest}" class="auth-form">
    <label>
        First name
        <input th:field="*{firstName}" class="glass-input" required maxlength="60" autocomplete="given-name">
        <span class="error" th:if="${#fields.hasErrors('firstName')}" th:errors="*{firstName}"></span>
    </label>
    ...
</form>
```

## Test your understanding

**Q1. Why does `index.html` have two hero sections?**
**A.** One renders when `primaryCityView` exists (the full weather hero); the other renders when it is `null` (the "welcome / choose a primary city" prompt). `th:if`/`th:if="...== null"` make them mutually exclusive.

**Q2. How does data from the backend reach `dashboard.js`?**
**A.** Through `th:data-*` attributes on each city card. The server renders `data-name="Lagos"`, `data-temp="24.57"`, etc.; `dashboard.js` reads them with `card.dataset` and swaps the hero content.

**Q3. What is the `+ Add city` card and where does it point?**
**A.** It is an `<a href="#search">` that scrolls to the search panel on the same page — the user can search and track a city without leaving the dashboard.

**Q4. Why do search-result forms use hidden inputs instead of a JSON API call?**
**A.** The page is server-rendered; posting hidden fields to `/cities` (or `/onboarding/select`) follows the standard form/PRG flow and requires no client-side state or fetch code.

**Q5. What does `th:attr="data-city-id=${primaryCityView.id}"` produce?**
**A.** It produces `data-city-id="1"` on the canvas. `history-chart.js` reads `canvas.dataset.cityId` and fetches `/api/weather/1/snapshots?days=7`.

**Q6. How is the login form tied to Spring Security?**
**A.** It posts to `/login` with fields named `email` and `password`. `SecurityConfig` sets `usernameParameter("email")`, so Spring Security treats the email field as the username.
