# 05 — Dashboard Components

The dashboard is laid out in three zones plus a top bar, as defined in `SYSTEM-DESIGN.md`. This page explains each component from a design perspective and shows how it is implemented.

## The top bar

**Design:** greeting (script font) on the left; live clock, Alerts bell, and logout on the right.

```html
<header th:fragment="topbar(greeting)" class="topbar glass">
    <p class="greeting" th:text="${greeting}">Hi comrade, Good morning</p>
    <div class="topbar-right">
        <span id="live-clock" class="live-clock"></span>
        <a th:href="@{/alerts}" class="bell-link glass-button">Alerts</a>
        <form method="post" th:action="@{/logout}"><button class="glass-button">Log out</button></form>
    </div>
</header>
```

```css
.topbar { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; }
.greeting { font-family: var(--font-script); font-size: clamp(1.6rem, 4vw, 2.6rem); }
```

## Zone 1 — Hero Weather Card

**Design (DESIGN.md):** large temperature, animated weather icon, high/low range, feels-like.

```html
<section th:if="${primaryCityView}" id="hero" class="hero glass">
    <p class="city-message" th:if="${cityMessage}" th:text="${cityMessage}">Skies are calm.</p>
    <div id="hero-icon" class="hero-icon" data-weather-icon="${primaryCityView.conditionCode}"></div>
    <h2 id="hero-city" class="city-name" th:text="${primaryCityView.cityName}">Lagos</h2>
    <p id="hero-temp" class="hero-temp" th:text="...">28 C</p>
    <p id="hero-condition" class="condition" th:text="${primaryCityView.conditionText}">clear sky</p>
    <span id="hero-delta" class="delta-badge" th:if="..." ...>Shift +1.2 C</span>
    <div class="hero-meta"> H / L / Feels like </div>
    <div class="metric-grid"> Humidity / Wind / Pressure </div>
</section>
```

```css
.hero { text-align: center; padding: 32px 24px; }
.hero-temp { font-weight: 700; font-size: clamp(4rem, 12vw, 7rem); line-height: 1; }
.metric-grid { display: flex; justify-content: center; gap: 24px; flex-wrap: wrap; }
```

Key design decisions:
- Centered layout; the temperature dominates with a huge fluid size.
- A delta badge shows the "shift" (green `up` / red `down`).
- The metric grid gives humidity, wind, and pressure at a glance.

## Zone 2 — Favorites Carousel

**Design:** a horizontal snap-scrolling row of glass mini-cards; primary city pinned first (badged "Primary"); a trailing "+ Add city" card.

```html
<div class="carousel">
    <article th:each="city : ${cities}"
             class="glass-card city-card"
             th:data-name="${city.cityName}" ... th:data-delta="${city.temperatureDelta}"
             th:classappend="${city.primaryCity} ? ' active'">
        <span class="badge" th:if="${city.primaryCity}">Primary</span>
        <div class="city-icon" data-weather-icon="${city.conditionCode}"></div>
        <h4 class="city-name" th:text="${city.cityName}">Lagos</h4>
        <span class="country-tag" th:if="${city.countryCode}" th:text="${city.countryCode}">NG</span>
        <p class="city-temp" th:text="...">28 C</p>
        <span class="delta-badge" th:if="${city.temperatureDelta != null}" ...>+1.2 C</span>
        <div class="card-actions"> Set primary / Remove </div>
    </article>
    <a href="#search" class="glass-card add-card">+ Add city</a>
</div>
```

```css
.carousel { display: flex; gap: 12px; overflow-x: auto; padding-bottom: 8px; scroll-snap-type: x mandatory; }
.city-card { position: relative; min-width: 180px; flex: 0 0 auto; cursor: pointer; }
.city-card.active { background: rgba(255, 255, 255, 0.18); border-color: var(--color-primary); }
```

