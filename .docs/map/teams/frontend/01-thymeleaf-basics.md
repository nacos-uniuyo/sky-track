# 01 — Thymeleaf Basics

Thymeleaf is the template engine that turns HTML skeletons into real pages. It runs on the server: the backend puts objects into a model, and Thymeleaf fills them into the HTML before it is sent to the browser.

## The namespace

Every template that uses Thymeleaf must declare the namespace on the `<html>` tag:

```html
<!doctype html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
```

Attributes that start with `th:` (e.g. `th:text`) are processed on the server and removed from the final HTML.

## Core expressions

### `${...}` — variable expressions (the most common)

Reads a value from the model, or a property of one:

```html
<p th:text="${greeting}">Hi comrade, Good morning</p>
<h2 th:text="${primaryCityView.cityName}">Lagos</h2>
```

The text inside the tag is the **natural template fallback** — what shows if the template is opened without the server.

### `@{...}` — link (URL) expressions

Builds context-aware URLs:

```html
<a th:href="@{/onboarding}">Choose your primary city</a>
<a th:href="@{/cities/{id}/remove(id=${city.id})}">Remove</a>
```

The second form inserts path variables: `/cities/5/remove`.

### `*{...}` — selection expressions

Works with `th:object` to bind a form to a DTO:

```html
<form method="post" th:action="@{/signup}" th:object="${signupRequest}">
    <input th:field="*{firstName}">
</form>
```

`*{firstName}` means "the `firstName` property of `signupRequest`". `th:field` binds the input both ways (value + name + validation styling).

## Conditionals

```html
<section th:if="${primaryCityView}" class="hero glass">...</section>
<section th:if="${primaryCityView == null}" class="hero glass">...</section>
<span th:unless="${city.primaryCity}" ...>Set primary</span>
```

- `th:if` renders the element only when the expression is true.
- `th:if="${x == null}"` checks absence.
- `th:unless` is the inverse.

## Loops — `th:each`

```html
<article th:each="city : ${cities}" class="glass-card city-card">
    <h4 th:text="${city.cityName}">Lagos</h4>
</article>

<li th:each="city : ${searchResults}" class="glass-card search-result">
    <span th:text="${city.name}">Lagos</span>
</li>
```

Each iteration creates one element per item. `city` is the current item.

## Attribute modifiers

- `th:text` — set the element's text (escaped, XSS-safe).
- `th:value` — set an input's value.
- `th:href` / `th:action` — set URLs.
- `th:classappend` — add to the existing class list.
- `th:attr` — set an arbitrary attribute.
- `th:field` — bind a form field.

```html
<span th:classappend="${city.primaryCity} ? ' active'">...</span>
<div th:attr="data-weather-icon=${hour.conditionCode}"></div>
```

## Data attributes for JavaScript

The dashboard embeds per-city data on cards so `dashboard.js` can read it without extra requests:

```html
<article th:each="city : ${cities}"
         class="glass-card city-card"
         th:data-name="${city.cityName}"
         th:data-temp="${city.temperature}"
         th:data-icon="${city.conditionCode}"
         th:data-delta="${city.temperatureDelta}">
```

Every `th:data-*` becomes an HTML `data-*` attribute with the rendered value. JavaScript reads them via `element.dataset`:

```js
card.dataset.name   // cityName
card.dataset.temp   // temperature
card.dataset.delta  // temperatureDelta
```

## Fragments — reusable chunks

Fragments let us reuse the same `<head>` and top bar across every page.

**Definition** (in `fragments/head.html`):
```html
<head th:fragment="head(title)">
    <title th:text="${title}">Sky Track</title>
    ...
</head>
```

**Use** (in any template):
```html
<head th:replace="~{fragments/head :: head('Sky Track')}"></head>
```

`th:replace` swaps the whole `<head>` for the fragment. The fragment can take parameters (here the page title).

The top bar fragment:
```html
<header th:fragment="topbar(greeting)" class="topbar glass">
    <p class="greeting" th:text="${greeting}">Hi comrade, Good morning</p>
</header>
```

Used by pages that need a top bar:
```html
<div th:replace="~{fragments/topbar :: topbar(${greeting})}"></div>
```

## Formatting helpers

```html
<!-- Concatenation -->
<p th:text="'Shift ' + ${primaryCityView.temperatureDelta} + ' C'">Shift +1.2 C</p>

<!-- Ternary inside an expression -->
<p th:text="${primaryCityView.temperature} != null ? ${primaryCityView.temperature} + ' C' : '--'">28 C</p>

<!-- Arithmetic -->
<p th:text="${day.precipitation} * 100 + '%'">20%</p>
```

## Escaping / security

- Always use `th:text` (escapes HTML) for user data. Never `th:utext` with user input — that is an XSS hole.
- URLs via `th:href`/`th:action` are auto-encoded.

## Test your understanding

**Q1. What is the difference between `th:text` and just writing text inside the tag?**
**A.** Plain text is the natural-template fallback (shown if opened statically). `th:text` replaces that fallback with the rendered server value, and it HTML-escapes the output.

**Q2. Why do the city cards use `th:data-*` attributes instead of the JavaScript making API calls?**
**A.** The data is already available on the server when the page is rendered. Embedding it as attributes means `dashboard.js` can switch cities instantly with zero network requests, keeping the interaction snappy.

**Q3. How does `th:classappend` differ from `th:class`?**
**A.** `th:class` replaces the entire class attribute; `th:classappend` adds to the existing classes. The city card already has `glass-card city-card`, and `th:classappend="${city.primaryCity} ? ' active'"` appends `active` only for the primary city.

**Q4. What happens if `th:if="${primaryCityView}"` is false?**
**A.** The element is not rendered at all (removed from output). That is why the page has two hero sections: one for when a primary city exists, one for the "welcome" empty state.

**Q5. Why is the `<head>` a fragment?**
**A.** Every page needs the same fonts, `tokens.css`, `app.css`, and `liquid.js`. A single fragment keeps them consistent — one change updates every page.

**Q6. What does `th:object` + `*{...}` + `th:field` accomplish in the signup form?**
**A.** It binds the form to the `signupRequest` DTO: `th:field` sets the input's `name` from the property, pre-fills the value on error re-render, and integrates with validation so `th:errors` can show messages.
