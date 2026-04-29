# VibePay 모놀리식 → MSA 분리 계획

> 새 세션/모델에서 바로 참조해 실행할 수 있도록 작성된 자체 완결 문서.

---

## 0. 배경 (Context)

VibePay는 현재 `api/`(Kotlin/Spring Boot 3.5) 단일 모듈에 7개 도메인(Member, Basket, Goods, Order, Payment, Point, Claim)이 모놀리식으로 구현되어 있다. 대용량 트래픽 환경에서의 확장성, 장애 격리, 분산 패턴(Saga, Outbox, 분산락 등) **학습**을 위해 MSA로 분리한다.

중요:

- 이 문서의 `gateway-service`, `core-service`, `goods-service`, `order-service`, `payment-service`, `claim-service`는 **현재 존재하는 실행 모듈 목록이 아니라 목표 상태**다.
- 현재 저장소 기준 실제 실행 코드는 `api/` 단일 모듈 모놀리식이며, 서비스 분리는 이제 처음부터 다시 진행한다.

### 결정 사항 (사용자 합의)

| 항목 | 결정 |
|---|---|
| **분리 범위** | Order, Payment, Claim, Goods를 독립 서비스로. Member + Basket + Point는 단일 `core-service`로 통합 |
| **레포 구조** | 모노레포 + Gradle 멀티모듈 (현재 `api/` 디렉토리를 멀티모듈 루트로 전환) |
| **통신 패턴** | Redis 분산락+캐시, Feign + Resilience4j Circuit Breaker, Kafka + Outbox, Saga Choreography (모두 학습) |
| **API Gateway** | Spring Cloud Gateway + docker-compose 컨테이너명 디스커버리 (Eureka 미사용) |

### 핵심 결합점 (현재 모놀리식 코드 — 분리 시 끊어야 함)

- `OrderServiceImpl.kt` L180: `basketBaseTrxRepository.updateBasketIsOrder()` 직접 호출
- `OrderServiceImpl.kt` L91-102: GoodsService로 재고/가격 검증
- `OrderServiceImpl.kt` L113: `paymentMethodFactory.getStrategy()` 결제
- `ClaimServiceImpl.kt` L208, L46: PointService로 포인트 반환
- `ClaimServiceImpl.kt` L150: PaymentGatewayFactory로 결제 취소
- `OrderServiceImpl` `@Transactional`: Order + Basket + Payment 단일 트랜잭션
- `ClaimServiceImpl` `@Transactional`: Order + Point + Payment 단일 트랜잭션

### DB FK 결합 (분리 시 제거)

- `order_base.member_no → member_base`
- `pay_base.order_no → order_base`, `pay_base.claim_no → order_detail`
- `order_detail.goods_no → goods_base` (스냅샷 컬럼으로 대체)
- `point_history.member_no → member_base`
- `basket_base.goods_no → goods_base`

---

## 1. 목표 모듈 구조 (Gradle 멀티모듈)

```
vibe-pay/
├── api/
│   ├── settings.gradle.kts          # 8개 모듈 include
│   ├── build.gradle.kts             # subprojects { } 공통 설정
│   ├── common-lib/
│   │   ├── common-core/             # ApiResponse, ApiError, ApiException, SystemEntity, SystemColumnAspect, GlobalExceptionHandler
│   │   ├── common-security/         # JwtTokenProvider, AuthPrincipal, HeaderPrincipalResolver
│   │   └── common-messaging/        # OutboxEntity, OutboxPublisher, EventEnvelope, KafkaTopics, AbstractIdempotentConsumer
│   ├── core-service/                # 8081: member + basket + point
│   ├── goods-service/               # 8082
│   ├── order-service/               # 8083
│   ├── payment-service/             # 8084
│   ├── claim-service/               # 8085
│   └── gateway-service/             # 8080 (Spring Cloud Gateway, WebFlux)
├── fo/                               # 변경 거의 없음
└── docker-compose.yml
```

이 구조는 최종 목표다. 현재 저장소에는 아직 아래 모듈들이 실제로 존재하지 않는다.

