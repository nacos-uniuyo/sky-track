# 03 — JavaScript

The JavaScript in `sky-track-web/src/main/resources/static/js/` adds behavior on top of the server-rendered HTML. There is no framework — every file is a small vanilla JS module wrapped in an IIFE.

| File | Job |
|------|-----|
| `liquid.js` | Animated liquid background |
| `clock.js` | Live clock in the top bar |
| `weather-icons.js` | Renders SVG weather icons |
| `dashboard.js` | Tap-to-switch the hero city |
| `history-chart.js` | History line chart + hover scrubber |

## IIFE pattern

Every file uses an immediately-invoked function expression to avoid polluting the global scope:

```js
(() => {
    // ... code ...
})();
```

The only global exposed is `window.renderWeatherIcon` (used by `dashboard.js`).

## liquid.js — the liquid background

```js
(() => {
    const canvas = document.getElementById('liquid-canvas');
    if (!canvas) {
        return;
    }
    const ctx = canvas.getContext('2d');
    let startColor = '#0D1B2A';
    let endColor = '#1B2838';
    let time = 0;
    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
```

**Reads theme colors from CSS:**
```js
function readThemeColors() {
    const style = getComputedStyle(document.body);
    startColor = style.getPropertyValue('--color-background-start').trim() || startColor;
    endColor = style.getPropertyValue('--color-background-end').trim() || endColor;
}
```

The canvas gradient comes from the `--color-background-start/end` tokens, so it automatically follows the theme set on `<body>`.

**Draws three drifting wave layers:**
```js
function wave(x, amplitude, frequency, phase) {
    return amplitude * Math.sin(x * frequency + time * 0.02 + phase)
            + amplitude * 0.5 * Math.sin(x * frequency * 2.3 + time * 0.03 + phase);
}

function draw() {
    ctx.clearRect(0, 0, width, height);
    const gradient = ctx.createLinearGradient(0, 0, 0, height);
    gradient.addColorStop(0, startColor);
    gradient.addColorStop(1, endColor);
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, width, height);

    const layers = [
        { amplitude: 60, frequency: 0.002, phase: 0, alpha: 0.18 },
        { amplitude: 90, frequency: 0.0012, phase: 2, alpha: 0.12 },
        { amplitude: 40, frequency: 0.003, phase: 4, alpha: 0.10 }
    ];
    layers.forEach((layer) => {
        ctx.beginPath();
        ctx.moveTo(0, height);
        for (let x = 0; x <= width; x += 16) {
            const y = height * 0.7 + wave(x, layer.amplitude, layer.frequency, layer.phase);
            ctx.lineTo(x, y);
        }
        ctx.lineTo(width, height);
        ctx.closePath();
        ctx.fillStyle = `rgba(255, 255, 255, ${layer.alpha})`;
        ctx.fill();
    });
}
```

**Animation loop (respects reduced motion):**
```js
function animate() {
    draw();
    if (!reducedMotion) {
        time += 1;
        window.requestAnimationFrame(animate);
    }
}
```

**Reacts to theme changes** — a `MutationObserver` watches the body class and re-reads colors:
```js
const observer = new MutationObserver(() => {
    readThemeColors();
    if (reducedMotion) {
        draw();
    }
});
observer.observe(document.body, { attributes: true, attributeFilter: ['class'] });
```

**Why a canvas:** CSS alone cannot produce the smooth morphing waves. A canvas lets us redraw on every frame for a fluid, "liquid" feel while reading colors straight from the CSS tokens.

## clock.js — the live clock

```js
(() => {
    const clock = document.getElementById('live-clock');
    if (!clock) {
        return;
    }
    function update() {
        const now = new Date();
        const options = { weekday: 'short', hour: '2-digit', minute: '2-digit', second: '2-digit' };
        clock.textContent = now.toLocaleTimeString(undefined, options);
    }
    update();
    setInterval(update, 1000);
})();
```

Updates the `#live-clock` span every second. Loaded by the top bar fragment.

