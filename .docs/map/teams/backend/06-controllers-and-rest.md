# 06 — Controllers and REST API

Controllers are the HTTP layer. They live in `sky-track-web/src/main/java/com/skywatch/weather/controller/`. There are two kinds:

- **MVC controllers** (`@Controller`) — return a view name; Thymeleaf renders an HTML page.
- **REST controllers** (`@RestController`) — return objects; Jackson serializes them to JSON.

Every protected controller resolves the current user through `CurrentUserResolver` and passes the `AppUser` into services.

## MVC controllers (pages)

### AuthController — `/login`, `/signup`

```java
@Controller
public class AuthController {

    private final SignupService signupService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final GreetingService greetingService;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String createAccount(
            @Valid @ModelAttribute SignupRequest signupRequest,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
            return "auth/signup";
        }
        try {
            signupService.createUser(signupRequest);
        } catch (EmailAlreadyRegisteredException exception) {
            bindingResult.rejectValue("email", "email.exists", "An account already exists for this email.");
            model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
            return "auth/signup";
        }
        // auto-login ...
        return "redirect:/onboarding";
    }
}
```

Key points:
- `@Valid` + `BindingResult` — Bean Validation runs; errors re-render the form with messages.
- `@ModelAttribute SignupRequest` — binds form fields (`firstName`, `lastName`, `email`, `password`) to the DTO.
- On success the user is auto-logged-in and redirected to `/onboarding`.

### HomeController — `GET /` (the dashboard)

```java
@GetMapping("/")
public String index(
        Authentication authentication,
        @RequestParam(required = false) String q,
        Model model
) {
    AppUser user = currentUserResolver.currentUser(authentication);
    model.addAttribute("greeting", greetingService.greeting(user, LocalTime.now()));
    model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
    model.addAttribute("firstName", user.getFirstName());
    model.addAttribute("alerts", weatherAlertRepository.findAllByTrackedCityUserIdOrderByDetectedAtDesc(user.getId()));

    List<TrackedCityView> cities = dashboardService.cityViews(user);
    model.addAttribute("cities", cities);
    model.addAttribute("primaryCityView", dashboardService.primaryCityView(user));
    // cityMessage, forecast, history, searchResults ...
    return "index";
}
```

Key points:
- Adds everything the template needs: greeting, theme, cities, primary city, message, forecast, history, search results.
- The optional `q` request parameter drives city search on the same page.
- Returns the view name `"index"` → `templates/index.html`.

### OnboardingController — `/onboarding`, `/onboarding/search`, `/onboarding/select`

```java
@PostMapping("/onboarding/search")
public String search(Authentication authentication, @RequestParam String query, Model model) {
    AppUser user = currentUserResolver.currentUser(authentication);
    List<GeoCityResponse> results = weatherService.searchCities(query, 5);
    model.addAttribute("greeting", greetingService.greeting(user, LocalTime.now()));
    model.addAttribute("results", results);
    model.addAttribute("query", query);
    return "onboarding";
}

@PostMapping("/onboarding/select")
public String select(
        Authentication authentication,
        @RequestParam String cityName,
        @RequestParam(required = false) String country,
        @RequestParam(required = false) String countryCode,
        @RequestParam double latitude,
        @RequestParam double longitude
) {
    AppUser user = currentUserResolver.currentUser(authentication);
    TrackedCityRequest request = new TrackedCityRequest(
            cityName, country, countryCode,
            BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    trackedCityService.savePrimaryCity(user, request);
    return "redirect:/";
}
```

### TrackedCityController — add / remove / set primary (form posts)