**설정 포인트**:

```kotlin
// settings.gradle.kts
rootProject.name = "vibepay"
include("common-lib:common-core", "common-lib:common-security", "common-lib:common-messaging")
include("core-service", "goods-service", "order-service", "payment-service", "claim-service", "gateway-service")
```

- `common-lib/*` 모듈: `bootJar { enabled = false }; jar { enabled = true }` (라이브러리 jar)
- 서비스 모듈: `implementation(project(":common-lib:common-core"))` 형태로 의존
- `common-core`는 JPA/Spring 의존하되 `@SpringBootApplication`은 없음
- 각 서비스는 `@EntityScan("com.vibepay")`/`@ComponentScan`으로 공통 빈 스캔

---

## 2. 서비스 책임 매트릭스

| 서비스 | 도메인 | 소유 테이블 | 주요 노출 API |
|---|---|---|---|
| **gateway** (8080) | 라우팅, JWT 1차 검증 | (없음) | 외부 트래픽 진입점 |
| **core** (8081) | Member, Basket, Point | member_base, basket_base, point_history, point_balance | `/api/members/**`, `/api/baskets/**`, `/api/point/**` |
| **goods** (8082) | Goods, Stock | goods_base, goods_item, goods_price_hist, goods_stock | `/api/goods/**`, internal: `/internal/goods/stock-deduct`, `/internal/goods/snapshot` |
| **order** (8083) | Order | order_base, order_detail, order_outbox | `/api/order/**` |
| **payment** (8084) | Payment, PG | pay_base, pay_interface_log, payment_outbox | `/api/payments/**`, PG callback 수신 |
| **claim** (8085) | Claim | claim_base, claim_detail, claim_saga_state, claim_outbox | `/api/claim/**` |

**주의**: `order_detail.goods_no`는 FK 끊고 **상품 스냅샷 컬럼**(goods_name, goods_price 등) 추가.

## 2.1 현재 시작점

현재 실제 시작점은 아래와 같다.

- `api/` 단일 Gradle 모듈
- 단일 Spring Boot 애플리케이션
- 주문/결제/클레임/상품/회원/장바구니/포인트가 같은 코드베이스 안에 공존
- `docker-compose.yml`에는 목표 MSA 형태 초안이 남아 있을 수 있지만, 현재 코드와 1:1로 대응되지 않을 수 있음

따라서 분리 작업은 `기존 일부 MSA 모듈을 이어받는 작업`이 아니라, `모놀리식에서 서비스 경계를 새로 정의하고 단계적으로 꺼내는 작업`으로 본다.

---

## 3. 통신 매핑 (현재 모놀리식 결합점 → 목표 분산 패턴)

| 현재 호출 | 새 패턴 | 이유 |
|---|---|---|
| Order → Basket.updateBasketIsOrder | **Kafka 이벤트** `OrderConfirmed` → core consume | 비동기 OK |
| Order → Goods 재고/가격 검증 | **Feign + Resilience4j CB + Redis 캐시** | 동기 검증 + 가격 1분 TTL |
| Order → Goods 재고 차감 | **Redis 분산락 + Feign** `/internal/goods/stock-deduct` | 동시성 제어 |
| Order → PaymentFactory | **Saga (Kafka)** `OrderCreated` → payment consume → `PaymentApproved/Failed` | 보상 트랜잭션 필요 |
| Claim → Point 환불 | **Saga Choreography** `ClaimRequested` → core consume | 비동기 |
| Claim → PG 취소 | **Saga Choreography** `ClaimRequested` → payment consume | 외부 호출 분리 |
| Order/Claim 단일 @Transactional | **Outbox + 로컬 트랜잭션 분할** | eventual consistency |

---

## 4. Saga Choreography 시나리오

### (1) 주문 생성

