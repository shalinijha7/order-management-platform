# Distributed Order Management Platform

A microservices demo: place an order → stock is reserved synchronously → an event
is published to Kafka → a notification service consumes it asynchronously.

## Architecture

```
                         ┌───────────────────┐
                         │  discovery-service │  (Eureka, :8761)
                         └─────────▲──────────┘
                                   │ all services register here
                                   │
   React (Vite, :5173)   ┌────────┴─────────┐
        │  /api/*         │   api-gateway     │  :8080
        └───────────────▶ │ (Spring Cloud GW) │
                          └───────┬───────────┘
                     ┌────────────┼─────────────┐
                     ▼            ▼              ▼
             user-service  inventory-service  order-service
                :8081          :8082             :8083
                 │                │                │  │
              MySQL            MySQL          MongoDB │
                                  ▲                    │
                                  │ Feign (reserve      │ Kafka
                                  │ stock, sync call)   │ "order-events"
                                  └────────────────────┘│
                                                          ▼
                                              notification-service :8084
                                                 (Kafka consumer)
```

## Request flow when an order is placed

1. Frontend calls `POST /api/orders` → hits **api-gateway** → routed to **order-service**
   (api-gateway resolves `order-service` via Eureka, not a hardcoded URL).
2. **order-service** calls **inventory-service** synchronously through a Feign client
   (`InventoryClient`) to reserve stock. If stock is insufficient, the order fails fast
   with a 409.
3. If stock reservation succeeds, **order-service** saves the order in **MongoDB** with
   status `PLACED`.
4. **order-service** publishes an `OrderEvent` to the Kafka topic `order-events`.
5. **notification-service** independently consumes that topic and logs/"sends" a
   notification — this happens asynchronously and doesn't block the original request.

This is the core pattern worth explaining in an interview: the *synchronous* path
(reserve stock) has to succeed before you commit the order, but the *side effect*
(notifying the user) doesn't need to block the response, so it goes over Kafka instead.

## Prerequisites

- Java 17+, Maven 3.9+
- Node.js 18+
- Docker Desktop (for Kafka, MySQL, MongoDB) — see the separate Kafka/Docker setup guide

## Running it

**1. Start infrastructure (Kafka, Zookeeper, MySQL, MongoDB, Kafka UI):**
```bash
docker compose up -d
```

**2. Start services in this order** (each in its own terminal, from its folder):
```bash
cd discovery-service    && mvn spring-boot:run   # wait until it's up on :8761
cd inventory-service    && mvn spring-boot:run
cd user-service         && mvn spring-boot:run
cd order-service        && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd api-gateway          && mvn spring-boot:run
```
Check http://localhost:8761 — you should see all 5 services registered.

**3. Start the frontend:**
```bash
cd frontend
npm install
npm run dev
```
Open http://localhost:5173

**4. Watch it work:**
- Register a user, pick a product, place an order.
- Check `GET http://localhost:8084/api/notifications` to see the notification-service
  log the Kafka event it consumed.
- Open http://localhost:8090 (Kafka UI) → topic `order-events` to see the raw message.

## Notes / things to extend if you want to go further

- Add JWT auth on user-service (the Library Management project on the resume already
  covers this pattern, so it's easy to port over).
- Add a `PAYMENT_FAILED` / retry flow to show you understand eventual consistency and
  the outbox pattern.
- Swap the in-memory notification log for an actual email (e.g. JavaMailSender) or a
  webhook, to make the async effect visible end-to-end.

## Application Flow
- How the flow works ?

- discovery-service (Eureka, :8761) — every other service registers here, so they can find each other by name instead of hardcoded IPs.
- api-gateway (:8080) — single entry point. Routes /api/users/**, /api/products/**, /api/orders/** to the right service via Eureka.
- user-service (:8081, MySQL) — register/fetch users.
- inventory-service (:8082, MySQL) — product catalog + a /reserve endpoint that decrements stock.
- order-service (:8083, MongoDB) — the orchestrator: calls inventory-service synchronously (via Feign) to reserve stock, saves the order, then publishes to Kafka.
- notification-service (:8084) — consumes the Kafka topic order-events asynchronously and logs a "notification."