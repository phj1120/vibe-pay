# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

VibePay is a payment module built with vibe coding. It implements order management, basket, payment (Inicis/Nice PG integration), and point system.

- **api/**: Kotlin + Spring Boot 3.5 backend
- **fo/**: Next.js 16 + TypeScript frontend
- **docs/**: Domain docs and DB schema (`docs/database/schema.md` is the authoritative schema reference)

---

## Commands

### API (backend)

```bash
# Run all tests
cd api && ./gradlew test

# Run a single test class
cd api && ./gradlew test --tests "com.api.app.service.order.OrderSheetServiceImplTest"

# Build
cd api && ./gradlew build

# Run locally (requires PostgreSQL on localhost:5432)
cd api && ./gradlew bootRun
```

### FO (frontend)

```bash
cd fo && npm run dev      # dev server on :3000
cd fo && npm run build    # production build
cd fo && npm run lint     # ESLint
```

### Infrastructure

```bash
# Start PostgreSQL + API
docker-compose up -d

# DB only
docker-compose up -d db
```

---

## Architecture

### Dual DataSource (RWDB / RODB)

The API uses two JPA datasource configurations in `DataSourceConfig.kt`:

- **`repository/rwdb/`** — write operations, backed by `primaryEntityManagerFactory` + `primaryTransactionManager`
- **`repository/rodb/`** — read operations, backed by `secondaryEntityManagerFactory` + `secondaryTransactionManager`

Both point to the same PostgreSQL instance in the current config but are structured for future read-replica separation. QueryDSL factories (`primaryJpaQueryFactory` / `secondaryJpaQueryFactory`) are wired per datasource.

### API Layer

```
Controller → Service (interface) → ServiceImpl → Repository (JPA / QueryDSL)
```

- All controllers return `ApiResponse<T>` (`common/response/ApiResponse.kt`)
- Error codes are centralized in `ApiError` enum (`common/exception/ApiError.kt`): `0000` success, `1xxx` client, `2xxx` auth, `3xxx` payment, `9xxx` server
- `GlobalExceptionHandler` handles `ApiException`, Bean Validation, and catch-all
- `@PreAuthorize("isAuthenticated()")` is the default on controllers; public endpoints are whitelisted in `SecurityConfig`

### AOP: SystemColumnAspect

`aop/SystemColumnAspect.kt` intercepts all `rwdb` `save*()` calls. It automatically sets `registId` (on first save) and `modifyId` (always) from the JWT principal on any entity extending `SystemEntity`.

### Auth Flow

JWT stateless auth. Access token expires in 30s (test config). `fo/src/lib/api-client.ts` handles silent token refresh with a queue to prevent concurrent refresh storms (401 with code `2002`/`2003` triggers refresh, then replays queued requests).

### FO API Client

`fo/src/lib/api-client.ts` is the single HTTP client. All domain API modules (`lib/basket-api.ts`, `lib/order-api.ts`, etc.) delegate to it. State is managed via Zustand stores (`store/basket-store.ts`, etc.). Zod is used for form validation at the page level.

### Payment Flow

Payment uses popup-based PG integration:
1. FO opens a popup window for PG redirect
2. PG posts result to `fo/src/app/api/order/payment/return/route.ts` (Next.js Route Handler)
3. Route handler detects PG type (Inicis: `resultCode`, Nice: `AuthResultCode`) and sends `postMessage` to parent window, then closes
4. Parent window handles the message and calls backend approval endpoint

### Key Entity Conventions

- All entities extend `SystemEntity` (provides `registId`, `registDateTime`, `modifyId`, `modifyDateTime`)
- PK formats: basket → 15-char sequence; order → `yyyyMMdd` + `O` + sequence (e.g. `20251027O000001`); claim → `yyyyMMdd` + `C` + sequence
- `order_detail` uses a 3-part composite PK (`order_no`, `order_sequence`, `order_process_sequence`) with `upper_order_process_sequence` tracking the original row for claims/exchanges

### Test Structure

Tests are currently in Java under `api/src/test/java/` (pending migration to Kotlin). They use Mockito `@ExtendWith(MockitoExtension.class)` and test service layer in isolation via `@InjectMocks` / `@Mock`.
