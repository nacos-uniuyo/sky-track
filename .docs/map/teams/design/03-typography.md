# 03 — Typography

Typography is one of the strongest parts of the Sky Track identity: a clean sans-serif for data and an elegant script for greetings.

## The two font families

| Stack token | Family | Mood |
|-------------|--------|------|
| `--font-metrics` | Inter (+ Plus Jakarta Sans fallback) | Clean, readable, for numbers and labels |
| `--font-script` | Playfair Display (+ Cormorant Garamond fallback) | Elegant, decorative, for greetings |

```css
--font-metrics: 'Inter', 'Plus Jakarta Sans', sans-serif;
--font-script: 'Playfair Display', 'Cormorant Garamond', serif;
```

## Type scale

| Role | Font stack | Weight | Size | Used for |
|------|-----------|--------|------|----------|
| Time greeting | `--font-script` | 400 | 2rem–3rem | "Good Morning", "Good Evening" |
| Location name | `--font-metrics` | 600 | 1.5rem | City, country |
| Temperature (hero) | `--font-metrics` | 700 | 6rem–8rem | Current temp |
| Labels / captions | `--font-metrics` | 500 | 0.875rem | High/Low, feels like, wind |
| Glass card titles | `--font-metrics` | 600 | 1rem | Hourly, forecast headers |

## How the fonts load

The Google Fonts import lives in the `head` fragment so every page gets it:

```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,600;1,400&display=swap" rel="stylesheet">
```

- `display=swap` means text renders with a fallback font immediately and swaps to the web font when loaded (no invisible text).
- `preconnect` speeds up the connection to the font host.

## Where the fonts are used in code

### The greeting (script font)

```html
<!-- topbar.html -->
<p class="greeting" th:text="${greeting}">Hi comrade, Good morning</p>
```

```css
/* app.css */
.greeting {
    font-family: var(--font-script);
    font-size: clamp(1.6rem, 4vw, 2.6rem);
}
```

The greeting is the personality moment — script font, fluid size.

### The hero temperature (bold metrics font)

```html
<!-- index.html -->
<p id="hero-temp" class="hero-temp" th:text="${primaryCityView.temperature} + ' C'">28 C</p>
```

```css
.hero-temp {
    font-family: var(--font-metrics);
    font-weight: 700;
    font-size: clamp(4rem, 12vw, 7rem);
    line-height: 1;
}
```

Big, bold, centered — the star of the hero.

### Labels and card titles (metrics font)

```css
.section-title {
    font-family: var(--font-metrics);
    font-weight: 600;
    font-size: 1rem;
    text-transform: uppercase;
    letter-spacing: 1px;
    color: var(--color-text-secondary);
}

.metric-label {
    font-size: 0.8rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    color: var(--color-text-secondary);
}
```

## Fallback rule

If Google Fonts is unavailable, `sans-serif` and `serif` system fonts must still render legibly. That is why the stacks end with generic families (`sans-serif`, `serif`).

## Test your understanding

**Q1. Why two different fonts instead of one?**
**A.** Data (temperatures, labels) needs a clean, legible sans-serif; the greeting is a brand/personality moment that benefits from an elegant script. The pairing gives both readability and character.

**Q2. What does `display=swap` do?**
**A.** It lets the browser show text immediately with a fallback font, then swap to the web font once loaded — preventing invisible-text (FOIT) and keeping the page usable offline/slowly.

**Q3. Which token would you use for a new metric readout?**
**A.** `var(--font-metrics)`, with weight 500–700 depending on emphasis. Data never uses the script font.

**Q4. How is the greeting font applied?**
**A.** The `.greeting` class sets `font-family: var(--font-script)`; the top bar fragment renders the greeting `<p class="greeting">`. Both the token and the class must stay consistent.

**Q5. Why does the hero temperature use `clamp()`?**
**A.** `clamp(4rem, 12vw, 7rem)` makes the number fluid — it scales with the viewport between 4rem and 7rem, so it looks balanced on phones and desktop without extra media queries.
