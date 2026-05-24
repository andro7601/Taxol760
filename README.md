# Taxol760

> Ride-hailing backend — drivers, riders, real-time tracking, live notifications.

Built with Java 17 and Spring Boot. Drivers connect via WebSocket for live ride requests and location updates. Riders get nearest driver suggestions based on GPS coordinates.

---

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot |
| Database | PostgreSQL 16 |
| Cache & Geo | Redis |
| Real-time | WebSocket |
| Auth | JWT + Spring Security |
| Infrastructure | Docker Compose |

---

## Getting started

```bash
cp .env.example .env
# fill in your values
docker compose up
```

App runs on `http://localhost:8080`

> Seeded test accounts and JWT tokens are printed to logs on startup
> ```bash
> docker compose logs taxol760 | grep seed
> ```

---

## API docs

Interactive Swagger docs available at:

```
http://localhost:8080/swagger-ui/index.html
```

Click **Authorize ** and paste a JWT to test authenticated endpoints.

---

## WebSocket

Drivers connect for real-time communication:

```
ws://localhost:8080/ws/rides?token=<JWT>
```

**Send location update:**
```json
{
  "type": "LOCATION_UPDATE",
  "longitude": 44.827096,
  "latitude": 41.694111
}
```

**Server pushes ride request when rider books:**
```json
{
  "type": "RIDE_REQUEST",
  "rideId": 1,
  "pickupLat": 41.69,
  "pickupLon": 44.82,
  "dropoffLat": 41.71,
  "dropoffLon": 44.85
}
```

---

## Environment variables

See `.env.example` for all required variables.

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection string |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `SPRING_DATA_REDIS_HOST` | Redis host |
| `SPRING_DATA_REDIS_PORT` | Redis port |
| `JWT_SECRET` | Signing secret |
| `JWT_EXPIRATION_MS` | Token expiry in ms |

---

## Features

- JWT auth with custom Spring Security filter chain
- Role based access — `USER` `DRIVER` `ADMIN`
- Redis GEO commands for driver location and nearest-driver suggestions
- WebSocket for real-time ride requests and location broadcasting
- Rate limiting and idempotency middleware via Redis
- Ride lifecycle — request → accept → start → complete → cancel
- Docker Compose with health checks and ordered service startup
