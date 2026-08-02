# 04 — CSS and Design Tokens

The styling lives in `sky-track-web/src/main/resources/static/css/` in two files: `tokens.css` (the design system) and `app.css` (the components). Together they implement `DESIGN.md`.

## tokens.css — the design system

`tokens.css` defines **CSS custom properties** (variables) that every component uses. It has a base set plus four theme overrides.

### The base tokens (`:root`)

```css
:root {
    --color-primary: #FF7E00;
    --color-secondary: #FFD700;
    --color-accent: #4A90E2;
    --color-surface: rgba(255, 255, 255, 0.12);
    --color-surface-border: rgba(255, 255, 255, 0.18);
    --color-text-primary: #FFFFFF;
    --color-text-secondary: rgba(255, 255, 255, 0.72);
    --glass-blur: 20px;
    --glass-radius: 16px;
    --font-metrics: 'Inter', 'Plus Jakarta Sans', sans-serif;
    --font-script: 'Playfair Display', 'Cormorant Garamond', serif;
}
```

| Token | Meaning |
|-------|---------|
| `--color-primary` | Brand/accent color (buttons focus ring, chart line) |
| `--color-secondary` | Secondary accent (icons) |
| `--color-accent` | Links/highlights |
| `--color-surface` / `--color-surface-border` | Glass panel fill + border |
| `--color-text-primary` / `--color-text-secondary` | Text colors |
| `--glass-blur` / `--glass-radius` | Glass effect parameters |
| `--font-metrics` | Sans-serif stack (Inter) for data |
| `--font-script` | Serif stack (Playfair) for greetings |

### Theme overrides

```css
.theme-morning {
    --color-primary: #FF7E00;
    --color-secondary: #FFD700;
    --color-accent: #4A90E2;
    --color-background-start: #0D1B2A;
    --color-background-end: #1B2838;
}

.theme-afternoon {
    --color-primary: #1E88E5;
    --color-secondary: #FFC107;
    --color-accent: #FFFFFF;
    --color-background-start: #1565C0;
    --color-background-end: #42A5F5;
}

.theme-evening {
    --color-primary: #D35400;
    --color-secondary: #8E44AD;
    --color-accent: #2C3E50;
    --color-background-start: #1A0A2E;
    --color-background-end: #3D1E6D;
}

.theme-night {
    --color-primary: #0B0F19;
    --color-secondary: #1B263B;
    --color-accent: #E0E6ED;
    --color-background-start: #060B14;
    --color-background-end: #0D1520;
}
```

**How theming works:** the backend puts one of these classes on `<body>` (`theme-morning` etc. from `GreetingService.themeClass`). Because CSS variables cascade, every component that uses a `var(--color-...)` instantly switches palette. No per-theme component CSS is needed.

## app.css — components and layout

`app.css` defines the actual look: the glass recipe, layout, and every component class.

### The glassmorphism recipe

```css
.glass {
    background: var(--color-surface);
    backdrop-filter: blur(var(--glass-blur));
    -webkit-backdrop-filter: blur(var(--glass-blur));
    border: 1px solid var(--color-surface-border);
    border-radius: var(--glass-radius);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
}

.glass-card {
    background: rgba(255, 255, 255, 0.08);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid rgba(255, 255, 255, 0.10);
    border-radius: 12px;
    padding: 16px;
    transition: transform 0.2s ease, background 0.2s ease;
}

.glass-card:hover {
    background: rgba(255, 255, 255, 0.14);
    transform: translateY(-2px);
}

.glass-input {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.14);
    border-radius: 10px;
    color: var(--color-text-primary);
    padding: 10px 14px;
    width: 100%;
    font: inherit;
}

.glass-input:focus {
    border-color: var(--color-primary);
    outline: none;
    box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.08);
}

.glass-button {
    background: rgba(255, 255, 255, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.22);
    border-radius: 10px;
    color: var(--color-text-primary);
    padding: 10px 20px;
    cursor: pointer;
    font: inherit;
    transition: background 0.2s ease, transform 0.1s ease;
}

.glass-button:hover { background: rgba(255, 255, 255, 0.25); }
.glass-button:active { transform: scale(0.97); }
.glass-button.danger { background: rgba(220, 38, 38, 0.35); }
```

