# Feed Processor

A Spring Boot microservice that ingests sports betting feed messages from multiple external providers, normalises them into a unified internal format, and publishes domain events for downstream consumption.

---

## Table of Contents

- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Design Decisions & Assumptions](#design-decisions--assumptions)
- [API Endpoints](#api-endpoints)
- [Publisher Strategy — Local vs Production](#publisher-strategy--local-vs-production)
- [Production Considerations](#production-considerations)
- [Test Coverage](#test-coverage)
- [Running the Application](#running-the-application)

---

## Architecture

The service is built on **Hexagonal Architecture** (Ports & Adapters). The rule is simple: the domain and application layers have zero framework imports — Spring lives entirely in the infrastructure layer.

```
┌─────────────────────────────────────────────────────┐
│                   Infrastructure                     │
│                                                     │
│  REST Controllers → Mappers → Use Case Interfaces   │
│                                                     │
│       ┌─────────────────────────────────┐           │
│       │       Application Layer         │           │
│       │                                 │           │
│       │  Use Cases → Interactors        │           │
│       │                                 │           │
│       │   ┌─────────────────────┐       │           │
│       │   │    Domain Layer     │       │           │
│       │   │                     │       │           │
│       │   │  Events  Models     │       │           │
│       │   └─────────────────────┘       │           │
│       └─────────────────────────────────┘           │
│                                                     │
│  DomainEventPublisher (interface) ← implementations │
└─────────────────────────────────────────────────────┘
```

**Flow per request:**

```
HTTP POST
  → Controller (validates, deserializes)
  → Mapper (DTO → Command)
  → Interactor (domain logic, publishes event)
  → DomainEventPublisher (interface — swappable implementation)
```

---

## Project Structure

```
src/main/java/com/sporty/feed/
│
├── domain/
│   ├── event/
│   │   ├── DomainEvent.java               # Marker interface for all domain events
│   │   ├── OddsChangedEvent.java          # Immutable record: eventId, timestamp, home/draw/away odds
│   │   └── BetSettledEvent.java           # Immutable record: eventId, timestamp, outcome
│   └── model/
│       ├── Outcome.java                   # Enum: HOME, DRAW, AWAY — with per-provider conversion
│       ├── OddsChangeMessage.java         # Normalised odds message (record)
│       └── BetSettlementMessage.java      # Normalised settlement message (record)
│
├── application/
│   ├── gateway/
│   │   └── DomainEventPublisher.java      # Output port — infrastructure provides the impl
│   ├── usecase/
│   │   ├── OddsChangeUseCase.java         # Input boundary interface
│   │   ├── BetSettlementUseCase.java      # Input boundary interface
│   │   └── command/
│   │       ├── FeedCommand.java           # Sealed base for all commands
│   │       ├── OddsChangeCommand.java     # Record: eventId, timestamp, odds
│   │       └── BetSettlementCommand.java  # Record: eventId, timestamp, outcome
│   └── service/
│       ├── OddsChangeInteractor.java      # Implements OddsChangeUseCase — pure Java, no Spring
│       └── BetSettlementInteractor.java   # Implements BetSettlementUseCase — pure Java, no Spring
│
└── infrastructure/
    ├── config/
    │   ├── ApplicationConfig.java         # Wires pure-Java interactors as Spring beans
    │   └── JacksonConfig.java             # Strict JSON coercion (rejects int/float for string fields)
    ├── messaging/
    │   └── LoggingDomainEventPublisher.java  # @Profile("local") stub — logs events, no broker
    └── web/
        ├── advice/
        │   └── GlobalExceptionHandler.java   # Unified error responses
        ├── controller/
        │   ├── ProviderAlphaController.java  # POST /provider-alpha/feed
        │   └── ProviderBetaController.java   # POST /provider-beta/feed
        ├── dto/
        │   ├── alpha/                         # Provider Alpha DTOs + custom deserializer
        │   └── beta/                          # Provider Beta DTOs + custom deserializer
        └── mapper/
            ├── AlphaFeedMapper.java           # Alpha DTO → FeedCommand
            └── BetaFeedMapper.java            # Beta DTO → FeedCommand
```

---

## Design Decisions & Assumptions

### Multi-provider normalisation

Each provider has its own wire format and is fully isolated behind its own DTO layer. Normalisation to the internal `Outcome` enum and command model happens in the mapper — the application layer never sees provider-specific types. Adding a third provider is additive: new DTOs, new mapper, new controller endpoint, zero changes to domain or application code.

### Sealed types for exhaustive dispatch

Java 21 sealed interfaces and records are used throughout the domain and application layers. Combined with pattern matching (`switch`), the compiler enforces that every message type is handled. There is no runtime `default` branch to silently swallow unhandled cases.

### Strict JSON validation

Two levels of protection against malformed payloads:

1. **Custom Jackson deserializers** (`AlphaOddsDeserializer`, `BetaOddsDeserializer`) — reject any field not in the known set before Bean Validation runs. Spring Boot globally disables `FAIL_ON_UNKNOWN_PROPERTIES`, so class-level `@JsonIgnoreProperties` alone is not reliable; a custom deserializer is the only guaranteed approach.
2. **Bean Validation** (`@NotNull`, `@Positive`, `@NotBlank`, `@Pattern`) — validates field presence and value constraints with descriptive per-field error messages.

Additionally, `JacksonConfig` uses Jackson 2.12's `CoercionConfigs` API to reject integers and floats where a `String` is expected (e.g. `event_id: 123` → `400`). There is no equivalent Spring Boot YAML property for this; the YAML `mapper.allow-coercion-of-scalars` silently does nothing.

### Assumptions

- **Event IDs** are treated as opaque strings. No format is enforced beyond non-blank. In production this would be a UUID validated with `@Pattern`.
- **Odds values** must be strictly positive `Double`s. Zero and negative values are rejected.
- **Provider Alpha** discriminates message type via `msg_type` (`odds_update`, `settlement`). **Provider Beta** uses `type` (`ODDS`, `SETTLEMENT`). Both use Jackson's `@JsonTypeInfo` / `@JsonSubTypes` for polymorphic deserialisation — no manual type-switching at the controller.
- Timestamps are assigned at ingestion time (not from the provider payload), which is acceptable given the requirement to record when the event was received.
- The service is stateless. No persistence layer is included; this is intentional for the scope of this implementation.

---

## API Endpoints

### Provider Alpha — `POST /provider-alpha/feed`

**Odds update:**
```json
{
  "msg_type": "odds_update",
  "event_id": "match-001",
  "values": { "1": 2.10, "X": 3.20, "2": 3.50 }
}
```

**Settlement:**
```json
{
  "msg_type": "settlement",
  "event_id": "match-001",
  "outcome": "1"
}
```
Valid outcomes: `1` (home), `X` (draw), `2` (away).

---

### Provider Beta — `POST /provider-beta/feed`

**Odds update:**
```json
{
  "type": "ODDS",
  "event_id": "match-001",
  "odds": { "home": 1.95, "draw": 3.20, "away": 4.00 }
}
```

**Settlement:**
```json
{
  "type": "SETTLEMENT",
  "event_id": "match-001",
  "result": "home"
}
```
Valid results: `home`, `draw`, `away`.

---

**Responses:**

| Status | Meaning |
|--------|---------|
| `202 Accepted` | Message accepted and processed |
| `400 Bad Request` | Validation failure, unknown type discriminator, or malformed JSON |

**Error response shape:**
```json
{ "message": "Unrecognized or malformed message format" }
```
```json
{ "message": "Validation failed", "errors": ["eventId: event_id must not be blank"] }
```

**Swagger UI** available at `http://localhost:8080/swagger-ui.html` when running locally.

---

## Publisher Strategy — Local vs Production

`LoggingDomainEventPublisher` is annotated `@Profile("local")`. It is the active implementation when `spring.profiles.active=local` (set in `application.yml`). It logs every domain event to SLF4J and does nothing else — no broker, no persistence.

For production, swap in a real implementation of the `DomainEventPublisher` interface and activate it on the appropriate profile. No other code changes anywhere in the codebase.

**Recommended production publisher architecture:**

A single `DomainEventPublisher` implementation in production is too narrow. The pattern to use is Spring's `ApplicationEventPublisher` with multiple `@EventListener` beans, each handling the event independently:

```
DomainEventPublisher (infra impl)
  └─ publishes via Spring ApplicationEventPublisher
       ├── MetricsEventListener      → increments Micrometer counters
       ├── AuditLogEventListener     → structured audit log entry
       └── OutboxEventListener       → writes to transactional outbox table
                                           └─ Outbox Relay (CDC / polling)
                                                 └─ Kafka topic
```

This gives fan-out without coupling: adding a new listener (e.g. alerting) touches nothing else.

---

## Production Considerations

This implementation was built to the stated requirements with the minimum complexity needed to satisfy them correctly. The following are the known gaps to close before production:

### Transactional Outbox
The current publisher is fire-and-forget. In production, event publication must be part of the same transaction as the business operation. The standard pattern is the **Transactional Outbox**: write the event to an `outbox` table in the same DB transaction, then relay it to Kafka via CDC (Debezium) or a polling relay. This eliminates dual-write inconsistency.

### Idempotency
The same event with the same `event_id` and `msg_type` can be ingested multiple times (provider retries, network duplicates). An idempotency table keyed on `(event_id, msg_type)` with a unique constraint will deduplicate at the ingestion boundary. Any duplicate returns `202` without reprocessing.

### Persistence & Transactions
No database exists currently. Production needs:
- A JPA/JDBC entity for the outbox table
- `@Transactional` on the interactors (currently pure Java with no transaction boundary)
- A connection pool (HikariCP, already bundled with Spring Boot) tuned for expected concurrency

### Retry & Resilience
Database `RuntimeException`s (connection timeout, deadlock) need retry logic with backoff — Spring Retry or Resilience4j. Kafka publish failures need a dead-letter queue strategy.

### Observability
- Micrometer metrics: ingestion count, error rate, processing latency per provider and message type
- Structured logging (Logstash JSON encoder) with `event_id` and `msg_type` in MDC for trace correlation
- Health endpoint (`/actuator/health`) with a Kafka liveness check in the production profile

### Security
- Mutual TLS or API key authentication per provider endpoint (providers should not share a secret)
- Rate limiting per provider to protect against runaway feed clients

### event_id Format
Currently validated only as non-blank. Production should enforce a UUID format: `@Pattern(regexp = "^[0-9a-fA-F-]{36}$")`.

---

## Test Coverage

59 tests across four layers:

### Unit Tests
| Test | What it covers |
|------|---------------|
| `OutcomeTest` | `Outcome.fromAlpha` and `Outcome.fromBeta` — all valid values and rejection of unknown values |
| `OddsChangeInteractorTest` | Interactor publishes `OddsChangedEvent` with correct field values |
| `BetSettlementInteractorTest` | Interactor publishes `BetSettledEvent` with correct field values |
| `AlphaFeedMapperTest` | DTO-to-command mapping for odds update and all three settlement outcomes |
| `BetaFeedMapperTest` | DTO-to-command mapping for odds update and all three settlement outcomes |

### Web Layer Slice Tests (`@WebMvcTest`)
| Test | What it covers |
|------|---------------|
| `ProviderAlphaControllerTest` | Controller routes `odds_update` → `OddsChangeUseCase`, `settlement` → `BetSettlementUseCase`, unknown type → 400 |
| `ProviderBetaControllerTest` | Same for Beta provider |
| `GlobalExceptionHandlerTest` | All error branches: unknown discriminator, malformed JSON, missing fields, invalid values |

### Integration Tests (`@SpringBootTest` — full context)
| Test | What it covers |
|------|---------------|
| `ProviderAlphaIntegrationTest` (15 tests) | Full chain HTTP → event publisher |
| `ProviderBetaIntegrationTest` (15 tests) | Full chain HTTP → event publisher |

**Validation scenarios covered by integration tests (both providers):**

| Scenario | Expected |
|----------|----------|
| Valid odds update | `202` + correct domain event fields |
| Valid settlement — home / draw / away | `202` + correct `Outcome` |
| Unknown message type discriminator | `400` |
| Missing `event_id` | `400` |
| Blank `event_id` (`""`) | `400` + `"event_id must not be blank"` |
| Whitespace `event_id` (`"   "`) | `400` + `"event_id must not be blank"` |
| Integer `event_id` (`123`) | `400` — coercion disabled |
| Missing odds object | `400` |
| Missing individual odds key | `400` + field-specific null message |
| Unknown odds key | `400` + `"Unknown field 'X' is not allowed"` |
| Negative odds value | `400` + field-specific positive message |
| Invalid outcome / result value | `400` |

---

## Running the Application

### Prerequisites
- Docker (recommended), or JDK 21 + Maven 3.9

### With Docker

```bash
# Build
docker build -t sporty-feed .

# Run
docker run -p 8080:8080 sporty-feed
```

The image uses a multi-stage build: dependencies are downloaded in a cached layer separate from source, so subsequent builds after a code-only change are fast.

### With Maven

```bash
mvn spring-boot:run
```

This activates the `local` profile automatically (configured in `application.yml`), which starts `LoggingDomainEventPublisher`.

### Running Tests

```bash
mvn test
```

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## Manual Testing with curl

### Provider Alpha

**Valid odds update:**
```bash
curl -s -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"odds_update","event_id":"match-001","values":{"1":2.10,"X":3.20,"2":3.50}}' \
  -w "\nHTTP %{http_code}"
```

**Valid settlement:**
```bash
curl -s -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"settlement","event_id":"match-001","outcome":"1"}' \
  -w "\nHTTP %{http_code}"
```

**Missing odds key (`"2"` absent):**
```bash
curl -s -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"odds_update","event_id":"match-001","values":{"1":2.10,"X":3.20}}' \
  -w "\nHTTP %{http_code}"
```

**Unknown odds key:**
```bash
curl -s -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"odds_update","event_id":"match-001","values":{"1":2.10,"X":3.20,"2":3.50,"3":4.00}}' \
  -w "\nHTTP %{http_code}"
```

**Negative odds:**
```bash
curl -s -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"odds_update","event_id":"match-001","values":{"1":-1.0,"X":3.20,"2":3.50}}' \
  -w "\nHTTP %{http_code}"
```

**Blank event_id:**
```bash
curl -s -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"odds_update","event_id":"","values":{"1":2.10,"X":3.20,"2":3.50}}' \
  -w "\nHTTP %{http_code}"
```

**Integer event_id:**
```bash
curl -s -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"odds_update","event_id":123,"values":{"1":2.10,"X":3.20,"2":3.50}}' \
  -w "\nHTTP %{http_code}"
```

**Unknown msg_type:**
```bash
curl -s -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"unknown","event_id":"match-001"}' \
  -w "\nHTTP %{http_code}"
```

**Invalid outcome:**
```bash
curl -s -X POST http://localhost:8080/provider-alpha/feed \
  -H "Content-Type: application/json" \
  -d '{"msg_type":"settlement","event_id":"match-001","outcome":"Z"}' \
  -w "\nHTTP %{http_code}"
```

---

### Provider Beta

**Valid odds update:**
```bash
curl -s -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"ODDS","event_id":"match-001","odds":{"home":1.95,"draw":3.20,"away":4.00}}' \
  -w "\nHTTP %{http_code}"
```

**Valid settlement:**
```bash
curl -s -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"SETTLEMENT","event_id":"match-001","result":"home"}' \
  -w "\nHTTP %{http_code}"
```

**Missing odds key (`away` absent):**
```bash
curl -s -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"ODDS","event_id":"match-001","odds":{"home":1.95,"draw":3.20}}' \
  -w "\nHTTP %{http_code}"
```

**Unknown odds key:**
```bash
curl -s -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"ODDS","event_id":"match-001","odds":{"home":1.95,"draw":3.20,"away":4.00,"extra":5.00}}' \
  -w "\nHTTP %{http_code}"
```

**Negative odds:**
```bash
curl -s -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"ODDS","event_id":"match-001","odds":{"home":-1.0,"draw":3.20,"away":4.00}}' \
  -w "\nHTTP %{http_code}"
```

**Blank event_id:**
```bash
curl -s -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"ODDS","event_id":"","odds":{"home":1.95,"draw":3.20,"away":4.00}}' \
  -w "\nHTTP %{http_code}"
```

**Integer event_id:**
```bash
curl -s -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"ODDS","event_id":123,"odds":{"home":1.95,"draw":3.20,"away":4.00}}' \
  -w "\nHTTP %{http_code}"
```

**Unknown type:**
```bash
curl -s -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"UNKNOWN","event_id":"match-001"}' \
  -w "\nHTTP %{http_code}"
```

**Invalid result:**
```bash
curl -s -X POST http://localhost:8080/provider-beta/feed \
  -H "Content-Type: application/json" \
  -d '{"type":"SETTLEMENT","event_id":"match-001","result":"invalid"}' \
  -w "\nHTTP %{http_code}"
```