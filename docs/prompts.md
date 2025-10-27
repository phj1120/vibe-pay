# VibePay 리버스 엔지니어링 및 재구축 프롬프트 모음

> 이 파일은 복붙해서 사용할 수 있는 프롬프트만 모아놓은 문서입니다.
> 각 프롬프트를 순서대로 Claude에게 전달하세요.

---

## 🔍 Phase 0: 리버스 엔지니어링

### 프롬프트: 요구사항 문서 생성

```
현재 vibe-pay 프로젝트를 분석하여 docs/requirements/ 디렉토리에 요구사항 문서 8개를 생성해주세요.

분석 대상:
- vibe-pay-backend/src/main/java/ (87개 Java 파일)
- vibe-pay-frontend/pages/ (12개 Vue 파일)
- vibe-pay-backend/src/main/resources/ (schema.sql, Mapper XML, application.yml)

생성할 문서:
1. phase1-data-layer.md
2. phase2-member-domain.md
3. phase2-product-domain.md
4. phase2-rewardpoints-domain.md
5. phase2-payment-domain.md
6. phase2-order-domain.md
7. data-requirements.md
8. test-scenarios.md

각 문서는 docs/plan.md에 정의된 구조대로 작성하되, 현재 코드를 실제로 분석하여 구체적인 내용을 채워주세요.

한 번에 모든 문서를 생성하고, 완료 후 요약을 보고해주세요.
```

**검증**:
- [ ] `docs/requirements/` 디렉토리에 8개 파일 생성 확인
- [ ] 각 문서 내용 간단히 검토

---

## 🏗️ Phase 1: 데이터 레이어 구축

### 프롬프트: 전체 데이터 레이어 생성

```
docs/requirements/phase1-data-layer.md를 읽고 전체 데이터 레이어를 생성해주세요.

생성 대상:
1. vibe-pay-backend/src/main/resources/schema.sql (8개 테이블)
2. Entity 클래스 8개 (Member, Product, Order, OrderItem, Payment, RewardPoints, PointHistory, PaymentInterfaceRequestLog)
3. Enum 클래스 6개 (PaymentMethod, PaymentStatus, PayType, PgCompany, OrderStatus, TransactionType)
4. DTO 클래스 (각 Entity별 Request/Response DTO)
5. Mapper 인터페이스 + XML 8개 (기본 CRUD: insert, selectById, selectAll, update, delete)

필수 준수:
- docs/conventions/api/ 컨벤션 100% 준수
- CLAUDE.md의 패키지 구조 준수
- Lombok 활용 (@Getter, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- Order ID, Payment ID는 String 타입 (17자)
- 모든 Timestamp는 LocalDateTime 타입

한 번에 모든 파일을 생성하고, 생성된 파일 목록을 보고해주세요.
```

**검증**:
- [ ] 파일 약 40개 생성 확인
- [ ] `./mvnw compile` 실행하여 컴파일 에러 없음 확인
- [ ] schema.sql을 DB에 실행하여 테이블 생성 확인

---

## 👤 Phase 2-1: Member 도메인

### 프롬프트: Member 도메인 생성

```
docs/requirements/phase2-member-domain.md를 읽고 Member 도메인의 비즈니스 로직과 프론트엔드를 생성해주세요.

생성 대상:
**API**:
- com.vibe.pay.backend.member.MemberService
- com.vibe.pay.backend.member.MemberController (5개 엔드포인트)

**FO**:
- vibe-pay-frontend/pages/members/index.vue (회원 목록)
- vibe-pay-frontend/pages/members/[id].vue (회원 상세)

참조:
- Phase 1의 Member, MemberMapper는 읽기만 (수정 금지)

필수 준수:
- docs/conventions/api/ 컨벤션
- docs/conventions/fo/ 컨벤션

완료 후 API 엔드포인트 목록과 FO 화면 구성을 보고해주세요.
```

**검증**:
- [ ] API 5개 엔드포인트 생성 확인
- [ ] `./mvnw compile` 컴파일 확인
- [ ] Postman으로 GET /api/members 테스트
- [ ] 브라우저에서 회원 목록 화면 확인

---

## 📦 Phase 2-2: Product 도메인

### 프롬프트: Product 도메인 생성

```
docs/requirements/phase2-product-domain.md를 읽고 Product 도메인을 생성해주세요.

생성 대상:
**API**:
- com.vibe.pay.backend.product.ProductService
- com.vibe.pay.backend.product.ProductController

**FO**:
- vibe-pay-frontend/pages/products/index.vue (상품 목록)
- vibe-pay-frontend/pages/products/[id].vue (상품 상세)

필수 준수:
- docs/conventions/api/ 컨벤션
- docs/conventions/fo/ 컨벤션

완료 후 결과를 보고해주세요.
```

