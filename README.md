# RentingService

Spring Boot backend for a property rental platform (daily bookings and monthly rent leads).

## Stack

- Java 21, Spring Boot 4.0.6
- PostgreSQL + PostGIS, Flyway
- JWT authentication, SpringDoc OpenAPI

## Run locally

1. Start PostGIS: `docker compose up -d` (maps container port **5433** on the host)
2. Set `JWT_SECRET` (optional; default in `application.yml` for dev)
3. Run: `./mvnw spring-boot:run`
4. API base: `http://localhost:8080/api/v1`
5. Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`

## Tests

Integration tests use Testcontainers (`postgis/postgis:16-3.4`). Docker must be running:

```bash
./mvnw test
```

Without Docker, tests annotated with `@EnabledIfDockerAvailable` are skipped.

## Create listing (extended body)

`POST /listings` requires nested `daily` or `monthly` pricing matching `rentMode`:

```json
{
  "rentMode": "DAILY",
  "title": "Cozy studio",
  "address": "Nevsky 1",
  "houseType": "STUDIO",
  "latitude": 59.93,
  "longitude": 30.33,
  "daily": { "pricePerNight": 100, "minNights": 1 }
}
```

Email verification and payments are mocked (see application logs).
