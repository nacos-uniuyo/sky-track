# 06 — Frontend Tough Questions (FAQ)

Cross-cutting questions spanning all the frontend material. Try to answer each before reading the answer.

## Q1. Explain the full path of a dashboard page from browser request to rendered screen.

**A.** Browser GET `/` → Spring Security filter chain (authenticated) → `HomeController.index` → resolves user, then `DashboardService` builds `cities`, `primaryCityView`, `forecast`, `history` → all added to the Thymeleaf `Model` → `templates/index.html` renders HTML with `th:text`/`th:each` → browser receives HTML + the CSS/JS assets → `liquid.js` starts the background, `weather-icons.js` renders icons, `history-chart.js` fetches snapshots, `dashboard.js` waits for taps.

## Q2. Why is there no frontend framework (React/Vue/Angular)?

**A.** The app is intentionally **server-rendered** (Thymeleaf) with vanilla JS enhancements. For this project's needs — a dashboard with a handful of interactive pieces — a framework would add build tooling and complexity without benefit. The design system is CSS-first, and interactions (switching, icons, chart) are small enough to hand-roll. This also keeps the whole app one deployable jar.

## Q3. How do the design tokens make theming almost free?

**A.** Every component color references `var(--color-...)`. The backend sets a theme class on `<body>` (`theme-morning` etc.), which overrides the variables. Because CSS variables cascade, the entire palette — panels, text, buttons, icons, the chart line, the background — updates without touching any component CSS.

## Q4. What is the difference between `th:replace` and `th:insert` for fragments?

**A.** `th:replace` replaces the host element entirely with the fragment (used here: `<head th:replace="~{fragments/head :: head(...)}">`). `th:insert` would place the fragment inside the host element. The project uses `replace` so the final HTML has no empty wrapper.

## Q5. Why are `defer` scripts used, and why does script order matter?

**A.** `defer` runs each script after the DOM is parsed, preserving source order. `weather-icons.js` must run before `dashboard.js` because it exposes `window.renderWeatherIcon` that `dashboard.js` calls when switching cities.

## Q6. How does the tap-to-switch work with zero network calls?

**A.** The server renders every city's data onto its card as `th:data-*` attributes. `dashboard.js` attaches a click handler to each `.city-card`, reads `card.dataset` on tap, and rewrites the hero elements' text/content. Only the `active` class moves and the icon re-renders. All data was already in the HTML.

## Q7. What is the `MutationObserver` in `liquid.js` watching and why?

**A.** It observes the `<body>` class attribute. If the theme class changes at runtime, it re-reads `--color-background-start/end` so the liquid gradient follows. Without it, the background would be stuck on the theme from page load.

## Q8. The signup form uses `th:field` — what does it generate and why does it matter?

**A.** `th:field="*{firstName}"` generates the input `name="firstName"` (matching the DTO), pre-fills the value, and integrates with validation so `th:errors` can display field errors after a failed submit. Without it, the form wouldn't bind to `SignupRequest`.

## Q9. How is XSS avoided in the templates?

**A.** All dynamic text uses `th:text`, which HTML-escapes the value (e.g. user input can't inject markup). The project never uses `th:utext` with user data. URLs go through `th:href`/`th:action`, which are encoded.

## Q10. Why does `history-chart.js` use `devicePixelRatio` in `resize()`?

**A.** To keep the chart crisp on high-DPI (Retina) screens. The canvas backing store is scaled by `dpr` while CSS size stays logical; without this, lines look blurry on hidpi displays.

## Q11. What happens if the user has no primary city?

**A.** `primaryCityView` is `null`, so the first hero section (`th:if="${primaryCityView}"`) is skipped and the second (`th:if="${primaryCityView == null}"`) renders the "Welcome to Sky Track / Choose your primary city" prompt. Forecast/history sections also guard on emptiness and simply don't render.

## Q12. How would you add a new page, say `/about`?

**A.**
1. Add a controller method in a (new or existing) `@Controller`: `@GetMapping("/about") public String about(Model model) { ...; return "about"; }` — it's automatically authenticated by `anyRequest().authenticated()`.
2. Create `templates/about.html` using the `head` fragment (and `topbar` if needed), following `DESIGN.md` classes.
3. If it needs data, add it to the model like other controllers do.
4. Add any new CSS to `app.css` using tokens; add JS to `static/js/` only if needed.

## Q13. Why is the search implemented as a GET to `/` with a `q` param instead of a REST call?

**A.** The dashboard is server-rendered, and search results are just another model attribute on the same page. GET with `?q=` keeps it shareable/bookmarkable and requires no client-side state. The REST search endpoint (`/api/cities/search`) exists separately for programmatic clients.

## Q14. What is the relationship between the hero `id` attributes and `dashboard.js`?

**A.** The hero elements have stable ids (`hero-icon`, `hero-city`, `hero-temp`, ...). `dashboard.js` captures them once into `heroFields` and updates their `textContent`/`innerHTML` on city switch. The ids are the contract between the template and the script.

## Q15. If the design team changes the night palette, what must change in code?

**A.** Only the values in `tokens.css` under `.theme-night` (and `DESIGN.md`). Because everything references the variables, no component CSS or template needs to change — the cascade handles the rest.
