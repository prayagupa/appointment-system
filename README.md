# appointment-rpc

Multi-tenant appointment scheduling for healthcare providers and other organizations, with gRPC for service-to-service integration and a web UI for booking.

**Package:** `com.pratyabhi`

## Stack

- Java 21, Spring Boot 3.4
- gRPC + Protocol Buffers
- REST API + web UI (static)
- PostgreSQL 17, Liquibase
- Docker, Kubernetes manifests

## Quick start

```bash
docker compose up -d postgres
./gradlew bootRun
```

| Endpoint | URL |
|----------|-----|
| **Web UI** | http://localhost:8080/ |
| gRPC | localhost:9090 |
| Health | http://localhost:8080/actuator/health |
| Metrics | http://localhost:8080/actuator/prometheus |

Default profile `dev` loads seed tenants and providers.

### Web UI

1. Open http://localhost:8080/
2. Select **City Health Clinic** (or another tenant)
3. Select a provider, pick a date and slot, book an appointment
4. View and cancel appointments in the list panel

### gRPC example: get provider

```bash
grpcurl -plaintext \
  -H 'x-tenant-id: 11111111-1111-1111-1111-111111111111' \
  -import-path src/main/proto \
  -proto appointment/v1/appointment.proto \
  -d '{"provider_id":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"}' \
  localhost:9090 appointment.v1.AppointmentService/GetProvider
```

## REST API (for UI / integrations)

All routes except `GET /api/tenants` require header `X-Tenant-Id: <tenant-uuid>`.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/tenants` | List tenants |
| GET | `/api/providers` | List providers |
| GET | `/api/availability` | Query slots (`providerId`, `from`, `to`, `slotMinutes`) |
| GET | `/api/appointments` | List appointments (`providerId` optional) |
| POST | `/api/appointments` | Book appointment |
| POST | `/api/appointments/{id}/cancel` | Cancel appointment |

## Tests

```bash
./gradlew test
```

## Docker

```bash
docker compose up --build
```

UI: http://localhost:8080/

## Docs

- [SDS](docs/sds.md)
- [Execution plan](docs/execution-plan.md)
- [Runbook](docs/runbook.md)
- [Product thoughts](docs/thoughts.md)