**검증**:
- [ ] API 5개 엔드포인트 생성 확인
- [ ] 컴파일 확인
- [ ] Postman으로 GET /api/products 테스트
- [ ] 브라우저에서 상품 목록 화면 확인

---

## 💰 Phase 2-3: RewardPoints 도메인

### 프롬프트: RewardPoints 도메인 생성

```
docs/requirements/phase2-rewardpoints-domain.md를 읽고 RewardPoints 도메인을 생성해주세요.

생성 대상:
**API**:
- com.vibe.pay.backend.rewardpoints.RewardPointsService (적립/사용/조회 로직)
- com.vibe.pay.backend.pointhistory.PointHistoryService (이력 관리)
- com.vibe.pay.backend.rewardpoints.RewardPointsController

**FO**:
- vibe-pay-frontend/pages/members/[id].vue에 포인트 섹션 추가

비즈니스 규칙:
- 포인트는 0 이상이어야 함
- 포인트 사용 시 잔액 부족하면 예외 발생
- 모든 포인트 변동은 PointHistory에 기록

필수 준수:
- docs/conventions/api/ 컨벤션
- docs/conventions/fo/ 컨벤션

완료 후 포인트 적립/사용 로직을 설명해주세요.
```

**검증**:
- [ ] 포인트 적립 API 테스트
- [ ] 포인트 사용 시 잔액 부족 체크 확인
- [ ] PointHistory 테이블에 기록 확인
- [ ] 브라우저에서 회원 상세 페이지의 포인트 섹션 확인

---

## 💳 Phase 2-4: Payment 도메인 (Part 1 - Factory & Adapter)

### 프롬프트: Factory와 Adapter 패턴 구현

```
docs/requirements/phase2-payment-domain.md를 읽고, 먼저 Factory와 Adapter 패턴을 구현해주세요.

생성 대상:
**1. Factory 패턴**:
- com.vibe.pay.backend.payment.factory.PaymentProcessorFactory
- com.vibe.pay.backend.payment.factory.PaymentGatewayFactory

**2. Adapter 패턴 (PG사별)**:
- com.vibe.pay.backend.payment.gateway.PaymentGateway (인터페이스)
- com.vibe.pay.backend.payment.gateway.InicisAdapter
- com.vibe.pay.backend.payment.gateway.NicePayAdapter
- com.vibe.pay.backend.payment.gateway.TossAdapter

**3. Strategy 패턴 (결제 수단별)**:
- com.vibe.pay.backend.payment.processor.PaymentProcessor (인터페이스)
- com.vibe.pay.backend.payment.processor.CreditCardPaymentProcessor
- com.vibe.pay.backend.payment.processor.PointPaymentProcessor

**4. Util 클래스**:
- com.vibe.pay.backend.util.PgWeightSelector (가중치 기반 PG 선택)
- com.vibe.pay.backend.util.HashUtils (서명 생성)
- com.vibe.pay.backend.util.WebClientUtil (HTTP 통신)

필수 준수:
- docs/conventions/api/ 컨벤션
- application.yml의 PG 설정 참조

완료 후 각 클래스의 역할을 설명해주세요.
```

**검증**:
- [ ] 컴파일 에러 없음 확인
- [ ] Factory 패턴 구조 확인
- [ ] Adapter 패턴 구조 확인

---

## 💳 Phase 2-4: Payment 도메인 (Part 2 - Service & Controller)

### 프롬프트: PaymentService와 Controller 구현

```
이어서 PaymentService와 Controller를 생성해주세요.

생성 대상:
**1. Service**:
- com.vibe.pay.backend.payment.PaymentService
  - initiate(PaymentInitRequest): PaymentInitResponse
  - confirm(PaymentConfirmRequest): PaymentConfirmResponse
  - netCancel(String paymentId): PaymentNetCancelResponse

- com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogService
  - logRequest(String pgCompany, String endpoint, String request, String response)

**2. Controller**:
- com.vibe.pay.backend.payment.PaymentController
  - POST /api/payments/initiate
  - POST /api/payments/confirm
  - POST /api/payments/cancel

**3. DTO 클래스** (약 10개):
- PaymentInitRequest, PaymentInitResponse
- PaymentConfirmRequest, PaymentConfirmResponse
- PaymentCancelRequest, PaymentCancelResponse
- PaymentNetCancelRequest, PaymentNetCancelResponse
- InicisConfirmRequest, InicisConfirmResponse
- NicePayConfirmRequest, NicePayConfirmResponse

필수 준수:
- docs/conventions/api/ 컨벤션
- PaymentInterfaceRequestLog에 모든 PG 통신 기록

완료 후 결제 플로우를 설명해주세요.
```

