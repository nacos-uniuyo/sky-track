# 07 — Infrastructure

Build, configuration, and deployment. This page explains the files that make Sky Track runnable and shippable.

---

## Maven multi-module build

### Root `pom.xml`
- **What:** The parent POM. Declares:
  - parent = `spring-boot-starter-parent` 3.3.5
  - `groupId` `com.skywatch`, `artifactId` `sky-track`
  - `packaging = pom`
  - modules: `sky-track-core`, `sky-track-web`
  - `<java.version>21</java.version>`
  - `dependencyManagement` for the two internal modules
- **Connections:** builds the two modules in reactor order.

### `sky-track-core/pom.xml`
- **Packaging:** jar.
- **Dependencies:**
  - `spring-web` (RestClient HTTP client)
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-security` (PasswordEncoder, UserDetails)
  - `spring-boot-starter-validation`
  - `flyway-core`, `flyway-database-postgresql`
  - `postgresql` (runtime)
  - `spring-boot-starter-test` (test)
- **Why no web starter here:** core has no web concerns; controllers live in web.

### `sky-track-web/pom.xml`
- **Packaging:** jar (the boot jar).
- **Dependencies:** `sky-track-core`, `spring-boot-starter-web`, `spring-boot-starter-thymeleaf`, `thymeleaf-extras-springsecurity6`.
- **Build plugin:** `spring-boot-maven-plugin` — produces the executable fat jar.

### Common commands

```bash
mvn install -DskipTests -pl sky-track-core -am   # install core to local repo (needed before running web standalone)
mvn spring-boot:run -pl sky-track-web            # dev server on http://localhost:8080
mvn package -pl sky-track-web -am                # build the boot jar
mvn test                                         # run tests in sky-track-core
```

> Note: `spring-boot:run -pl sky-track-web` resolves `sky-track-core` from `~/.m2`, so core must be installed first after changing it.

---

## Configuration (`sky-track-web/src/main/resources/`)

### `application.yml`
- `spring.application.name: sky-track`
- Datasource via env vars with defaults:
  - `DB_URL` → `jdbc:postgresql://localhost:5432/skytrack`
  - `DB_USERNAME` → `skytrack_user`
  - `DB_PASSWORD` → `skywatch99`
- JPA: `ddl-auto: validate`, `open-in-view: false`
- Flyway: enabled
- `weather.api.base-url: https://api.openweathermap.org`
- `weather.api.key` → `${OPENWEATHER_API_KEY:}`
- `weather.snapshot.fixed-rate-ms` → `${SNAPSHOT_FIXED_RATE_MS:1800000}` (30 min)

### `application-prod.yml`
- Active in Docker (set via `SPRING_PROFILES_ACTIVE=prod`).
- Disables SQL formatting.
- HikariCP pool sizing (`DB_POOL_SIZE`, default 10; minimum idle 2).
- Disables the whitelabel error page.

---

## Database (`db/migration/`)

Flyway SQL files `V1` → `V9`, applied in order on startup. Full details in [03-database-schema.md](03-database-schema.md). The rule: **never edit an applied migration** — add a new `V<n>__*.sql` instead.

---

## Docker

### `Dockerfile`
Single-stage production image:
```dockerfile
FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY pom.xml .
COPY sky-track-core/pom.xml sky-track-core/
COPY sky-track-web/pom.xml sky-track-web/
RUN mvn dependency:go-offline -B -pl sky-track-web -am
COPY . .
RUN mvn package -DskipTests -pl sky-track-web -am
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "sky-track-web/target/sky-track-web-0.0.1-SNAPSHOT.jar"]
```
- Copying the POMs before the source lets Maven cache dependencies.
- The runtime is a single image containing the fat jar; the app runs as the image's default user.

### `.dockerignore`
Excludes `.git`, `.agents`, `*.md`, `.env`, `.env.example`, compose files, IDE folders, and `target/` so they never bloat the build context.

### `docker-compose.yml`
Two services on a shared `app-net` network:

- **`db`** — `postgres:16-alpine`
  - env: `POSTGRES_DB=skytrack`, `POSTGRES_USER=skytrack_user`, `POSTGRES_PASSWORD=skywatch99`
  - named volume `pgdata` for persistence
  - healthcheck: `pg_isready`
  - `restart: unless-stopped`
- **`app`** — built from the Dockerfile
  - port `8080:8080`
  - env: `SPRING_PROFILES_ACTIVE=prod`, DB URL pointing at `db:5432`, `OPENWEATHER_API_KEY` from `.env`
  - healthcheck: HTTP GET `/`
  - `depends_on` db with `condition: service_healthy`
  - `restart: unless-stopped`

### `.env`
Local secrets file (gitignored): `OPENWEATHER_API_KEY=...`. `.env.example` is the committed template.

### Commands

```bash
docker compose up --build     # build and start the full stack
docker compose up -d db       # start only the database
docker compose down           # stop containers (keeps the volume)
docker compose down -v        # stop and remove the volume
```

---

## Security configuration summary

- Public: `/signup`, `/login`, `/css/**`, `/js/**`
- Authenticated: everything else
- Login field: `email`
- Passwords: BCrypt
- Session fixation: `changeSessionId` on login
- Logout: invalidates session + deletes `JSESSIONID`
- Static JS must be served from `/js/**` (public) or the dashboard scripts 302-redirect to login.

---

## Full-stack startup order

1. `docker compose up --build` starts `db`; healthcheck waits for Postgres.
2. `app` starts only after `db` is healthy.
3. Flyway applies migrations V1–V9.
4. Hibernate validates entities against the schema.
5. The embedded Tomcat serves on 8080.
6. The scheduler starts; after an initial delay it snapshots all cities every 30 minutes.

Next: [08-file-index.md](08-file-index.md)
