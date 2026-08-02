# 06 — Design Tough Questions (FAQ)

Cross-cutting questions spanning the design system. Try to answer each before reading the answer.

## Q1. Why is the whole app built around glassmorphism?

**A.** It is the design identity: translucent frosted surfaces over a colorful liquid background create a premium, "alive" feel that matches the app's weather-tracking theme. It is also lightweight to implement with CSS tokens and scales consistently across every page.

## Q2. How does the app pick which theme to show, and where is that logic?

**A.** On the backend: `GreetingService.themeClass(LocalTime.now())` maps the current time to `theme-morning/-afternoon/-evening/-night`. Controllers add it to the model; templates render `<body th:class="${themeClass}">`. The CSS token cascade does the rest.

## Q3. Explain the full chain from "it's 2 AM" to a dark UI.

**A.** `LocalTime` is 02:00 → `themeClass` returns `theme-night` → `<body class="theme-night">` → `tokens.css` `.theme-night` overrides `--color-*` and `--color-background-start/end` → every `var()` consumer re-colors → `liquid.js`'s `MutationObserver` re-reads the background tokens and redraws the canvas with the night gradient.

## Q4. Why are colors stored as tokens and not as CSS classes like `.btn-primary`?

**A.** Tokens decouple the palette from the components. A component rule says "use `--color-primary`"; the theme decides what that value is. This gives free theming and single-point color changes, and keeps components theme-agnostic.

## Q5. What would violate principle 3 (theme-driven palettes)?

**A.** Any hardcoded hex in a template (`style="color:#FF7E00"`) or in component CSS outside `tokens.css`. The rule: colors exist only as tokens; everything else references them.

## Q6. Why does the design use two font families?

**A.** Separation of personality: `--font-metrics` (Inter) keeps data — temperatures, labels, captions — clean and legible; `--font-script` (Playfair Display) gives the greeting elegance. Mixing them gives the UI both readability and brand character.

## Q7. What is the relationship between the liquid background and the glass panels?

**A.** They depend on each other: the background provides the color that `backdrop-filter: blur()` frosts. Remove the liquid background and the glass looks flat; remove the glass and the background looks raw. Together they create depth.

## Q8. How would you make the dashboard responsive on a phone?

**A.** The design is mobile-first: the page column shrinks (max-width + flexible padding), carousels and hourly scrollers snap horizontally, the hero temperature uses `clamp()` to scale, the metric grid wraps, and touch targets stay ≥ 44px. No desktop-first assumptions.

## Q9. What is the delta badge and how is it colored?

**A.** It shows the temperature "shift since last check" (e.g. `+1.2 C`). CSS classes `.up` (green `#4ade80`) and `.down` (red `#f87171`) are applied by the template (and `dashboard.js` on switch) based on the sign of the delta.

## Q10. How are animations made accessible?

**A.** `prefers-reduced-motion` is honored in two places: `app.css` zeroes out transition/animation durations via the media query, and `liquid.js` skips its `requestAnimationFrame` loop (drawing one static frame instead).

## Q11. What happens to the auth pages under this design system?

**A.** They use the exact same glass recipe and the active theme applies globally (implementation rule 5) — so login/signup look like the rest of the app, not like separate pages.

## Q12. How would you change the accent color across the entire app?

**A.** Update `--color-accent` (and the theme values in `DESIGN.md`). Because everything references the token, one change re-themes buttons, links, and highlights everywhere.

## Q13. Why does the hero temperature use `clamp(4rem, 12vw, 7rem)` instead of a fixed size?

**A.** Fluid sizing: it scales with viewport width between 4rem and 7rem, so the headline number looks proportionate on small and large screens without media queries.

## Q14. What is the "+ Add city" card in the carousel?

**A.** A trailing glass card that links (`<a href="#search">`) to the search panel on the same page, letting users add cities without leaving the dashboard. It is an affordance, not a data card.

## Q15. How does the chart stay on-theme?

**A.** `history-chart.js` reads the current `--color-primary` from the body's computed style each draw, so the temperature line matches whatever theme is active.
