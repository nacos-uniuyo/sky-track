# Design System: Sky Track

## Overview

Sky Track is a real-time weather tracking application with premium visual aesthetics. The UI uses a dynamic liquid background system that adapts to real-time weather conditions, time-of-day phases, and geographical context. A glassmorphism layout layer floats over an organic, morphing background.

## Design Principles

> These are **non-negotiable** — every frontend change must comply.

1. **Glassmorphism-first** — all panels, cards, and UI surfaces use the glassmorphism recipe below. No solid opaque panels.
2. **Liquid morphing background** — the background responds to weather + time-of-day. Never static.
3. **Theme-driven palettes** — all colors come from the token system below. No hardcoded hex values in templates or component styles.
4. **Sticky consistency** — fonts, spacing, and micro-animations follow this document. Do not deviate.

---

## Theme System

Four visual states mapped to time-of-day. The active theme is set as a class on the `body` element (`.theme-morning`, `.theme-afternoon`, `.theme-evening`, `.theme-night`).

### Morning Theme

| Property | Value |
|----------|-------|
| Reference | skyline sunbursts, soft golden hour landscapes |
| Primary | `#FF7E00` (soft amber) |
| Secondary | `#FFD700` (golden yellow) |
| Accent | `#4A90E2` (pale morning blue) |
| Elements | Gentle sun ray simulations, rolling landscape silhouettes, soft warm highlights |

### Afternoon Theme

| Property | Value |
|----------|-------|
| Reference | brilliant sky blue, radiant sunburst glows, shimmering reflections |
| Primary | `#1E88E5` (brilliant sky blue) |
| Secondary | `#FFC107` (vibrant gold) |
| Accent | `#FFFFFF` (radiant white sunburst) |
| Elements | Crisp cloud particles, high-contrast urban skyline vectors, bright shimmering water reflections |

### Evening / Sunset Theme

| Property | Value |
|----------|-------|
| Reference | dramatic fiery clouds, glowing city lights, amber waterfront glows |
| Primary | `#D35400` (deep burnt orange) |
| Secondary | `#8E44AD` (rich violet) |
| Accent | `#2C3E50` (twilight navy) |
| Elements | Dramatic fiery clouds, glowing city lights transition, amber waterfront glows |

### Night Theme

| Property | Value |
|----------|-------|
| Reference | photorealistic moon, twinkling starfields, illuminated urban grids |
| Primary | `#0B0F19` (midnight obsidian) |
| Secondary | `#1B263B` (deep indigo) |
| Accent | `#E0E6ED` (lunar silver) |
| Elements | Detailed moon phase, twinkling starfield particle layer, illuminated urban grid reflections |

---

## Palette as CSS Tokens

Central token file (`css/tokens.css`) defines `:root` defaults and theme overrides. All templates and component styles reference these tokens.

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

---

## Typography

| Role | Font Stack | Weight | Size | Usage |
|------|-----------|--------|------|-------|
| Time greeting | `var(--font-script)` | 400 | 2rem - 3rem | "Good Morning", "Good Evening" |
| Location name | `var(--font-metrics)` | 600 | 1.5rem | City, country |
| Temperature (hero) | `var(--font-metrics)` | 700 | 6rem - 8rem | Current temp |
| Labels / captions | `var(--font-metrics)` | 500 | 0.875rem | High/Low, feels like, wind |
| Glass card titles | `var(--font-metrics)` | 600 | 1rem | Hourly, forecast headers |

Google Fonts CDN import:

```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,600;1,400&display=swap" rel="stylesheet">
```

Fallback: if Google Fonts unavailable, `sans-serif` and `serif` system fonts must still render legibly.

---

## Glassmorphism Recipe

All UI panels, cards, and interactive surfaces follow this base recipe.

### Base Glass Panel

```css
.glass {
    background: rgba(255, 255, 255, 0.12);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: var(--glass-radius);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
}
```

### Glass Card (forecast tiles, metric cards)

```css
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
```

### Glass Input / Button

```css
.glass-input {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.14);
    border-radius: 10px;
    color: var(--color-text-primary);
    padding: 10px 14px;
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
    transition: background 0.2s ease, transform 0.1s ease;
}

.glass-button:hover {
    background: rgba(255, 255, 255, 0.25);
}

.glass-button:active {
    transform: scale(0.97);
}
```

---

## Dashboard Components

### 1. Header Section

- **Location name** — left-aligned, `var(--font-metrics)`, weight 600
- **Greeting** — `var(--font-script)`, "Good Morning" / "Good Afternoon" / "Good Evening", time-aware
- **Local time** — right-aligned or below location

### 2. Hero Weather Card

- **Temperature** — `var(--font-metrics)`, weight 700, extra-large (6-8rem)
- **Weather icon** — animated SVG or Canvas-based, theme-aware
- **High / Low range** — small, below temp, subtle opacity
- **Feels like** — secondary label

### 3. Liquid Visualizer Widget

- Interactive canvas element
- Wave intensity maps to wind speed (amplitude) and humidity (frequency)
- Color bands follow `var(--color-primary)` and `var(--color-secondary)` with low-opacity blends
- Smooth morphing transitions on data change (CSS `transition` or `requestAnimationFrame`)

### 4. Hourly Forecast Scroll

- Horizontal scroll container (snap scroll on mobile)
- `.glass-card` tiles
- Each tile: time, weather icon, temperature
- Micro-animation: stagger fade-in on load

### 5. 7-Day Forecast

- Vertical list of `.glass-card` rows
- Each row: day name, weather icon, high/low temp
- Expandable detail on tap (precipitation, wind, humidity)

---

## Implementation Rules

1. **Theme switching** — set `body` class to `.theme-morning`, `.theme-afternoon`, `.theme-evening`, or `.theme-night` based on server-rendered or client-side time-of-day. The CSS token swap handles all colors.
2. **No hardcoded colors** — use CSS custom properties from the token system. No inline `style="color: #FF7E00"` anywhere.
3. **Fonts load asynchronously** — use `font-display: swap` in `@font-face` declarations.
4. **Animations respect `prefers-reduced-motion`** — disable liquid morphing and micro-animations when set.
5. **Auth pages** (`login`, `signup`) — use the same glassmorphism recipe. The active theme applies globally.
6. **Mobile-first** — glass panels scale down gracefully. Snap scrolling for hourly forecast. Minimum touch target 44px.
