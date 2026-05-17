# appointment-rpc

Multi-tenant appointment scheduling for healthcare providers and other organizations, with gRPC for service-to-service integration.

## Stack

- Java 21, Spring Boot 3.4
- gRPC + Protocol Buffers
- PostgreSQL 17, Liquibase
- Docker, Kubernetes manifests

## Quick start

```bash
docker compose up -d postgres
./gradlew bootRun
```

- gRPC: `localhost:9090`
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/prometheus

Default profile `dev` loads seed tenants and providers.

### Example: get provider

```bash
grpcurl -plaintext \
  -H 'x-tenant-id: 11111111-1111-1111-1111-111111111111' \
  -import-path src/main/proto \
  -proto appointment/v1/appointment.proto \
  -d '{"provider_id":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"}' \
  localhost:9090 appointment.v1.AppointmentService/GetProvider
```

### Example: book appointment

Use a Monday 10:00 UTC slot within seeded availability (Mon–Fri 09:00–17:00):

```bash
grpcurl -plaintext \
  -H 'x-tenant-id: 11111111-1111-1111-1111-111111111111' \
  -import-path src/main/proto \
  -proto appointment/v1/appointment.proto \
  -d '{
    "provider_id":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "patient_ref":"patient-001",
    "start_time":{"seconds":1730116800},
    "duration_minutes":30
  }' \
  localhost:9090 appointment.v1.AppointmentService/BookAppointment
```

## Tests

```bash
./gradlew test
```

Uses Testcontainers for PostgreSQL.

## Docker

```bash
docker compose up --build
```

## Docs

- [SDS](docs/sds.md)
- [Execution plan](docs/execution-plan.md)
- [Product thoughts](docs/thoughts.md)
