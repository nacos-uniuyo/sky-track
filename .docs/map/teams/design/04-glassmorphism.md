# 04 — Glassmorphism

Glassmorphism is the core visual technique of Sky Track: translucent frosted surfaces floating over a colorful background. This page explains the recipe and its variants.

## The recipe (from DESIGN.md)

```css
.glass {
    background: var(--color-surface);                    /* translucent fill */
    backdrop-filter: blur(var(--glass-blur));             /* frosts the background */
    -webkit-backdrop-filter: blur(var(--glass-blur));     /* Safari support */
    border: 1px solid var(--color-surface-border);        /* subtle light edge */
    border-radius: var(--glass-radius);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);           /* soft depth shadow */
}
```

## The three ingredients

| Ingredient | What it does |
|------------|--------------|
| **Translucent fill** | `rgba(255,255,255,0.12)` — a hint of white so the background glows through |
| **Backdrop blur** | `blur(20px)` — frosts whatever is behind the surface |
| **Light border + shadow** | A 1px light edge and a soft shadow give the panel definition and depth |

## Variants

### `.glass-card` — smaller tiles (city cards, forecast tiles)

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

Lighter fill, less blur, tighter radius — and a gentle **hover lift** (translateY + brighter fill) for interactivity.

### `.glass-input` — form fields

```css
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
```

Focus is signaled by the primary-colored border + a soft glow.

### `.glass-button`

```css
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

.glass-button:hover  { background: rgba(255, 255, 255, 0.25); }
.glass-button:active { transform: scale(0.97); }
```

- Hover brightens the fill.
- Active presses down (`scale(0.97)`) for tactile feedback.
- `.glass-button.danger` uses a red-tinted fill for destructive actions.

## The layer stack

```
┌─────────────────────────────────────────────┐
│  liquid.js canvas  (theme gradient + waves) │  ← background world
├─────────────────────────────────────────────┤
│  glass panels/cards  (backdrop-filter)      │  ← frosted UI
│  glass inputs/buttons                       │
└─────────────────────────────────────────────┘
```

The glass effect is only visible because there is something colorful (the liquid background) behind it to blur. This is why principles 1 and 2 are paired.

## Why `-webkit-` prefix?

`backdrop-filter` is a relatively new CSS feature. The `-webkit-` prefixed version is required for older Safari/WebKit browsers; the standard property covers modern Chrome/Firefox/Edge.

## Test your understanding

**Q1. Why does glassmorphism need a colorful background behind it?**
**A.** `backdrop-filter: blur()` blurs what is behind the element. With nothing colorful behind, the panels would look flat gray — the liquid background is what makes the frost effect meaningful.

**Q2. What is the difference between `.glass` and `.glass-card`?**
**A.** `.glass` is the full panel (more fill/blur, larger radius, bigger shadow). `.glass-card` is the lighter tile variant with a hover lift. Both follow the same recipe at different intensities.

**Q3. How is focus communicated on a glass input?**
**A.** The border becomes `var(--color-primary)` and a soft `box-shadow` glow appears — replacing the browser's default outline.

**Q4. Why the `-webkit-backdrop-filter` line?**
**A.** Backwards/sideways compatibility: older WebKit (Safari) browsers need the prefixed property for the blur to work.

**Q5. What role does the shadow play in glassmorphism?**
**A.** The soft `0 8px 32px rgba(0,0,0,0.18)` shadow separates the panel from the busy background, giving it depth and a defined edge so it reads as a distinct surface.
