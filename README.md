# Taxol760

A ride-hailing backend built with Spring Boot, PostgreSQL, and Redis.

## Stack

- **Spring Boot** — REST API, JWT authentication, custom middleware filters
- **PostgreSQL** — persistent storage for users, drivers, vehicles, rides
- **Redis** — geo-spatial driver indexing (`GEOSEARCH`), occupied-driver tracking (Redis Set), idempotency key store, rate limit counters
- **Docker Compose** — single command boot

## Architecture Notes

Drivers push their location via `PUT /api/drivers/me/location`. Coordinates are stored in a Redis Geo Set. When a rider calls `GET /api/drivers/suggestions`, the API runs a `GEOSEARCH` against that set and filters out any driver IDs present in the `occupied-drivers` Redis Set — drivers currently in an active ride. This means availability filtering never touches PostgreSQL.

## Getting Started

```bash
cp .env.example .env
docker compose up -d
```

App runs on `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Demo Scripts

Full ride lifecycle — register, request, accept, start, complete:
```bash
node demo_lifecycle.js
```

Idempotency key protection and rate limiting filter:
```bash
./demo_middleware.sh
```

Geo-spatial search with occupied-drivers filter at scale:
```bash
node demo_geosearch.js
```

## Tests

```bash
mvn test
```

Covers: rate limiting filter, idempotency key filter, ride access control.

## Environment Variables

See `.env.example` — all values are filled in and ready to run as-is.

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection string |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `SPRING_DATA_REDIS_HOST` | Redis host |
| `SPRING_DATA_REDIS_PORT` | Redis port |
| `JWT_SECRET` | Signing secret |
| `JWT_EXPIRATION_MS` | Token expiry in ms (`-1` = never) |