## weather-icons.js — SVG weather icons

Maps OpenWeatherMap condition codes to inline SVG paths.

```js
const icons = {
    sun: '<circle .../><line .../>...',
    cloud: '<path .../>',
    rain: '...',
    thunder: '...',
    snow: '...',
    mist: '...'
};

function iconFor(conditionCode) {
    if (conditionCode >= 200 && conditionCode < 300) return icons.thunder;
    if (conditionCode >= 300 && conditionCode < 600) return icons.rain;
    if (conditionCode >= 600 && conditionCode < 700) return icons.snow;
    if (conditionCode >= 700 && conditionCode < 800) return icons.mist;
    if (conditionCode === 800) return icons.sun;
    if (conditionCode > 800 && conditionCode < 900) return icons.cloud;
    return icons.sun;
}
```

**Renders into any `[data-weather-icon]` element:**
```js
function render(container, conditionCode) {
    const svg = document.createElementNS(SVG_NS, 'svg');
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.setAttribute('class', 'weather-icon');
    svg.innerHTML = iconFor(conditionCode);
    container.appendChild(svg);
}

document.querySelectorAll('[data-weather-icon]').forEach((element) => {
    const code = element.getAttribute('data-weather-icon');
    if (code) {
        render(element, parseInt(code, 10));
    }
});

window.renderWeatherIcon = render;
```

- Uses `currentColor` for the stroke/fill so the icon inherits the CSS color token.
- `window.renderWeatherIcon` lets `dashboard.js` re-render the hero icon when switching cities.

## dashboard.js — tap-to-switch city

The dashboard is fully server-rendered with all cities' data on the cards. `dashboard.js` reads a tapped card's `data-*` attributes and swaps the hero content in place — no page reload.

```js
const heroFields = {
    icon: document.getElementById('hero-icon'),
    city: document.getElementById('hero-city'),
    temp: document.getElementById('hero-temp'),
    condition: document.getElementById('hero-condition'),
    high: document.getElementById('hero-high'),
    low: document.getElementById('hero-low'),
    feels: document.getElementById('hero-feels'),
    humidity: document.getElementById('hero-humidity'),
    wind: document.getElementById('hero-wind'),
    pressure: document.getElementById('hero-pressure'),
    delta: document.getElementById('hero-delta')
};

document.querySelectorAll('.city-card').forEach((card) => {
    card.addEventListener('click', (event) => {
        if (event.target.closest('form')) {
            return;   // ignore clicks on the Set primary / Remove buttons
        }
        document.querySelectorAll('.city-card').forEach((c) => c.classList.remove('active'));
        card.classList.add('active');
        updateHero(card);
    });
});

function updateHero(card) {
    const city = card.dataset;

    heroFields.icon.innerHTML = '';
    if (city.icon) {
        window.renderWeatherIcon(heroFields.icon, parseInt(city.icon, 10));
    }

    heroFields.city.textContent = city.name;
    heroFields.temp.textContent = city.temp ? city.temp + ' C' : '--';
    heroFields.condition.textContent = city.condition || '';
    heroFields.high.textContent = city.high ? 'H ' + city.high : '';
    heroFields.low.textContent = city.low ? 'L ' + city.low : '';
    heroFields.feels.textContent = city.feels ? 'Feels like ' + city.feels : '';
    heroFields.humidity.textContent = city.humidity ? 'Humidity ' + city.humidity + '%' : '';
    heroFields.wind.textContent = city.wind ? 'Wind ' + city.wind + ' m/s' : '';
    heroFields.pressure.textContent = city.pressure ? 'Pressure ' + city.pressure + ' hPa' : '';

    if (city.delta) {
        const value = parseFloat(city.delta);
        heroFields.delta.textContent = 'Shift ' + (value > 0 ? '+' : '') + value + ' C';
        heroFields.delta.className = 'delta-badge ' + (value >= 0 ? 'up' : 'down');
    } else {
        heroFields.delta.textContent = '';
        heroFields.delta.className = 'delta-badge';
    }
}
```