```
[order] POST /api/order
  TX: insert order_base(PENDING) + outbox(OrderCreated) → topic: order.events
       ↓
[payment] consume OrderCreated → PG 호출
  성공: TX insert pay_base + outbox(PaymentApproved) → topic: payment.events
  실패: outbox(PaymentFailed)
       ↓
[order] consume PaymentApproved → TX: status=PAID + outbox(OrderConfirmed)
[order] consume PaymentFailed → 보상: status=CANCELLED + outbox(OrderCancelled)
       ↓
[core]  consume OrderConfirmed → basket.is_order=Y, point 적립
[goods] consume OrderConfirmed → 예약 재고 → 확정
[core]  consume OrderCancelled → 보상: 적립 회수, basket 복원
[goods] consume OrderCancelled → 보상: 예약 재고 해제
```

**재고 처리**: 주문 시점 **Redis 분산락 + 예약(reserved_qty)**, OrderConfirmed에서 확정.

### (2) 클레임(취소)

```
[claim] POST /api/claim
  TX: insert claim_base(REQUESTED) + outbox(ClaimRequested{orderNo, payNo, refundPoint})
       ↓ topic: claim.events
[payment] consume → PG 취소
  성공: outbox(PaymentCancelled) / 실패: outbox(PaymentCancelFailed)
[core]  consume → 포인트 환불 + outbox(PointRefunded)
       ↓
[claim] consume PaymentCancelled + PointRefunded (둘 다 수신 시)
  → claim_base.status=COMPLETED
[claim] consume PaymentCancelFailed → 보상: core에 PointRefundCompensate 발행 + status=FAILED
```

**상관관계 추적**: `claim_saga_state(claim_no, payment_done, point_done)` 작은 테이블로 관리.

---

## 5. Outbox 패턴

### 스키마

```sql
CREATE TABLE outbox (
  id BIGSERIAL PRIMARY KEY,
  aggregate_type VARCHAR(50) NOT NULL,
  aggregate_id   VARCHAR(50) NOT NULL,
  event_type     VARCHAR(80) NOT NULL,
  topic          VARCHAR(80) NOT NULL,
  payload        JSONB NOT NULL,
  status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  retry_count    INT NOT NULL DEFAULT 0,
  created_at     TIMESTAMP NOT NULL DEFAULT now(),
  sent_at        TIMESTAMP
);
CREATE INDEX idx_outbox_pending ON outbox(status, id) WHERE status='PENDING';
```

### Publisher

- `@Scheduled(fixedDelay=500)` + `SELECT ... FOR UPDATE SKIP LOCKED LIMIT 100 WHERE status='PENDING'`
- 학습 목적상 Debezium/CDC 미사용 (단순 polling)
- `common-messaging` 모듈에 `OutboxPublisher` 추상화, `KafkaTemplate` 주입

### Consumer 멱등성

- Redis `SETNX idem:{topic}:{eventId}` TTL 24h
- `EventEnvelope { eventId, occurredAt, type, schemaVersion, payload }` 구조 통일

---

## 6. DB 분리 전략

**단일 PostgreSQL 컨테이너 + 서비스별 schema 분리** (학습 비용 최소화. 운영 정석은 별 인스턴스).

```
DB: vibepay
├── schema: core    (member, basket, point)
├── schema: goods
├── schema: order   (order + outbox)
├── schema: payment
└── schema: claim
```

- 각 서비스는 자기 schema의 user로만 접속 (권한 분리로 강제)
- **Flyway**: 각 서비스 모듈의 `src/main/resources/db/migration/`로 분리, `spring.flyway.schemas` / `default-schema` 설정
- 기존 RWDB/RODB 이중 DataSource는 학습 단계에서 **단순화**(서비스당 단일 DataSource). 구조는 `common-core`에 재활용 가능 형태로 보존

---

## 7. Redis 활용

| 용도 | 위치 | Key |
|---|---|---|
| 분산락 (재고 차감) | goods | `lock:goods:stock:{goodsNo}` TTL 3s, Redisson `tryLock` |
| 분산락 (포인트 차감) | core | `lock:point:{memberNo}` |
| 캐시 (상품 스냅샷/가격) | order | `cache:goods:{goodsNo}` TTL 60s |
| 캐시 (member profile) | gateway | `cache:member:{memberNo}` TTL 5m |
| 멱등성 (Saga consumer) | 모든 consumer | `idem:{topic}:{eventId}` TTL 24h |
| Refresh token blacklist | core | `auth:refresh:blacklist:{jti}` |

