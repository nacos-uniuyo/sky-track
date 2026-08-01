# Skytrack Weather

Java 21 + Spring Boot 3 weather web app using PostgreSQL, Flyway, JPA, Validation, Spring Security, and temporary Thymeleaf pages.

## 1. Install Maven

Java 21 is already required. Install Maven if you do not have it:

```bash
sudo apt update
sudo apt install maven
```

Check it:

```bash
mvn -version
```

## 2. Start Local PostgreSQL

On Ubuntu/Linux:

```bash
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

Check that Postgres is accepting connections:

```bash
pg_isready
```

## 3. Create The App Database And User

Open the Postgres shell as the `postgres` admin user:

```bash
sudo -u postgres psql
```

Run these SQL commands:

```sql
CREATE DATABASE skytrack;
CREATE USER skytrack_user WITH PASSWORD 'skywatch99';
GRANT ALL PRIVILEGES ON DATABASE skytrack TO skytrack_user;
\c skytrack
GRANT ALL ON SCHEMA public TO skytrack_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO skytrack_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO skytrack_user;
\q
```

Test the connection:

```bash
psql -h localhost -U skytrack_user -d skytrack
```

Enter `skywatch99` when prompted.

## 4. Configure The App

The app already has local defaults in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/skytrack
spring.datasource.username=skytrack_user
spring.datasource.password=skywatch99
```

For real development, prefer environment variables so passwords do not live in code:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/skytrack
export DB_USERNAME=skytrack_user
export DB_PASSWORD=skywatch99
export OPENWEATHER_API_KEY=your_openweathermap_api_key
```

## 5. Run The App

```bash
mvn spring-boot:run
```

When the app starts, Flyway automatically runs:

```text
src/main/resources/db/migration/V1__create_saved_locations.sql
src/main/resources/db/migration/V2__create_app_users.sql
```

Then open:

```text
http://localhost:8080
```

## Project Layout

```text
src/main/java/com/skywatch/weather
  controller/      Web endpoints
  dto/             Form request objects
  model/           JPA entities
  repository/      Spring Data repositories
  service/         Business logic, weather API calls later

src/main/resources
  db/migration/    Flyway SQL migrations
  templates/       Temporary Thymeleaf frontend
  static/          CSS/assets
```

## Notes For The Frontend Developer

The current Thymeleaf page is only a placeholder:

```text
src/main/resources/templates/index.html
```

They can replace that file and add more templates/static assets without changing the backend/database setup.

## Authentication

The app has database-backed signup and login:

```text
/signup
/login
/logout
```

Passwords are hashed with BCrypt before being stored in the `app_users` table.
