# 01 — Design Principles

The four principles in `DESIGN.md` are the constitution of the Sky Track UI. Every frontend change must comply — they are non-negotiable.

## Principle 1 — Glassmorphism-first

> All panels, cards, and UI surfaces use the glassmorphism recipe. No solid opaque panels.

**What it means:** every surface — the hero, search panel, city cards, forecast tiles, auth panels — is translucent frosted glass over the liquid background, never a solid box.

**Implementation:** the `.glass` and `.glass-card` classes in `app.css`:
```css
.glass {
    background: var(--color-surface);                 /* translucent white */
    backdrop-filter: blur(var(--glass-blur));          /* frost the background */
    border: 1px solid var(--color-surface-border);     /* subtle edge */
    border-radius: var(--glass-radius);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);        /* depth */
}
```

**How to check compliance:** if you see an opaque `background: #fff` panel, it violates the principle. Use the glass recipe instead.

## Principle 2 — Liquid morphing background

> The background responds to weather + time-of-day. Never static.

**What it means:** the page background is alive — a gradient that follows the theme and drifts with waves. It is drawn by `liquid.js` on a fixed canvas.

**Implementation:**
```js
// liquid.js reads the theme's gradient tokens
const style = getComputedStyle(document.body);
startColor = style.getPropertyValue('--color-background-start').trim();
endColor = style.getPropertyValue('--color-background-end').trim();
```
Three translucent wave layers are animated with `requestAnimationFrame`, giving the "liquid" motion behind the glass panels.

**How to check compliance:** a static background color alone would fail this principle. There must be the canvas layer and the gradient.

## Principle 3 — Theme-driven palettes

> All colors come from the token system. No hardcoded hex values in templates or component styles.

**What it means:** colors are referenced by name (`var(--color-primary)`) everywhere. The actual values live only in `tokens.css`. Changing a token re-themes the whole app.

**Implementation:** every component rule uses tokens:
```css
.glass-button:focus {
    border-color: var(--color-primary);
}
.weather-icon {
    color: var(--color-secondary);
}
```

**Forbidden pattern:**
```html
<div style="color: #FF7E00">   <!-- violates principle 3 -->
```

**How to check compliance:** grep for `#` hex codes outside `tokens.css`. Any hit is a violation.

## Principle 4 — Sticky consistency

> Fonts, spacing, and micro-animations follow this document. Do not deviate.

**What it means:** one system, applied everywhere. Same greeting font, same card radius, same hover animation — on every page, including auth.

**Implementation:**
- Fonts via `--font-metrics` / `--font-script` tokens.
- Radius/blur via `--glass-radius` / `--glass-blur`.
- Hover/active micro-animations defined once in `.glass-card`, `.glass-button`.

## How the principles work together

1. The **liquid background** (principle 2) is the world behind the glass.
2. The **glass surfaces** (principle 1) float over it.
3. The **token palette** (principle 3) colors both, switching with the theme.
4. **Consistency** (principle 4) keeps every page feeling like the same product.

## Test your understanding

**Q1. What makes a panel "glass" in this system?**
**A.** Translucent white fill (`--color-surface`), `backdrop-filter: blur(...)`, a subtle light border, rounded corners, and a soft drop shadow — defined once in `.glass`.

**Q2. Why can't a component use a hardcoded color like `#0f766e`?**
**A.** It would not follow the theme (breaking principle 3) and could drift from the design system. All colors must be tokens so the cascade and themes stay consistent.

**Q3. What happens to the background when the theme class changes?**
**A.** The `--color-background-start/end` tokens change; `liquid.js` observes the body class via a `MutationObserver` and re-reads them, so the gradient and waves follow the new theme.

**Q4. Are auth pages exempt from the glassmorphism requirement?**
**A.** No. Implementation rule 5 explicitly says auth pages use the same glass recipe and the active theme applies globally.

**Q5. What does "mobile-first" mean for this design?**
**A.** Panels scale down gracefully, hourly/carousel scrollers snap, and touch targets are at least 44px (implementation rule 6).
