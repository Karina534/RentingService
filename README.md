# Renting Service: Monolith -> Microservices (Migration Base)

This repository is now prepared for a microservice runtime topology in Docker:

- `gateway` (`nginx`) on `http://localhost:8080`
- `user-service`
- `property-service`
- `communication-service`
- `notification-service`
- single Postgres image with multiple logical databases
- `kafka` for async events (RabbitMQ replaced)
- `mailhog` SMTP emulator for verification emails and notifications

## 1) Architecture

### Synchronous path

1. Client -> `gateway` (`nginx`)
2. Gateway routes by URL prefix:
   - `/api/v1/auth/**`, `/api/v1/users/**` -> `user-service`
   - `/api/v1/listings/**`, `/api/v1/rents/**`, `/api/v1/bookings/**`, `/api/v1/payments/**` -> `property-service`
   - `/api/v1/chats/**`, `/api/v1/internal/chats` -> `communication-service`

### Asynchronous path

1. Domain service publishes events to Kafka (for example: booking created/completed, rent created/closed).
2. `notification-service` consumes those events.
3. `notification-service` sends email via SMTP (`mailhog:1025`).
4. Open MailHog UI at `http://localhost:8025`.

## 2) Databases from One Postgres Image

Single container `postgres` initializes separate DBs using `docker/postgres/init/01-create-databases.sql`:

- `user_db`
- `property_db`
- `comm_db`
- `notification_db`

This keeps strict service data ownership while still using one Postgres image.

## 3) Kafka Instead of RabbitMQ

RabbitMQ is not used in this setup.

Kafka broker is started in KRaft mode and exposed to services via:

- `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`

Recommended topics:

- `booking-events`
- `rent-events`
- `user-events`

Recommended event style:

- key: aggregate id (`bookingId`, `rentId`, `userId`)
- value: JSON payload with `eventType`, `eventId`, `occurredAt`, `data`

## 4) Files Added/Changed

- `compose.yaml` - full microservice runtime
- `docker/nginx/nginx.conf` - API gateway routing
- `docker/postgres/init/01-create-databases.sql` - multi-DB init
- `Dockerfile` - container build for services
- `src/main/resources/application.yml` - env-driven runtime config
- `src/main/resources/config/application-user.yml`
- `src/main/resources/config/application-property.yml`
- `src/main/resources/config/application-communication.yml`
- `src/main/resources/config/application-notification.yml`

## 5) Run

```bash
docker compose up --build
```

Main entrypoints:

- Gateway: `http://localhost:8080`
- MailHog UI: `http://localhost:8025`
- Kafka broker: `localhost:9092`
- Postgres: `localhost:5433`

## 6) Important Migration Note

Current runtime is a migration base: infrastructure is already microservice-ready and endpoints are routed through gateway.

Next step is code boundary extraction (separate Maven modules/apps) so each service contains only its own domain code:

- `user-service`: auth/profile/email verification + JWT validation endpoint
- `property-service`: listings/rents/bookings/payments
- `communication-service`: chats/messages
- `notification-service`: Kafka consumers + SMTP sending + delivery logs

This phased approach avoids a risky big-bang rewrite and keeps your project runnable while splitting code safely.