---

## 8. API Gateway 라우팅

| Path | 대상 | 비고 |
|---|---|---|
| `/api/members/**`, `/api/auth/**` | core | login은 public |
| `/api/baskets/**`, `/api/point/**` | core | JWT |
| `/api/goods/**` | goods | 조회는 일부 public |
| `/api/order/**` | order | JWT |
| `/api/payments/**` | payment | JWT |
| `/api/claim/**` | claim | JWT |
| `/internal/**` | (Gateway에서 차단) | — |

### JWT 처리 (추천)

- **Gateway에서**: 서명 검증 + 만료 체크 + claims를 헤더 주입 (`X-User-No`, `X-User-Role`)
- **각 서비스에서**: 헤더 신뢰 + `@PreAuthorize`로 권한만 체크 (이중 검증 제거)
- `/internal/**`: Gateway가 라우팅 안 함 + 서비스 측 IP allowlist 또는 `X-Internal-Token`

---

## 9. 공통 라이브러리 의존 관계

```
common-core      → SystemEntity, SystemColumnAspect, ApiResponse, ApiError, ApiException, GlobalExceptionHandler
common-security  → common-core 의존. JwtTokenProvider, AuthPrincipal, HeaderPrincipalResolver, SecurityConfig 헬퍼
common-messaging → common-core 의존. OutboxEntity/Repo, OutboxPublisher, EventEnvelope, AbstractIdempotentConsumer, KafkaTopics(상수)
```

서비스별 의존:
- core/goods: `common-core` + `common-security` + `common-messaging`
- order/payment/claim: 모두 `common-core` + `common-security` + `common-messaging`
- gateway: `common-core` + `common-security` (messaging 불필요)

---

## 10. docker-compose 변경

```yaml
services:
  postgres:        # 5432
  redis:           # 6379
  kafka:           # KRaft 모드 (Zookeeper 미사용), 9092
  kafka-ui:        # 8090 (학습 편의)
  gateway-service: # 8080 → 외부 노출 (유일한 외부 진입점)
  core-service:    # 8081
  goods-service:   # 8082
  order-service:   # 8083
  payment-service: # 8084
  claim-service:   # 8085

위 compose 구조 역시 목표 상태 예시다.
현재 단계 1에서는 이 전체를 한 번에 맞추지 않는다.
우선순위는 `order-service 경계 정의 → 모듈 스캐폴딩 → 주문 책임 이동 → 나머지 서비스 연동` 순서다.
```

서비스 간은 컨테이너 이름으로 디스커버리 (예: `http://goods-service:8082`).

---

## 11. FO 변경

- `fo/src/lib/api-client.ts` base URL **그대로 8080** (Gateway가 흡수). `NEXT_PUBLIC_API_BASE_URL` 유지.
- 도메인별 API 모듈 경로 변경 없음 (Gateway가 path 기반 라우팅).
- PG return route(`fo/src/app/api/order/payment/return/route.ts`) 변경 없음.
- **추가 필요**: Saga 비동기 특성상 결제 직후 주문 상태가 `PENDING`일 수 있음 → 주문 완료 화면에 **2초 간격 폴링(최대 10초)** 로직 추가.

---

## 12. 마이그레이션 순서 (Phase별, 각 단계 동작 검증)