**검증**:
- [ ] 컴파일 에러 없음
- [ ] Postman으로 POST /api/payments/initiate 테스트
- [ ] PG 파라미터 생성 확인
- [ ] 가중치 선택 로직 확인

---

## 💳 Phase 2-4: Payment 도메인 (Part 3 - FO)

### 프롬프트: 결제 프론트엔드 구현

```
이제 결제 관련 프론트엔드를 생성해주세요.

생성 대상:
**1. 페이지**:
- vibe-pay-frontend/pages/order/index.vue (주문서 작성)
  - 회원 선택 (드롭다운)
  - 상품 선택 (다중 선택, 수량 입력)
  - PG사 선택 (INICIS/NICEPAY/가중치 라디오 버튼)
  - 결제수단 선택 (카드/포인트 라디오 버튼)
  - 총액 계산 및 표시
  - "결제하기" 버튼 → 결제 초기화 API 호출 → 팝업 오픈

- vibe-pay-frontend/pages/order/popup.vue (PG 결제 팝업)
  - PG사별 팝업 크기 동적 조정 (INICIS: 720x700, NICEPAY: 500x800)
  - PostMessage로 부모 창과 통신
  - 결제 완료 시 progress-popup 오픈 후 자동 닫기

**2. 플러그인**:
- vibe-pay-frontend/plugins/inicis.client.ts
  - INICIS SDK 로드 및 초기화
  - INIStdPay.pay() 호출

- vibe-pay-frontend/plugins/nicepay.client.ts
  - NicePay SDK 로드 및 초기화
  - 결제 폼 동적 생성 및 submit

필수 준수:
- docs/conventions/fo/ 컨벤션
- PostMessage 통신 반드시 구현
- 팝업 차단 방지 로직

완료 후 화면 플로우를 설명해주세요.
```

**검증**:
- [ ] 주문서 화면 렌더링 확인
- [ ] PG 선택 라디오 버튼 동작 확인
- [ ] "결제하기" 버튼 클릭 시 팝업 오픈 확인
- [ ] 팝업 크기가 PG사별로 다른지 확인

---

## 📦 Phase 2-5: Order 도메인 (Part 1 - Command Pattern)

### 프롬프트: Command 패턴 구현

```
docs/requirements/phase2-order-domain.md를 읽고, 먼저 Command 패턴을 구현해주세요.

생성 대상:
**Command 패턴**:
- com.vibe.pay.backend.order.command.OrderCommand (인터페이스)
  - execute(): String (orderId 반환)
  - rollback(): void

- com.vibe.pay.backend.order.command.CreateOrderCommand
  - execute(): 주문 생성 + OrderItem 생성
  - rollback(): 주문 삭제 + 포인트 환불 (포인트 결제 시)

- com.vibe.pay.backend.order.command.CancelOrderCommand
  - execute(): 주문 취소 + 결제 취소 + 포인트 환불
  - rollback(): N/A

- com.vibe.pay.backend.order.command.OrderCommandInvoker
  - invoke(OrderCommand): String
  - rollback(OrderCommand): void

필수 준수:
- docs/conventions/api/ 컨벤션

완료 후 Command 패턴의 흐름을 설명해주세요.
```

**검증**:
- [ ] 컴파일 에러 없음
- [ ] Command 패턴 구조 확인

---

## 📦 Phase 2-5: Order 도메인 (Part 2 - Service & Controller)

### 프롬프트: OrderService와 Controller 구현

```
이어서 OrderService와 Controller를 생성해주세요.

생성 대상:
**1. Service**:
- com.vibe.pay.backend.order.OrderService
  - createOrder(OrderRequest): OrderResponse
    - Payment 승인 확인
    - CreateOrderCommand 실행
    - 실패 시 PaymentService.netCancel() 호출 (망취소)

  - getOrder(String orderId): OrderDetailDto

  - cancelOrder(String orderId): void
    - CancelOrderCommand 실행

**2. Controller**:
- com.vibe.pay.backend.order.OrderController
  - POST /api/orders (주문 생성)
  - GET /api/orders/{orderId} (주문 상세 조회)
  - DELETE /api/orders/{orderId} (주문 취소)

비즈니스 로직:
1. 주문 생성 시 Payment가 APPROVED 상태인지 확인
2. OrderItem은 최소 1개 이상 필요
3. 주문 생성 실패 시 자동 망취소
4. 포인트 결제는 주문 생성과 동시에 포인트 차감

필수 준수:
- docs/conventions/api/ 컨벤션
- 망취소 로직 반드시 구현

완료 후 주문 생성 플로우를 설명해주세요.
```