```java
@PostMapping("/cities")
public String add(Authentication authentication, @RequestParam String cityName, ...) {
    TrackedCityRequest request = new TrackedCityRequest(cityName, country, countryCode,
            BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    trackedCityService.addCity(currentUserResolver.currentUser(authentication), request);
    return "redirect:/";
}

@PostMapping("/cities/{id}/remove")
public String remove(Authentication authentication, @PathVariable Long id) {
    trackedCityService.removeCity(currentUserResolver.currentUser(authentication), id);
    return "redirect:/";
}

@PostMapping("/cities/{id}/primary")
public String setPrimary(Authentication authentication, @PathVariable Long id) {
    trackedCityService.setPrimary(currentUserResolver.currentUser(authentication), id);
    return "redirect:/";
}
```

These are classic PRG (Post/Redirect/Get) form handlers: mutate, then `redirect:/` so refreshing the page does not resubmit.

### CorridorController — `/corridor`

```java
@PostMapping("/corridor")
public String generate(Authentication authentication,
        @RequestParam String origin, @RequestParam String destination, Model model) {
    ...
    CorridorBundle bundle = buildCorridor(origin, destination);
    model.addAttribute("corridor", bundle);
    model.addAttribute("originQuery", origin);
    model.addAttribute("destinationQuery", destination);
    return "corridor";
}
```

`buildCorridor` geocodes both city names, then delegates to `CorridorService.generate`.

### AlertsController — `/alerts`

```java
@GetMapping("/alerts")
public String alerts(Authentication authentication, Model model) {
    AppUser user = currentUserResolver.currentUser(authentication);
    model.addAttribute("alerts", weatherAlertRepository.findAllByTrackedCityUserIdOrderByDetectedAtDesc(user.getId()));
    return "alerts";
}

@PostMapping("/alerts/{id}/acknowledge")
public String acknowledge(Authentication authentication, @PathVariable Long id) {
    AppUser user = currentUserResolver.currentUser(authentication);
    if (weatherAlertRepository.existsByIdAndTrackedCityUserId(id, user.getId())) {
        weatherAlertRepository.findById(id).ifPresent(alert -> {
            alert.setAcknowledged(true);
            weatherAlertRepository.save(alert);
        });
    }
    return "redirect:/alerts";
}
```

## REST controllers (JSON)

### CityApiController — `/api/cities`

```java
@RestController
@RequestMapping("/api/cities")
public class CityApiController {

    @GetMapping
    public List<TrackedCityView> list(Authentication authentication) {
        return dashboardService.cityViews(currentUserResolver.currentUser(authentication));
    }

    @GetMapping("/search")
    public List<GeoCityResponse> search(@RequestParam String q) {
        return weatherService.searchCities(q, 5);
    }

    @PostMapping
    public TrackedCityView add(Authentication authentication, @RequestBody TrackedCityRequest request) {
        var city = trackedCityService.addCity(currentUserResolver.currentUser(authentication), request);
        return dashboardService.cityViews(currentUserResolver.currentUser(authentication)).stream()
                .filter(view -> view.id().equals(city.getId())).findFirst().orElse(null);
    }

    @DeleteMapping("/{id}")
    public void remove(Authentication authentication, @PathVariable Long id) {
        trackedCityService.removeCity(currentUserResolver.currentUser(authentication), id);
    }

    @PostMapping("/{id}/primary")
    public void setPrimary(Authentication authentication, @PathVariable Long id) {
        trackedCityService.setPrimary(currentUserResolver.currentUser(authentication), id);
    }

    @PutMapping("/reorder")
    public void reorder(Authentication authentication, @RequestBody List<Long> cityIds) {
        trackedCityService.reorder(currentUserResolver.currentUser(authentication), cityIds);
    }
}
```

- `@RequestBody` binds the JSON body to a DTO (`TrackedCityRequest` for add, `List<Long>` for reorder).
- Return values are automatically serialized to JSON.

### WeatherApiController — `/api/weather/{cityId}/...`