| Phase | 작업 | 검증 |
|---|---|---|
| **1. 인프라/공통** | Gradle 멀티모듈 전환, common-lib 3개 추출, 기존 코드는 `monolith` 모듈로 유지. docker-compose에 redis/kafka 추가 | 기존 빌드/테스트 통과 |
| **2. Gateway 도입** | gateway-service 추가, 모든 트래픽이 8080→gateway→monolith:8081로 흐르도록 | FO 골든패스(로그인→장바구니→주문→결제) 정상 |
| **3. goods-service 분리** | 가장 독립적. 테이블/코드 이전, FK(`order_detail.goods_no`) 끊고 스냅샷 컬럼 추가, monolith는 Feign으로 호출 | 상품 조회/주문 시 스냅샷 정상 |
| **4. core-service 분리** | member+basket+point 한번에. **Outbox/Kafka 첫 도입**. Order→Basket을 Kafka로 변경 | 주문 후 장바구니 비워짐(약간 지연) |
| **5. payment-service 분리** | **Saga Choreography 첫 도입**. OrderCreated→PaymentApproved 흐름 + 보상 | 결제 실패 시 주문 자동 취소 |
| **6. claim-service 분리** | 가장 복잡. **두 번째 Saga**, 두 이벤트 수신 후 완료 | 부분/전체 취소 시 PG 취소 + 포인트 환불 모두 정상 |
| **7. 정리** | monolith 모듈 제거, order-service만 남기고 책임 분리. JWT 검증 Gateway로 이전 | 전체 회귀 테스트 |

각 Phase 1~2일 권장. 매 Phase 끝에서 동작하는 시스템을 유지.

---

## 13. 검증 방법

- **Postman 컬렉션**: 회원가입→로그인→상품→장바구니→주문→결제→조회→클레임→환불 시퀀스. 매 Phase 실행.
- **FO 골든패스**: UI로 위 흐름. Phase 5 이후 **eventual consistency 관찰**(주문완료 화면 폴링).
- **단위 테스트**: 기존 Mockito 패턴 유지. Saga consumer는 `@EmbeddedKafka` + Testcontainers.
- **장애 시나리오**:
  - payment-service 다운 상태에서 주문 → outbox에 적재 후 복구 시 발행되는지
  - PG 호출 fail 강제 → 보상 트랜잭션 동작
  - Redis 분산락 contention(동시 주문) → 재고 정합성
- **Kafka UI**(8090)로 토픽/메시지 직접 확인.

---

## 14. 제외/보류 (학습 범위 외)

- **Distributed Tracing**(Zipkin/Tempo): `traceId` MDC 전파만 수동 구현
- **Kubernetes/Helm**: docker-compose로 충분
- **모니터링**(Prometheus/Grafana): Spring Actuator만 노출
- **Service Mesh**(Istio), **Schema Registry**(Avro), **CDC/Debezium**, **Multi-region/HA**, **Eureka/Consul**, **Read replica 실 분리**: 미적용

---

## 15. Critical Files

### 참고/이해 (현재 코드)

- `api/src/main/kotlin/com/api/app/service/order/OrderServiceImpl.kt` (L91-180: 결합점)
- `api/src/main/kotlin/com/api/app/service/claim/ClaimServiceImpl.kt` (L37-208: 결합점)
- `api/src/main/kotlin/com/api/app/common/config/DataSourceConfig.kt`
- `api/src/main/kotlin/com/api/app/common/aop/SystemColumnAspect.kt`
- `api/src/main/kotlin/com/api/app/common/security/SecurityConfig.kt`
- `fo/src/lib/api-client.ts`

### 신규/대수정

- `api/settings.gradle.kts` (1줄 → 멀티모듈)
- `api/build.gradle.kts` (subprojects 공통 설정)
- `api/{common-lib/*, core-service, goods-service, order-service, payment-service, claim-service, gateway-service}/build.gradle.kts`
- `docker-compose.yml` (postgres + redis + kafka + 6개 서비스)
- 각 서비스의 `src/main/resources/db/migration/V*__*.sql`

### FO 소폭 수정

- `fo/src/app/order/complete/page.tsx` (Saga 비동기 폴링 추가, Phase 5 이후)

---

## 16. 참고 가능한 Skills

`.agents/skills/` 하위:

- `cloud-design-patterns` — Saga, Outbox, Circuit Breaker 등 패턴 레퍼런스
- `kotlin-springboot` — Spring Boot + Kotlin 베스트 프랙티스
- `kotlin-backend-jpa-entity-mapping` — JPA 엔티티 설계 (스냅샷 컬럼, FK 제거 등)
- `postgresql-optimization`, `postgresql-code-review` — 스키마 설계, 인덱스
