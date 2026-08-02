# 02 — Repositories

Repositories are Spring Data JPA interfaces. You declare a method name; Spring generates the SQL implementation at runtime. They live in `sky-track-core/src/main/java/com/skywatch/weather/repository/`.

## What a Spring Data repository is

```java
public interface TrackedCityRepository extends JpaRepository<TrackedCity, Long> {
    List<TrackedCity> findByUserIdOrderBySortOrderAscIdAsc(Long userId);
}
```

- `JpaRepository<TrackedCity, Long>` gives CRUD (`save`, `findById`, `findAll`, `delete`, ...) plus `Long` is the id type.
- `findByUserIdOrderBySortOrderAscIdAsc` is a **derived query**: Spring parses the name into `SELECT ... FROM tracked_cities WHERE user_id = ? ORDER BY sort_order ASC, id ASC`.
- You never write the implementation.

## The five repositories

### `AppUserRepository`

```java
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
```

- `findByEmailIgnoreCase` — finds a user by email, case-insensitive. Used by login and by `CurrentUserResolver`.
- `existsByEmailIgnoreCase` — duplicate check during signup.

### `TrackedCityRepository`

```java
public interface TrackedCityRepository extends JpaRepository<TrackedCity, Long> {

    List<TrackedCity> findByUserIdOrderBySortOrderAscIdAsc(Long userId);

    Optional<TrackedCity> findByUserIdAndId(Long userId, Long id);

    Optional<TrackedCity> findByUserIdAndPrimaryCityTrue(Long userId);

    Optional<TrackedCity> findFirstByUserIdAndPrimaryCityTrueOrderByCreatedAtAsc(Long userId);

    boolean existsByUserIdAndPrimaryCityTrue(Long userId);

    long countByUserId(Long userId);
}
```

- Every query is **scoped by `userId`** — this is the per-user security rule.
- `findByUserIdAndPrimaryCityTrue` — the current primary city.
- `findFirstBy...OrderByCreatedAtAsc` — a single result.
- `existsBy...` and `countBy...` — cheap checks used to decide defaults.

### `WeatherSnapshotRepository`

```java
public interface WeatherSnapshotRepository extends JpaRepository<WeatherSnapshot, Long> {

    Optional<WeatherSnapshot> findFirstByTrackedCityIdOrderByRecordedAtDesc(Long trackedCityId);

    Optional<WeatherSnapshot> findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(
            Long trackedCityId, OffsetDateTime before);

    List<WeatherSnapshot> findByTrackedCityIdOrderByRecordedAtAsc(Long trackedCityId);

    List<WeatherSnapshot> findByTrackedCityIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long trackedCityId, OffsetDateTime from, OffsetDateTime to);
}
```

- `findFirstBy...OrderByRecordedAtDesc` — the **latest** snapshot.
- `findFirstBy...RecordedAtBefore...Desc` — the latest snapshot **before** a given time (used for the delta baseline).
- `...RecordedAtBetween...Asc` — a time range for the history chart.

### `WeatherAlertRepository`

```java
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {

    List<WeatherAlert> findByTrackedCityIdOrderByDetectedAtDesc(Long trackedCityId);

    List<WeatherAlert> findAllByTrackedCityUserIdOrderByDetectedAtDesc(Long userId);

    boolean existsByIdAndTrackedCityUserId(Long id, Long userId);
}
```

- `findAllByTrackedCityUserId...` — property traversal: `alert.trackedCity.user.id == userId`. This resolves the relationship **in SQL**, not in Java — avoiding lazy-loading problems.
- `existsByIdAndTrackedCityUserId` — ownership check for acknowledge (see the note below).

### `CorridorRouteRepository`

```java
public interface CorridorRouteRepository extends JpaRepository<CorridorRoute, Long> {

    List<CorridorRoute> findByUserIdOrderByCreatedAtDesc(Long userId);
}
```

## Important: the LazyInitializationException trap

Because `open-in-view: false`, lazy relationships are **not** loaded outside a transaction. A naive ownership check like this will crash:

```java
// CRASHES: alert.getTrackedCity() is a lazy proxy with no open session
alert.getTrackedCity().getUser().getId().equals(userId);
```

The fix is a **repository ownership query** — push the traversal into SQL:

```java
boolean existsByIdAndTrackedCityUserId(Long id, Long userId);
```

Spring generates `WHERE id = ? AND tracked_city.user_id = ?` — no proxies involved. This is exactly why `WeatherAlertRepository` has `existsByIdAndTrackedCityUserId`, and why `AlertsController` / `AlertApiController` use it before acknowledging.

## Method name cheat sheet

| Name fragment | Generated SQL |
|---------------|---------------|
| `findByX` | `WHERE x = ?` |
| `findFirstByXOrderByYDesc` | `ORDER BY y DESC LIMIT 1` |
| `existsByX` | `SELECT EXISTS(...)` |
| `countByX` | `SELECT COUNT(*)` |
| `And` | `AND` |
| `OrderByA asc/desc` | `ORDER BY a` |
| Property traversal `TrackedCityUserId` | joins through `tracked_city.user_id` |

## Test your understanding

**Q1. How does Spring know the SQL for `findByUserIdOrderBySortOrderAscIdAsc` without a `@Query`?**
**A.** Spring Data parses the method name into subject + predicate. It builds `WHERE` from `UserId`, `ORDER BY` from `SortOrderAsc` + `IdAsc`, and runs the query on the repository's entity type (`TrackedCity`).

**Q2. Why does every city query take `userId`?**
**A.** Data is scoped per user. Passing the authenticated user's id in every query prevents a user from seeing or mutating another user's cities (IDOR protection).

**Q3. What does `existsByIdAndTrackedCityUserId` solve that `findById().get().getTrackedCity().getUser().getId()` does not?**
**A.** It runs entirely in SQL (`WHERE id = ? AND tracked_city.user_id = ?`), so it never touches a lazy proxy. The Java navigation would throw `LazyInitializationException` under `open-in-view: false`.

**Q4. If you needed the 3 most recent snapshots for a city, what method would you add?**
**A.** `List<WeatherSnapshot> findTop3ByTrackedCityIdOrderByRecordedAtDesc(Long trackedCityId);` — `Top3` limits results, `OrderByRecordedAtDesc` sorts newest first.

**Q5. What does `findFirstByUserIdAndPrimaryCityTrueOrderByCreatedAtAsc` return if the user has no primary city?**
**A.** An empty `Optional<TrackedCity>` — callers must handle absence (e.g. `DashboardService.primaryCityView` returns `null`, and the template shows the "choose a primary city" state).
