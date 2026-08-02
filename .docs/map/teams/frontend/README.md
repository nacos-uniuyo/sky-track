# Frontend Team — Study Guide

This is the complete frontend study material for the Sky Track project. Everything visual — Thymeleaf templates, fragments, CSS, and JavaScript — is explained from scratch with real code snippets, data flows, and tough questions with answers.

## Your role

The frontend team owns everything the user sees and interacts with: pages, layouts, styling, and client-side behavior. You consume the data the backend team provides through controllers and REST endpoints, and you follow the design system defined by the design team.

## What you own

| Area | Files | Read more |
|------|-------|-----------|
| Templating | Thymeleaf basics: expressions, fragments | [01-thymeleaf](01-thymeleaf-basics.md) |
| Pages | 7 templates + 2 fragments | [02-templates](02-templates-map.md) |
| Behavior | 5 JavaScript files | [03-javascript](03-javascript.md) |
| Styling | `tokens.css` + `app.css` | [04-css](04-css-and-design-tokens.md) |
| Data flow | How controller data reaches the DOM | [05-data-flow](05-data-flow-to-frontend.md) |
| Tough questions | Interview-style quiz | [06-faq](06-faq.md) |

## How the frontend connects to the other teams

- **Backend team** provides the data: MVC controllers add objects to the Thymeleaf model (`th:${...}`), and REST endpoints return JSON that our JavaScript fetches.
- **Design team** defines `DESIGN.md`; the CSS tokens and glassmorphism recipe implement it.
- **Testers team** manually verifies the flows we render (signup, onboarding, dashboard, corridor, alerts).
- **Documentation team** records our pages in the map's `06-frontend-map.md`.

## Architecture rule you must know

```
Backend controller → adds data to Thymeleaf model → template renders HTML
                                                  → CSS styles it
                                                  → JS adds behavior (background, icons, switching, chart)
```

- Pages are **server-rendered** — the HTML is produced on the server, not by a JavaScript framework.
- JavaScript is **progressive enhancement** on top of that HTML.
- The `<body>` class carries the theme (`theme-morning`, etc.) chosen by the backend.

## Non-negotiable project rules

- App name is **Sky Track** (never "Sky Watch" / "Skywatch").
- No comments, no emojis in code or UI.
- Follow `DESIGN.md` exactly — no off-spec colors, fonts, or layouts.
- Use CSS tokens, never hardcoded colors inline.
- `prefers-reduced-motion` must disable animations.

---

Begin with [01-thymeleaf-basics.md](01-thymeleaf-basics.md).
