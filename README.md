# Sky Track

Java 21 + Spring Boot 3.3.5 weather tracking app using PostgreSQL, Flyway, JPA, Spring Security, and Thymeleaf. A glassmorphism dashboard with a liquid animated background that adapts to time of day.

## Features

- Signup / login with BCrypt password hashing (email login)
- Onboarding: choose a primary city, add tracked cities to a favorites carousel
- Live current conditions, hourly and 7-day forecasts from OpenWeatherMap
- Weather delta tracking ("Shift since last check") via scheduled snapshots
- Historical playback chart with hover scrubber
- Severe weather anomaly alerts (pressure drop, wind surge) with acknowledge
- Travel weather corridor: weather timeline between origin and destination
- Time-of-day themes and liquid background per DESIGN.md
- REST API for cities, weather, delta, snapshots, and alerts

## Quick Start (Docker)

```bash
cp .env.example .env
docker compose up --build
```

The app will be available at http://localhost:8080.

The command starts a PostgreSQL 16 database and the Spring Boot application. Flyway runs migrations automatically on first startup. Set your OpenWeatherMap API key in `.env` before starting.

## Local Development (without Docker)

### Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 16 (running locally)

### Setup

```sql
CREATE DATABASE skytrack;
CREATE USER skytrack_user WITH PASSWORD 'skywatch99';
GRANT ALL PRIVILEGES ON DATABASE skytrack TO skytrack_user;
\c skytrack
GRANT ALL ON SCHEMA public TO skytrack_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO skytrack_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO skytrack_user;
```

### Run

```bash
mvn install -DskipTests -pl sky-track-core -am
export DB_URL=jdbc:postgresql://localhost:5432/skytrack
export DB_USERNAME=skytrack_user
export DB_PASSWORD=skywatch99
export OPENWEATHER_API_KEY=your_openweathermap_api_key
mvn spring-boot:run -pl sky-track-web
```

Run `mvn install -pl sky-track-core` first whenever core code changes; `spring-boot:run` resolves the core module from the local Maven repository.

## Project Layout

```
sky-track/                          parent POM (dependency management)
├── sky-track-core/                 domain logic (model, repository, service, dto)
│   └── src/main/java/com/skywatch/weather/
│       ├── model/                  JPA entities
│       ├── repository/             Spring Data repositories
│       ├── service/                business logic
│       └── dto/                    request/response objects
├── sky-track-web/                  web layer (controllers, config, templates)
│   ├── src/main/java/com/skywatch/weather/
│   │   ├── controller/             web endpoints
│   │   ├── config/                 security configuration
│   │   └── SkywatchWeatherApplication.java
│   └── src/main/resources/
│       ├── application.yml         base configuration
│       ├── application-prod.yml    production profile
│       ├── db/migration/           Flyway SQL migrations
│       ├── static/                 CSS and assets
│       └── templates/              Thymeleaf views
├── Dockerfile                      production image
├── docker-compose.yml              database + application services
└── .env.example                    environment variable template
```

## Authentication

```text
/signup
/login
/logout
```

Passwords are hashed with BCrypt. Form login uses `email` as the username parameter.

## Configuration

| Profile | Description |
|---------|-------------|
| default | Local PostgreSQL, used with `mvn spring-boot:run` |
| `prod` | Production settings, used in Docker Compose |
