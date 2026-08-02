# Design Team — Study Guide

This is the complete design study material for the Sky Track project. It explains the design system defined in `DESIGN.md` and how it is implemented in the codebase (`tokens.css`, `app.css`, templates).

## Your role

The design team owns the **visual language**: how Sky Track looks and feels. You define the themes, typography, glassmorphism recipe, and component patterns. The frontend team implements your design in templates and CSS; you review that it stays on-spec.

## What you own

| Area | Source of truth | Implementation | Read more |
|------|-----------------|----------------|-----------|
| Principles | `DESIGN.md` §1 | every frontend file | [01-principles](01-design-principles.md) |
| Themes | `DESIGN.md` §2 | `tokens.css` | [02-themes](02-theme-system.md) |
| Typography | `DESIGN.md` §Typography | `tokens.css` + `head.html` | [03-typography](03-typography.md) |
| Glassmorphism | `DESIGN.md` §Glass | `app.css` | [04-glass](04-glassmorphism.md) |
| Components | `DESIGN.md` §Dashboard | `index.html` + `app.css` | [05-components](05-dashboard-components.md) |
| Tough questions | — | — | [06-faq](06-faq.md) |

## How the design connects to the other teams

- **Backend team** picks the active theme: `GreetingService.themeClass(now)` puts `theme-morning/-afternoon/-evening/-night` on `<body>`. Your token system consumes that class.
- **Frontend team** implements your recipes in templates and `app.css`; `liquid.js` renders the liquid background using your tokens.
- **Testers team** visually checks the app follows the design and behaves correctly.
- **Documentation team** records the design system in the map.

## The four non-negotiable principles

1. **Glassmorphism-first** — no solid opaque panels.
2. **Liquid morphing background** — never static.
3. **Theme-driven palettes** — all colors from tokens, no hardcoded hex.
4. **Sticky consistency** — fonts, spacing, and micro-animations follow `DESIGN.md`.

---

Begin with [01-design-principles.md](01-design-principles.md).