```java
@GetMapping("/{cityId}/delta")
public ResponseEntity<DeltaView> delta(Authentication authentication, @PathVariable Long cityId) {
    AppUser user = currentUserResolver.currentUser(authentication);
    Optional<TrackedCity> city = trackedCityService.findCity(user, cityId);
    if (city.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    return deltaService.deltaFor(cityId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}

@GetMapping("/{cityId}/forecast")      // → ForecastBundle
@GetMapping("/{cityId}/snapshots")     // → List<SnapshotView>, ?days=7 default
```

`ResponseEntity` gives control over the HTTP status (200 vs 404).

### AlertApiController — `/api/alerts`

```java
@GetMapping                    // all alerts for the user's cities
@GetMapping("/{cityId}")       // alerts for one city
@PatchMapping("/{id}/acknowledge")  // mark acknowledged (ownership-checked)
```

## Full endpoint map

### Pages (HTML)

| Method | Path | Controller |
|--------|------|-----------|
| GET | `/login` | AuthController |
| GET | `/signup` | AuthController |
| POST | `/signup` | AuthController |
| GET | `/` | HomeController |
| GET | `/onboarding` | OnboardingController |
| POST | `/onboarding/search` | OnboardingController |
| POST | `/onboarding/select` | OnboardingController |
| POST | `/cities` | TrackedCityController |
| POST | `/cities/{id}/remove` | TrackedCityController |
| POST | `/cities/{id}/primary` | TrackedCityController |
| GET/POST | `/corridor` | CorridorController |
| GET | `/alerts` | AlertsController |
| POST | `/alerts/{id}/acknowledge` | AlertsController |

### REST (JSON)

| Method | Path | Controller |
|--------|------|-----------|
| GET | `/api/cities` | CityApiController |
| GET | `/api/cities/search?q=` | CityApiController |
| POST | `/api/cities` | CityApiController |
| DELETE | `/api/cities/{id}` | CityApiController |
| POST | `/api/cities/{id}/primary` | CityApiController |
| PUT | `/api/cities/reorder` | CityApiController |
| GET | `/api/weather/{id}/delta` | WeatherApiController |
| GET | `/api/weather/{id}/forecast` | WeatherApiController |
| GET | `/api/weather/{id}/snapshots?days=` | WeatherApiController |
| GET | `/api/alerts` | AlertApiController |
| GET | `/api/alerts/{cityId}` | AlertApiController |
| PATCH | `/api/alerts/{id}/acknowledge` | AlertApiController |

## Test your understanding

**Q1. What is the difference between `@Controller` and `@RestController`?**
**A.** `@Controller` returns a view name resolved to a Thymeleaf template; `@RestController` = `@Controller` + `@ResponseBody`, so the return value is serialized directly to JSON.

**Q2. Why do the form-handling controllers redirect after a POST?**
**A.** Post/Redirect/Get (PRG): the browser follows the 302 to a GET, so refreshing or going back does not resubmit the mutation. All `TrackedCityController` and onboarding writes redirect to `/`.

**Q3. What happens if a REST client requests `/api/weather/999/delta` for a city that is not theirs?**
**A.** `trackedCityService.findCity(user, 999L)` returns `Optional.empty()` (the query is scoped by the user's id), so the controller returns `404 Not Found` — the data is invisible and the wrong owner is never exposed.

**Q4. How does `HomeController` decide between showing a city message or the onboarding prompt?**
**A.** If `findPrimaryCity` returns a primary city, it builds the message (and forecast/history). If there is no primary city, it adds `null`/empty values, and the template shows the "Choose your primary city" state.

**Q5. Why does `CityApiController.add` re-query the list to return the view?**
**A.** `addCity` returns a `TrackedCity` entity; the API contract returns a `TrackedCityView` (with live weather + delta). Re-running `cityViews` and filtering by the new id produces the enriched view.

**Q6. What is the role of `Authentication` in controller methods?**
**A.** Spring injects the current security context. Controllers hand it to `CurrentUserResolver.currentUser(...)` to obtain the `AppUser` entity that services use to scope all queries.
