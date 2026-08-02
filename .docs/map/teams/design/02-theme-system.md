# 02 — Theme System

Sky Track has four visual states mapped to time of day. The active theme is a class on the `<body>` element; the CSS token system handles all the colors.

## The four themes

| Theme | Class | Time of day | Primary | Secondary | Accent |
|-------|-------|-------------|---------|-----------|--------|
| Morning | `.theme-morning` | 05:00–11:59 | `#FF7E00` amber | `#FFD700` gold | `#4A90E2` blue |
| Afternoon | `.theme-afternoon` | 12:00–16:59 | `#1E88E5` sky blue | `#FFC107` gold | `#FFFFFF` white |
| Evening | `.theme-evening` | 17:00–20:59 | `#D35400` burnt orange | `#8E44AD` violet | `#2C3E50` navy |
| Night | `.theme-night` | 21:00–04:59 | `#0B0F19` obsidian | `#1B263B` indigo | `#E0E6ED` silver |

## How the theme is chosen (backend side)

`GreetingService.themeClass(LocalTime.now())` maps the current time to a class:

```java
public String themeClass(LocalTime now) {
    if (now.isAfter(LocalTime.of(21, 0)) || now.isBefore(LocalTime.of(5, 0))) return "theme-night";
    if (now.isBefore(LocalTime.of(12, 0))) return "theme-morning";
    if (now.isBefore(LocalTime.of(17, 0))) return "theme-afternoon";
    return "theme-evening";
}
```

The controller puts it on the model, and the template applies it:

```html
<body th:class="${themeClass}">
```

## How the tokens implement it (`tokens.css`)

The `:root` block defines defaults; each `.theme-*` block overrides them:

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

.theme-night {
    --color-primary: #0B0F19;
    --color-secondary: #1B263B;
    --color-accent: #E0E6ED;
    --color-background-start: #060B14;
    --color-background-end: #0D1520;
}
```

**The cascade is the whole trick:** setting `.theme-night` on `<body>` changes the variables for the entire document tree. Every rule using `var(--color-primary)` — panels, buttons, the chart line, icons — updates instantly. No per-theme component CSS exists.

## The background gradient tokens

Each theme also defines `--color-background-start` and `--color-background-end`:

| Theme | Start | End |
|-------|-------|-----|
| Morning | `#0D1B2A` | `#1B2838` |
| Afternoon | `#1565C0` | `#42A5F5` |
| Evening | `#1A0A2E` | `#3D1E6D` |
| Night | `#060B14` | `#0D1520` |

These feed both the body background gradient (in `app.css`) and the liquid canvas (`liquid.js`).

## Visual intent of each theme

| Theme | Mood | Design references |
|-------|------|-------------------|
| Morning | Soft, warm, hopeful | Golden-hour sunbursts, rolling silhouettes |
| Afternoon | Bright, crisp, energetic | Radiant blue sky, shimmering reflections |
| Evening | Dramatic, warm | Fiery clouds, glowing city lights |
| Night | Deep, calm, luminous | Photorealistic moon, starfields |

## Test your understanding

**Q1. What exactly changes when the theme class changes?**
**A.** Only the CSS custom properties in `tokens.css`. Because every component references `var(--color-*)`, the whole palette and the background gradient follow automatically.

**Q2. Where is the theme class decided?**
**A.** On the backend: `GreetingService.themeClass(LocalTime.now())` → added to the Thymeleaf model → `<body th:class="${themeClass}">`.

**Q3. What are the two tokens that drive the background, and who reads them?**
**A.** `--color-background-start` and `--color-background-end`. The body gradient in `app.css` reads them for the page background, and `liquid.js` reads them for the animated canvas.

**Q4. Why is `:root` needed if every theme overrides the colors?**
**A.** `:root` provides defaults (used before a theme is set and for tokens like fonts/glass that do not change). It also guarantees valid values even if a theme class is missing.

**Q5. How would you add a new theme, say "storm" for severe weather?**
**A.** Add a `.theme-storm { ... }` block in `tokens.css` with its palette + background tokens, then have the backend (or a future weather-driven hook) set the `theme-storm` class on `<body>`. No component CSS changes needed.