Key behaviors:
- Clicking anywhere on a card except inside a `<form>` triggers the switch.
- The `active` class moves to the selected card (highlighted via CSS).
- Every hero field is updated from the card's `dataset` (mirror of the `th:data-*` attributes).

## history-chart.js — the historical playback chart

Draws a temperature line chart on a canvas and adds a hover scrubber. Data comes from the REST API, not the page.

```js
const canvas = document.getElementById('history-chart');
const cityId = canvas.dataset.cityId;

function resize() {
    const rect = canvas.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);
    chartWidth = rect.width;
    chartHeight = rect.height;
}
```

**Empty state:**
```js
if (points.length < 2) {
    ctx.fillText('Not enough historical data yet', chartWidth / 2, chartHeight / 2);
    return;
}
```

**Scaling and drawing:**
```js
const temps = points.map((p) => p.temperature);
const minTemp = Math.min(...temps);
const maxTemp = Math.max(...temps);
const pad = 40;
const range = maxTemp - minTemp || 1;

const xFor = (i) => pad + (i * (chartWidth - pad * 2)) / (points.length - 1);
const yFor = (t) => pad + ((maxTemp - t) / range) * (chartHeight - pad * 2);
```

- Draws horizontal grid lines with temperature labels.
- Draws the polyline and dot markers using the theme's `--color-primary`.

**Hover scrubber** — maps the mouse X to the nearest point index and draws a dashed vertical guide + tooltip:
```js
function onMove(event) {
    const x = event.clientX - rect.left;
    const index = Math.round(((x - pad) / (chartWidth - pad * 2)) * (points.length - 1));
    hoverIndex = Math.max(0, Math.min(points.length - 1, index));
    draw();
}
```

**Data fetch:**
```js
if (cityId) {
    fetch('/api/weather/' + cityId + '/snapshots?days=7')
        .then((response) => response.json())
        .then((data) => {
            points = data;
            draw();
        })
        .catch(() => {});
}
```

This is the one place the frontend consumes the backend REST API directly.

## Script loading order (index.html)

```html
<script th:src="@{/js/weather-icons.js}" defer></script>
<script th:src="@{/js/dashboard.js}" defer></script>
<script th:src="@{/js/history-chart.js}" defer></script>
```

`defer` runs scripts in order after the DOM is parsed, so `weather-icons.js` (which exposes `renderWeatherIcon`) runs before `dashboard.js` uses it.

## Test your understanding

**Q1. Why does `liquid.js` use a canvas instead of CSS animations?**
**A.** CSS can animate transforms and gradients but cannot cheaply draw per-pixel flowing waves. A canvas redraws every frame, which is what creates the smooth "liquid" effect, while still reading colors from the CSS tokens.

**Q2. How does `liquid.js` know the current theme colors?**
**A.** It reads `getComputedStyle(document.body).getPropertyValue('--color-background-start')`. The theme class on `<body>` changes these variables; a `MutationObserver` re-reads them when the class changes.

**Q3. How does `dashboard.js` avoid breaking when the user clicks the "Remove" button?**
**A.** The click handler checks `event.target.closest('form')` and returns early, so clicks inside forms (Set primary / Remove) are left to the form's own submission.

**Q4. Why does `weather-icons.js` expose `window.renderWeatherIcon`?**
**A.** So `dashboard.js` can re-render the hero icon when switching cities without re-running the whole script or duplicating the icon-drawing code.

**Q5. Where does `history-chart.js` get its data?**
**A.** It fetches `GET /api/weather/{cityId}/snapshots?days=7` (a backend REST endpoint). The city id comes from the canvas's `data-city-id` attribute. This is the only JS-driven API call in the app.

**Q6. What does `prefers-reduced-motion` do in `liquid.js`?**
**A.** If the user's OS requests reduced motion, the script still draws one static frame but never starts the `requestAnimationFrame` loop, honoring the accessibility preference.
