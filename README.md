# Customer Portal BFF

A reactive **Backend for Frontend (BFF)** that aggregates three downstream services — Customer, Order and Payment — behind a single, resilient API tailored for a customer-facing frontend (Angular/React).

```
                     ┌────────────────────┐
 Angular / React ───▶│   Customer Portal   │
                     │    BFF (WebFlux)    │
                     └─────────┬──────────┘
                 ┌─────────────┼──────────────┐
                 ▼              ▼              ▼
         Customer Service  Order Service  Payment Service
                 │              │              │
                 └──────────────┴──────────────┘
                     aggregation + fallback
                                │
                                ▼
                     single JSON response
                     (cached in Redis)
```

The BFF receives one request from the frontend, fans out to the three downstream APIs concurrently, tolerates partial failures, and returns one aggregated response — the canonical Backend-for-Frontend pattern.

## Why this project

Built as a portfolio piece to demonstrate production-grade backend engineering practices: reactive programming, resilience patterns (circuit breaker, retry, timeout, fallback), JWT security, caching, clean/DDD-inspired layering, and a full CI pipeline — not just CRUD.

## Modules

| Module | Port | Description |
|---|---|---|
| `bff-gateway` | 8080 | The BFF itself. Spring WebFlux, aggregates and secures the API exposed to the frontend. |
| `customer-service` | 8081 | Mock downstream REST API for customer data (Spring MVC). |
| `order-service` | 8082 | Mock downstream REST API for orders (Spring MVC). |
| `payment-service` | 8083 | Mock downstream REST API for payments (Spring MVC). |

The three downstream services use an in-memory repository (seeded with sample data) instead of a database — the point of this project is the BFF's orchestration and resilience layer, not persistence. Swapping in JPA/R2DBC repositories would be a drop-in change behind the existing `*Repository` interfaces.

## Architecture & patterns

- **Backend for Frontend**: `bff-gateway` is the only service the frontend talks to; it hides the downstream topology and shapes the response for the UI.
- **DDD-inspired layering** in every module: `domain` (entities, ports, exceptions) → `application` (use cases) → `infrastructure` (WebClient adapters, Redis, in-memory repositories) → `web` (controllers, DTOs, exception handling).
- **Resilience4j**: every downstream call is wrapped with `@CircuitBreaker` + `@Retry` (exponential backoff) plus a reactive `.timeout(...)`. The customer lookup is mandatory — if it fails, the whole request fails (a portal with no identity is meaningless). Orders and payments degrade gracefully to an empty list plus a `warnings` entry in the response, so a partial outage never becomes a hard error for the frontend.
- **Correlation ID propagation**: `CorrelationIdWebFilter` assigns/reuses an `X-Correlation-Id` header, echoes it back, and forwards it to every downstream WebClient call via the Reactor context — so a single request can be traced across all four services in the logs.
- **JWT security**: `bff-gateway` is an OAuth2 resource server validating HS256-signed JWTs (Spring Security, reactive). A demo `/api/auth/token` endpoint issues tokens for a hardcoded demo user — good enough to exercise the pattern end-to-end; a real deployment would delegate to an identity provider. The BFF terminates auth at the edge; downstream services on the internal network are not re-validated (a real deployment would add mTLS or a service-to-service token for that hop).
- **Redis caching**: the aggregated portal view is cached per customer (short TTL) to protect the downstream services from repeated bursts for the same customer; cache is evicted on customer update/delete. Cache reads/writes fail open (a Redis outage degrades to always recomputing, it never breaks the request).

## Tech stack

Java 25 · Spring Boot 4.0.8 · Spring WebFlux · Spring Security (OAuth2 Resource Server, reactive) · Spring Data Redis (reactive) · Resilience4j (circuit breaker, retry) · Project Reactor · Maven (multi-module) · JUnit 5 · Mockito · WireMock · Podman · GitHub Actions

## API overview

### `bff-gateway` (port 8080)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/token` | none | Issues a demo JWT (`{"username":"demo","password":"demo123"}`) |
| GET | `/api/portal/customers/{id}` | Bearer JWT | Aggregated customer + orders + payments view |
| POST | `/api/portal/customers` | Bearer JWT | Creates a customer |
| PUT | `/api/portal/customers/{id}` | Bearer JWT | Updates a customer (evicts cache) |
| DELETE | `/api/portal/customers/{id}` | Bearer JWT | Deletes a customer (evicts cache) |

Each downstream mock service (`customer-service`, `order-service`, `payment-service`) additionally exposes full `GET/POST/PUT/DELETE` CRUD on its own resource, with Bean Validation on every write and a consistent JSON error body (`timestamp`, `status`, `error`, `message`, `details`).

## Running locally

### Option A — Maven (fastest for development)

```bash
mvn clean install
# in 4 separate terminals:
java -jar customer-service/target/customer-service.jar
java -jar order-service/target/order-service.jar
java -jar payment-service/target/payment-service.jar
java -jar bff-gateway/target/bff-gateway.jar   # needs Redis on localhost:6379
```

### Option B — Podman (full stack, one command)

```bash
podman compose -f podman-compose.yml up --build
```

This starts Redis and all four services on the network defined in `podman-compose.yml`. Host ports are shifted (`18080`-`18083`, `16379`) to avoid clashing with other stacks that use the conventional `8080`-`8083`/`6379` range — internal container-to-container calls still use the standard ports.

| Service | URL from the host |
|---|---|
| bff-gateway | http://localhost:18080 |
| customer-service | http://localhost:18081 |
| order-service | http://localhost:18082 |
| payment-service | http://localhost:18083 |
| redis | localhost:16379 |

### Try it end-to-end

```bash
TOKEN=$(curl -s -X POST http://localhost:18080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}' | jq -r .accessToken)

curl -s http://localhost:18080/api/portal/customers/cust-1001 \
  -H "Authorization: Bearer $TOKEN" | jq
```

## Testing

```bash
mvn clean verify
```

49 tests across the four modules: unit tests (application/service layer), Spring MockMvc/WebTestClient slice tests for every controller (happy path + validation + not-found), and a WireMock-backed integration test that exercises the *real* Resilience4j retry and circuit breaker behavior against a simulated downstream failure — not just asserting the annotations are present.

## CI/CD

`.github/workflows/ci.yml` builds and runs the full test suite (`mvn verify`) on every push and pull request against `master`.