- `.glass` — full panels (hero, search, auth).
- `.glass-card` — small tiles (city cards, forecast tiles) with a hover lift.
- `.glass-input` / `.glass-button` — form controls.
- Everything uses translucent white + `backdrop-filter: blur(...)` → the frosted look.

### Layout classes

```css
.page { max-width: 960px; margin: 0 auto; padding: 24px 16px; display: flex; flex-direction: column; gap: 20px; }
.topbar { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px; }
.hero { text-align: center; padding: 32px 24px; }
.carousel { display: flex; gap: 12px; overflow-x: auto; scroll-snap-type: x mandatory; }
.hourly-scroll { display: flex; gap: 12px; overflow-x: auto; scroll-snap-type: x mandatory; }
.daily-list { display: grid; gap: 8px; }
```

- Carousels/horiz scroll use `scroll-snap-type: x mandatory` for snap scrolling.
- The page is a vertical flex column capped at 960px.

### Component-specific classes

```css
.hero-temp { font-weight: 700; font-size: clamp(4rem, 12vw, 7rem); line-height: 1; }
.greeting { font-family: var(--font-script); font-size: clamp(1.6rem, 4vw, 2.6rem); }
.weather-icon { width: 64px; height: 64px; color: var(--color-secondary); }
.delta-badge.up { color: #4ade80; }
.delta-badge.down { color: #f87171; }
.metric-grid { display: flex; justify-content: center; gap: 24px; flex-wrap: wrap; }
.city-card.active { background: rgba(255, 255, 255, 0.18); border-color: var(--color-primary); }
```

- The big hero temperature uses `clamp()` so it scales fluidly.
- Delta badges are green (up) / red (down).
- The active carousel card is highlighted with the primary color border.

### Accessibility

```css
@media (prefers-reduced-motion: reduce) {
    * {
        animation-duration: 0.01ms !important;
        transition-duration: 0.01ms !important;
    }
}
```

Disables animations/transitions for users who request reduced motion.

## Typography rules

- **Data/metrics** use `var(--font-metrics)` (Inter) — clean and readable.
- **Greetings** use `var(--font-script)` (Playfair Display) — elegant script look.
- Fonts are loaded from Google Fonts in the `head` fragment with `display=swap`.

## Why tokens instead of hardcoded colors?

1. **One change updates everything** — change `--color-primary` and every button/focus/chart updates.
2. **Themes are free** — a body class swap re-colors the whole app because every rule references variables.
3. **Consistency** — no scattered `#FF7E00` literals that can drift off-spec.
4. **DESIGN.md compliance** — the token values come directly from the design system.

## Test your understanding

**Q1. How does the whole app change theme with just one body class?**
**A.** All colors are `var(--color-...)`. Setting `theme-night` on `<body>` overrides those variables for the whole tree, so every component re-colors automatically — no per-theme CSS required.

**Q2. What is the role of `backdrop-filter: blur(...)` in the glass effect?**
**A.** It blurs whatever is behind the element (the liquid background canvas), creating the frosted-glass look. Without it the panels would be flat translucent rectangles.

**Q3. Why is the hero temperature sized with `clamp(4rem, 12vw, 7rem)`?**
**A.** `clamp` sets a fluid size: it scales between 4rem and 7rem based on viewport width, so the number looks right on phones and desktop without media queries.

**Q4. How does `scroll-snap-type: x mandatory` improve the carousel?**
**A.** It makes horizontal scrolling snap to card boundaries instead of stopping mid-card — the behavior DESIGN.md specifies for the favorites and hourly carousels.

**Q5. What would happen if a component hardcoded `#0f766e` instead of a token?**
**A.** It would not follow the theme (looking wrong at night) and could drift from DESIGN.md. This is why the rule is "no hardcoded colors in templates or components."

**Q6. How does `prefers-reduced-motion` affect both CSS and JS?**
**A.** CSS disables transitions/animations via the media query, and `liquid.js` skips its animation loop. Both honor the same user preference.
