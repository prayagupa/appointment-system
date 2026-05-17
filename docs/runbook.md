# Operations Runbook

## Health checks

- Liveness: `GET /actuator/health/liveness`
- Readiness: `GET /actuator/health/readiness`
- Metrics: `GET /actuator/prometheus`

## Ports

| Port | Purpose |
|------|---------|
| 9090 | gRPC (appointments API) |
| 8080 | Web UI (`/`) and Actuator (`/actuator/*`) |

## Required headers (gRPC)

| Header | Required |
|--------|----------|
| `x-tenant-id` | Yes — tenant UUID |
| `authorization` | Yes when `AUTH_ENABLED=true` — `Bearer <api-key>` |

## Deploy (Docker)

```bash
docker compose up --build -d
```

## Deploy (Kubernetes)

```bash
kubectl apply -f deploy/k8s/
```

Create secret `appointment-rpc-secrets` with `db-host`, `db-name`, `db-user`, `db-password`, `api-keys`.

## Rollback

Redeploy previous image tag on the Deployment, or:

```bash
kubectl rollout undo deployment/appointment-rpc
```

## Common failures

| Symptom | Likely cause |
|---------|----------------|
| `INVALID_ARGUMENT: Missing x-tenant-id` | Client omitted tenant metadata |
| `NOT_FOUND` on provider | Wrong tenant or unknown provider id |
| `FAILED_PRECONDITION` on book | Slot outside hours or double-booked |
| `UNAUTHENTICATED` | Auth enabled but no API key |