Design decisions:
- `scroll-snap-type: x mandatory` gives the snap-scroll behavior.
- The `active` card is highlighted with the primary border (managed by `dashboard.js`).
- Tapping a card switches the hero (client-side).

## Zone 3 — Forecasts and Analytics

### Hourly forecast (horizontal snap scroll)

```html
<div class="hourly-scroll">
    <article th:each="hour : ${forecast.hourly}" class="glass-card hourly-card">
        <span class="hourly-time" th:text="${hour.time}">14:00</span>
        <div class="hourly-icon" th:attr="data-weather-icon=${hour.conditionCode}"></div>
        <span class="hourly-temp" th:text="...">28 C</span>
    </article>
</div>
```

```css
.hourly-scroll { display: flex; gap: 12px; overflow-x: auto; scroll-snap-type: x mandatory; }
.hourly-card { min-width: 90px; text-align: center; display: grid; gap: 6px; justify-items: center; }
```

Each tile: time, icon, temperature.

### 7-day forecast (vertical list)

```html
<div class="daily-list">
    <article th:each="day : ${forecast.daily}" class="glass-card daily-row">
        <span class="daily-day" th:text="${day.day}">MONDAY</span>
        <div class="daily-icon" th:attr="data-weather-icon=${day.conditionCode}"></div>
        <span class="daily-pop" th:if="...">20%</span>
        <span class="daily-temps"><span class="daily-max">30 C</span><span class="daily-min">26 C</span></span>
    </article>
</div>
```

```css
.daily-list { display: grid; gap: 8px; }
.daily-row { display: flex; align-items: center; gap: 16px; padding: 12px 16px; }
.daily-temps { margin-left: auto; display: flex; gap: 12px; }
.daily-max { font-weight: 700; }
.daily-min { color: var(--color-text-secondary); }
```

Day name on the left, icon, precipitation %, high/low pushed to the right.

### Historical playback (chart)

```html
<section th:if="${!history.isEmpty()}" class="forecast-section">
    <h3 class="section-title">Historical playback</h3>
    <div class="glass chart-panel">
        <canvas id="history-chart" class="history-canvas" th:attr="data-city-id=${primaryCityView.id}"></canvas>
    </div>
</section>
```

```css
.chart-panel { padding: 16px; }
.history-canvas { width: 100%; height: 240px; display: block; }
```

The chart line uses `--color-primary` (read at runtime by `history-chart.js`), so it follows the theme.

## Search panel

```css
.search-panel { padding: 20px 24px; }
.search-form { display: flex; gap: 10px; margin-bottom: 16px; }
.search-result { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 10px 16px; }
```

The search panel holds the "Track a city" form and its results — a glass surface like every other zone.

## Micro-animations

| Component | Effect |
|-----------|--------|
| `.glass-card:hover` | `translateY(-2px)` lift + brighter fill |
| `.glass-button:active` | `scale(0.97)` press |
| `.glass-button:hover` | brighter fill |
| Liquid background | continuous `requestAnimationFrame` drift |

All are disabled under `prefers-reduced-motion`.

## Test your understanding

**Q1. How is the primary city visually distinguished in the carousel?**
**A.** It gets a "Primary" badge and the `active` class (primary-colored border + brighter fill).

**Q2. Why does the carousel use `scroll-snap-type: x mandatory`?**
**A.** So horizontal scrolling snaps to card boundaries, matching the snap-scroll behavior DESIGN.md specifies for mobile.

**Q3. Where do the weather icons come from?**
**A.** Each element with `data-weather-icon` gets an SVG injected by `weather-icons.js`. The icon color uses `currentColor`, which resolves to `--color-secondary`.

**Q4. How does the chart line follow the theme?**
**A.** `history-chart.js` reads `getComputedStyle(document.body).getPropertyValue('--color-primary')` at draw time and strokes the line with it.

**Q5. What is the purpose of the metric grid in the hero?**
**A.** It surfaces humidity, wind, and pressure at a glance without cluttering the main temperature readout — quick-glanceability per the design goals.
