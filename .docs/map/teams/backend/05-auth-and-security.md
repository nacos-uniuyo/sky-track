# 05 — Authentication and Security

Sky Track uses **Spring Security** for authentication and authorization. This page explains the security filter chain, login, signup auto-login, password hashing, and per-user data scoping.

## SecurityConfig — the filter chain

```java
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/signup", "/login", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId())
                .build();
    }
```

What each part does:

| Setting | Effect |
|---------|--------|
| `requestMatchers(...).permitAll()` | Public paths: signup, login, static CSS/JS |
| `anyRequest().authenticated()` | Everything else requires a logged-in session |
| `loginPage("/login")` | Custom login page (not the default) |
| `usernameParameter("email")` | Login field is `email`, not `username` |
| `defaultSuccessUrl("/", true)` | Redirect to the dashboard after login |
| `invalidateHttpSession(true)` | Clear the session on logout |
| `deleteCookies("JSESSIONID")` | Remove the session cookie on logout |
| `sessionFixation().changeSessionId()` | Prevent session fixation on login |

## The supporting beans

```java
@Bean
DaoAuthenticationProvider authenticationProvider(
        AppUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
}

@Bean
AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
}

@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

- `PasswordEncoder` → **BCrypt**, the standard secure hash.
- `DaoAuthenticationProvider` → ties the user database to Spring Security.
- `AuthenticationManager` → the entry point for programmatic login (used by signup auto-login).

## AppUserDetailsService — bridging the database

```java
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        AppUser appUser = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(appUser.getEmail())
                .password(appUser.getPasswordHash())
                .authorities(appUser.getRole())
                .disabled(!appUser.isEnabled())
                .build();
    }
}
```

- Spring Security calls `loadUserByUsername` during login (with the value of the `email` login field).
- The method converts the `AppUser` entity into a `UserDetails` object: username = email, password = the BCrypt hash, authorities = role, disabled = `!enabled`.
- `DaoAuthenticationProvider` compares the submitted password against the stored hash using `PasswordEncoder`.

## Signup + auto-login

`SignupService.createUser` hashes and saves the user. Then `AuthController.createAccount` logs them in immediately:

```java
Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(signupRequest.getEmail(), signupRequest.getPassword()));

SecurityContext context = SecurityContextHolder.createEmptyContext();
context.setAuthentication(authentication);
SecurityContextHolder.setContext(context);
securityContextRepository.saveContext(context, request, response);

return "redirect:/onboarding";
```

- `authenticationManager.authenticate(...)` verifies the just-created credentials (this works because the user is now in the DB).
- The authenticated `SecurityContext` is **saved to the session** via `securityContextRepository` (`HttpSessionSecurityContextRepository`). Without this, the next request would not see the login and would redirect to `/login`.
- The new user lands on `/onboarding` to choose their primary city.

## CurrentUserResolver — "who is logged in?"

Every protected controller needs the current user. `CurrentUserResolver` centralizes that:

```java
@Component
public class CurrentUserResolver {

    private final AppUserRepository appUserRepository;

    public AppUser currentUser(Authentication authentication) {
        return appUserRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
    }
}
```

- `authentication.getName()` returns the username = the email.
- The email is looked up in `app_users` and returned as a full `AppUser` entity.
- Controllers then pass this `AppUser` into services, which scope every query by `user.getId()`.

## Per-user data scoping

This is the security rule at the data layer:

```java
// TrackedCityRepository — every query carries userId
Optional<TrackedCity> findByUserIdAndId(Long userId, Long id);

// WeatherAlertRepository — ownership check in SQL
boolean existsByIdAndTrackedCityUserId(Long id, Long userId);
```

A user can only read/mutate **their own** tracked cities and alerts. The authenticated user's id is always resolved from the session, never trusted from the request body.

## Security headers (verified in production)

The Spring Security defaults add protection headers to every response:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Cache-Control: no-cache, no-store`
- `Set-Cookie: JSESSIONID=...; HttpOnly`

## Public vs protected paths

| Path | Access |
|------|--------|
| `/signup`, `/login` | Public |
| `/css/**`, `/js/**` | Public (static assets loaded by the login page too) |
| `/`, `/onboarding`, `/corridor`, `/alerts` | Authenticated |
| `/api/**` | Authenticated |

## Test your understanding

**Q1. What happens if a user accesses `/` while not logged in?**
**A.** Spring Security intercepts the request, stores it as a saved request, and redirects to `/login`. After a successful login the user is returned to their original destination (or `/` via `defaultSuccessUrl`).

**Q2. Why does the login page use `usernameParameter("email")`?**
**A.** The default parameter is `username`. The app stores login identifiers in the `email` column, so the form field and the filter chain must agree on the name `email`.

**Q3. Why is `loadUserByUsername` case-insensitive?**
**A.** It calls `findByEmailIgnoreCase`, so `USER@x.com` and `user@x.com` resolve to the same account. Combined with `SignupService` normalizing email to lowercase on registration, duplicate accounts are prevented.

**Q4. What is session fixation and how is it prevented?**
**A.** Session fixation is an attack where the attacker sets a victim's session id before login, then reuses it after authentication. `changeSessionId()` issues a new session id at login, invalidating any attacker-known id.

**Q5. Why is the security context explicitly saved after signup auto-login?**
**A.** Setting the context in `SecurityContextHolder` only affects the current thread/request. The `SecurityContextRepository.saveContext(...)` persists it into the session so the redirect to `/onboarding` arrives already authenticated.

**Q6. How does the app prevent a user from acknowledging someone else's alert?**
**A.** `AlertApiController` and `AlertsController` call `existsByIdAndTrackedCityUserId(id, user.getId())` before acknowledging. Only if the alert belongs to the current user does the request succeed.