**검증**:
- [ ] 컴파일 에러 없음
- [ ] Postman으로 POST /api/orders 테스트 (결제 승인 후)
- [ ] 망취소 로직 확인

---

## 📦 Phase 2-5: Order 도메인 (Part 3 - FO)

### 프롬프트: 주문 프론트엔드 구현

```
마지막으로 주문 관련 프론트엔드를 생성해주세요.

생성 대상:
**1. 페이지**:
- vibe-pay-frontend/pages/order/progress-popup.vue
  - 결제 진행 상태 표시
  - 결제 승인 완료 대기
  - 주문 생성 API 호출 (POST /api/orders)
  - 성공 시 부모 창을 /order/complete로 리디렉션
  - 실패 시 부모 창을 /order/failed로 리디렉션
  - PostMessage로 부모 창과 통신

- vibe-pay-frontend/pages/order/complete.vue
  - 주문 완료 화면
  - 주문 ID로 상세 정보 조회 (GET /api/orders/{orderId})
  - 주문 정보, 결제 정보, 주문 상품 목록 표시
  - "확인" 버튼 → 메인 페이지로 이동

- vibe-pay-frontend/pages/order/failed.vue
  - 주문 실패 화면
  - 실패 사유 표시
  - "다시 시도" 버튼 → 주문서로 이동

- vibe-pay-frontend/pages/order/close.vue
  - 팝업 닫기 전용 페이지 (빈 페이지)

필수 준수:
- docs/conventions/fo/ 컨벤션
- PostMessage 통신 구현
- 로딩 스피너 표시

완료 후 전체 주문 플로우를 설명해주세요.
```

**검증**:
- [ ] 전체 플로우 테스트 (주문서 → 결제 팝업 → 진행 팝업 → 완료)
- [ ] progress-popup에서 주문 생성 API 호출 확인
- [ ] 완료 페이지에서 주문 정보 표시 확인

---

## 🧪 Phase 3: 통합 테스트

### 프롬프트: 통합 테스트 실행

```
docs/requirements/test-scenarios.md와 docs/requirements/data-requirements.md를 읽고 7개 시나리오를 테스트해주세요.

테스트 시나리오:
1. INICIS 카드 결제 성공
2. NICEPAY 카드 결제 성공
3. 포인트 결제 성공
4. 가중치 기반 PG 선택
5. 망취소 (주문 생성 실패)
6. 회원/상품 CRUD
7. 주문 취소

실행 방법:
1. 각 시나리오별로 필요한 초기 데이터를 SQL로 INSERT
2. API 또는 브라우저를 통해 플로우 실행
3. 예상 결과와 실제 결과 비교
4. 성공/실패 여부 보고

완료 후 전체 테스트 결과를 표로 정리해주세요:
| 시나리오 | 상태 | 비고 |
|---------|------|------|
| 1. INICIS 카드 결제 | ✅/❌ | ... |
| ... | ... | ... |
```

**검증**:
- [ ] 모든 시나리오 성공 확인
- [ ] 망취소 정상 동작 확인
- [ ] 포인트 적립/사용 정확성 확인

---

## 📋 요약

### 실행 순서
1. **Phase 0**: 리버스 엔지니어링 (요구사항 문서 8개 생성)
2. **Phase 1**: 데이터 레이어 (약 40개 파일 생성)
3. **Phase 2-1**: Member 도메인
4. **Phase 2-2**: Product 도메인
5. **Phase 2-3**: RewardPoints 도메인
6. **Phase 2-4**: Payment 도메인 (Part 1 → Part 2 → Part 3)
7. **Phase 2-5**: Order 도메인 (Part 1 → Part 2 → Part 3)
8. **Phase 3**: 통합 테스트

### 예상 소요 시간
- Phase 0: 30분
- Phase 1: 40분
- Phase 2-1, 2-2: 각 20분
- Phase 2-3: 30분
- Phase 2-4: 60분 (Part 1, 2, 3 합계)
- Phase 2-5: 50분 (Part 1, 2, 3 합계)
- Phase 3: 40분
- **총 약 5시간**

### 핵심 원칙
- ✅ 각 프롬프트를 순서대로 실행
- ✅ 검증 포인트에서 반드시 확인
- ✅ 에러 발생 시 Claude에게 에러 메시지 전달
- ✅ 컨벤션 100% 준수
- ✅ 컨텍스트는 유연하게 관리 (필요하면 리셋)
