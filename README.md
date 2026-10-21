# Customer Portal Platform - observability, tests and deployment

This is the Customer Portal microservices project (repo 1) plus the production-readiness layer:

| Area | What is included |
|---|---|
| Metrics | Spring Boot Actuator + Micrometer -> Prometheus -> Grafana dashboard (requests/s, 5xx, p95 latency, JVM heap) |
| Tracing | Micrometer Tracing (Brave) -> Zipkin, trace/span ids in every log line |
| Integration tests | Testcontainers: repositories tested against real PostgreSQL |
| Contract tests | Pact: billing-service (consumer) states what it needs from customer-service, customer-service (provider) verifies it |
| CI/CD | GitHub Actions: build + all tests, then build (and on `main`, push) one Docker image per service to GHCR |
| Kubernetes | Plain manifests + kustomize: Deployments, Services, probes, resources, HPA, config and secrets |

You can publish this as its own repo, or copy the extra files (`observability/`, `k8s/`, `.github/`,
`docker-compose.observability.yml`, the `src/test` additions, and the dependency/config changes in the poms and
`application.yml` files) into repo 1 as a second stage of the same project.

## Run with metrics and tracing
```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up --build
```
- API gateway: http://localhost:8080 (use the curl flow from the Customer Portal README)
- Grafana: http://localhost:3000 (anonymous admin, dashboard "Customer Portal - Service Overview")
- Prometheus: http://localhost:9090 (Status > Targets: all six services should be UP)
- Zipkin: http://localhost:9411 (search for a trace after sending some requests)

## Tests
Docker must be running (Testcontainers starts PostgreSQL).

```bash
mvn verify
```
Order matters for the contract test: `billing-service` runs first and writes
`billing-service/target/pacts/billing-service-customer-service.json`; `customer-service` then verifies it.
To see the contract protect you, rename `fullName` in `CustomerController.CustomerView` and re-run: the provider
verification fails.

## Kubernetes (kind or minikube)
```bash
docker compose build                       # builds portal/<service>:latest images
kind load docker-image $(docker images --format '{{.Repository}}:{{.Tag}}' | grep '^portal/')   # kind only
kubectl apply -k k8s
kubectl -n portal get pods -w
kubectl -n portal port-forward svc/api-gateway 8080:8080
```
Postgres uses an `emptyDir` volume and secrets are demo values; both need replacing for real use.

## CI
`.github/workflows/ci.yml` runs on push and pull request. Images are pushed to
`ghcr.io/<your-username>/portal-<service>:latest` only from `main`.
