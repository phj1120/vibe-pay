# VibePay 리버스 엔지니어링 및 재구축 계획

> **목적**: 현재 VibePay 프로젝트(기능적으로 완성, 코드 품질 개선 필요)를 리버스 엔지니어링하여 요구사항 문서를 추출하고, 이를 바탕으로 컨벤션을 준수하는 고품질 프로젝트로 재구축

> **실행 가이드**: 실제 실행할 프롬프트는 `docs/prompts.md` 참고. 이 문서는 전체 전략과 구조를 설명합니다.

---

## 📊 현황 분석

### 현재 프로젝트 규모
- **Backend**: Java 87개 파일 (Spring Boot 3.5.5 + MyBatis + PostgreSQL)
- **Frontend**: Vue 12개 파일 (Nuxt.js 3.12.2 + Vuetify)
- **PG 연동**: INICIS, NicePay, Toss (Adapter Pattern)
- **도메인**: Member, Product, Order, Payment, RewardPoints

### 기존 자산
- ✅ `CLAUDE.md`: 프로젝트 개요 및 아키텍처 설명
- ✅ `docs/conventions/`: API, FO, SQL 컨벤션 문서
- ✅ 완성된 기능 (결제 플로우, 포인트 시스템, 망취소 등)

---

## 🎯 핵심 전략

### 1. 리버스 엔지니어링 원칙
- **도메인 중심 분석**: Entity/Service/Controller → 비즈니스 규칙 추출
- **데이터 플로우 추적**: 화면 → API → DB 전체 흐름 문서화
- **패턴 식별**: Factory, Adapter, Command 등 디자인 패턴 추출
- **데이터 적재 방법**: 초기 데이터 및 테스트 시나리오 정의

### 2. 재구축 핵심 원칙
**Phase 분리 전략**:
- ✅ **Phase 1**: 전체 데이터 레이어를 한번에 생성 → 타입/네이밍 일관성 확보
- ✅ **Phase 2**: 도메인별로 FO + API 동시 개발 → 불일치 방지
- ✅ **컨텍스트 격리**: 각 도메인은 새 컨텍스트에서 개발 → 잡음 제거

---

## 📁 문서 구조

### requirements/ 디렉토리
```
docs/requirements/
├── phase1-data-layer.md              # Phase 1 전용
│   ├── 데이터베이스 스키마 (8개 테이블)
│   ├── 전체 Entity 정의 (Member, Product, Order, Payment 등)
│   ├── 전체 DTO 정의 (Request/Response)
│   ├── Enum 정의 (PaymentMethod, PaymentStatus, PgCompany 등)
│   └── 기본 CRUD Mapper 명세 (insert, selectById, selectAll, update, delete)
│
├── phase2-member-domain.md           # Task 2-1: Member 도메인
│   ├── MemberService 비즈니스 로직
│   ├── MemberController API 명세
│   └── FO: pages/members/index.vue, [id].vue 요구사항
│
├── phase2-product-domain.md          # Task 2-2: Product 도메인
│   ├── ProductService 비즈니스 로직
│   ├── ProductController API 명세
│   └── FO: pages/products/index.vue, [id].vue 요구사항
│
├── phase2-rewardpoints-domain.md     # Task 2-3: RewardPoints 도메인
│   ├── RewardPointsService (포인트 적립/사용/조회)
│   ├── PointHistoryService (트랜잭션 이력)
│   ├── RewardPointsController API 명세
│   └── FO: 포인트 정보 표시 컴포넌트
│
├── phase2-payment-domain.md          # Task 2-4: Payment 도메인 (가장 복잡)
│   ├── Factory 패턴 (PaymentProcessorFactory, PaymentGatewayFactory)
│   ├── Adapter 패턴 (InicisAdapter, NicePayAdapter, TossAdapter)
│   ├── Strategy 패턴 (CreditCardPaymentProcessor, PointPaymentProcessor)
│   ├── Util (PgWeightSelector, HashUtils, WebClientUtil)
│   ├── PaymentService (initiate, confirm, netCancel)
│   ├── PaymentController API 명세
│   └── FO: pages/order/index.vue, popup.vue, plugins/inicis.client.ts, nicepay.client.ts
│
├── phase2-order-domain.md            # Task 2-5: Order 도메인
│   ├── Command 패턴 (CreateOrderCommand, CancelOrderCommand, OrderCommandInvoker)
│   ├── OrderService (결제 승인 후 주문 생성, 실패 시 망취소)
│   ├── OrderController API 명세
│   └── FO: pages/order/progress-popup.vue, complete.vue, failed.vue
│
├── data-requirements.md              # 데이터 적재 요구사항
│   ├── 초기 데이터 (시스템 운영에 필수)
│   │   ├── 테스트 회원 3명 (포인트 보유/무보유/VIP)
│   │   ├── 상품 10개 (카테고리별, 가격대별)
│   │   └── 각 회원의 초기 포인트 잔액
│   ├── 도메인 간 관계 데이터
│   │   ├── 회원 - RewardPoints: 1:1 (회원 생성 시 자동 생성)
│   │   ├── 주문 - OrderItem: 1:N (최소 1개 이상)
│   │   └── 결제 - 주문: 1:1 (결제 승인 후 주문 생성)
│   └── 테스트 시나리오별 데이터
│       ├── 카드 결제 성공
│       ├── 포인트 결제 성공
│       ├── 가중치 PG 선택
│       └── 망취소 시나리오
│
└── test-scenarios.md                 # 통합 테스트 시나리오
    ├── 시나리오 1: 카드 결제 플로우 (INICIS)
    ├── 시나리오 2: 카드 결제 플로우 (NICEPAY)
    ├── 시나리오 3: 포인트 결제 플로우
    ├── 시나리오 4: 가중치 기반 PG 선택
    ├── 시나리오 5: 망취소 (주문 생성 실패)
    └── 시나리오 6: 회원/상품 CRUD
```

---

## 🔄 Phase 1: 전체 데이터 레이어 구축

### 목표
모든 테이블, Entity, DTO, 기본 CRUD Mapper를 **한 번에 생성**하여 데이터 구조의 일관성 확보

### 생성 대상 (약 40개 파일)

#### 1. 데이터베이스
- `schema.sql`: 8개 테이블 정의
  - member, product, reward_points, point_history
  - orders, order_item, payment, payment_interface_request_log

#### 2. Entity 클래스 (8개)
```
com.vibe.pay.backend.member.Member
com.vibe.pay.backend.product.Product
com.vibe.pay.backend.rewardpoints.RewardPoints
com.vibe.pay.backend.pointhistory.PointHistory
com.vibe.pay.backend.order.Order
com.vibe.pay.backend.order.OrderItem
com.vibe.pay.backend.payment.Payment
com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLog
```

**공통 규칙**:
- Lombok 활용 (`@Getter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Order ID, Payment ID는 `String` 타입 (17자: `YYYYMMDD + 8자리 시퀀스`)
- 모든 Timestamp는 `LocalDateTime` 타입
- 컨벤션 준수 (`docs/conventions/api/` 참고)

#### 3. Enum 클래스 (6개)
```
com.vibe.pay.backend.enums.PaymentMethod      # CARD, POINT
com.vibe.pay.backend.enums.PaymentStatus      # READY, APPROVED, CANCELLED, COMPLETED
com.vibe.pay.backend.enums.PayType            # CARD, BANK, VBANK, MOBILE
com.vibe.pay.backend.enums.PgCompany          # INICIS, NICEPAY, TOSS, WEIGHT
com.vibe.pay.backend.enums.OrderStatus        # PENDING, COMPLETED, CANCELLED
com.vibe.pay.backend.enums.TransactionType    # EARN, USE, REFUND
```

#### 4. DTO 클래스 (각 Entity별 2-3개)
- Request DTO (API 요청용)
- Response DTO (API 응답용)
- Detail DTO (상세 조회용, 필요 시)

예시:
```
com.vibe.pay.backend.member.MemberRequest
com.vibe.pay.backend.member.MemberResponse
com.vibe.pay.backend.product.ProductRequest
com.vibe.pay.backend.product.ProductResponse
...
```

#### 5. Mapper 인터페이스 + XML (8개 × 2)
각 도메인별 Mapper 인터페이스 및 MyBatis XML 파일

**기본 CRUD 메서드만**:
```java
void insert(Entity entity);
Entity selectById(Long id); // 또는 String id
List<Entity> selectAll();
void update(Entity entity);
void delete(Long id); // 또는 String id
```

Mapper 목록:
```
MemberMapper.java + mapper/MemberMapper.xml
ProductMapper.java + mapper/ProductMapper.xml
RewardPointsMapper.java + mapper/RewardPointsMapper.xml
PointHistoryMapper.java + mapper/PointHistoryMapper.xml
OrderMapper.java + mapper/OrderMapper.xml
OrderItemMapper.java + mapper/OrderItemMapper.xml
PaymentMapper.java + mapper/PaymentMapper.xml
PaymentInterfaceRequestLogMapper.java + mapper/PaymentInterfaceRequestLogMapper.xml
```

### 검증 방법
1. PostgreSQL에서 테이블 생성 확인: `\dt`
2. 각 Mapper의 기본 CRUD 메서드 단위 테스트
3. Foreign Key 제약조건 확인

### 컨텍스트 리셋 ✂️
Phase 1 완료 후 **반드시 새 컨텍스트**에서 Phase 2 시작

---

## 🚀 Phase 2: 도메인별 비즈니스 로직 + FO 개발

### 공통 원칙
- 각 도메인은 **새 컨텍스트**에서 개발
- Phase 1의 데이터 레이어는 **읽기만** (수정 금지)
- API와 FO를 **동시에** 개발하여 불일치 방지
- 컨벤션 100% 준수

---

### Task 2-1: Member 도메인

**컨텍스트**: 새로 시작 ✨

**참조**: Phase 1의 Member, MemberMapper (읽기만)

**API 생성**:
```
com.vibe.pay.backend.member.MemberService
  - 추가 비즈니스 로직 (이메일 중복 체크 등)

com.vibe.pay.backend.member.MemberController
  - GET /api/members          # 목록 조회
  - GET /api/members/{id}     # 상세 조회
  - POST /api/members         # 생성
  - PUT /api/members/{id}     # 수정
  - DELETE /api/members/{id}  # 삭제
```

**FO 생성**:
```
pages/members/index.vue       # 회원 목록 (테이블 형식)
pages/members/[id].vue        # 회원 상세 (정보 조회/수정)
```

**FO 요구사항**:
- Vuetify 테이블 컴포넌트 사용
- 회원 생성 시 다이얼로그 사용
- 회원 상세 페이지에서 포인트 정보도 표시 (준비만, Task 2-3에서 완성)

**검증**:
- Postman: 회원 CRUD API 테스트
- 브라우저: 회원 목록 → 생성 → 상세 → 수정 → 삭제 플로우

**컨텍스트 리셋** ✂️

---

### Task 2-2: Product 도메인

**컨텍스트**: 새로 시작 ✨

**참조**: Phase 1의 Product, ProductMapper

**API 생성**:
```
com.vibe.pay.backend.product.ProductService
  - 추가 비즈니스 로직 (가격 유효성 검증 등)

com.vibe.pay.backend.product.ProductController
  - GET /api/products         # 목록 조회
  - GET /api/products/{id}    # 상세 조회
  - POST /api/products        # 생성
  - PUT /api/products/{id}    # 수정
  - DELETE /api/products/{id} # 삭제
```

**FO 생성**:
```
pages/products/index.vue      # 상품 목록 (카드 형식)
pages/products/[id].vue       # 상품 상세 (정보 조회/수정)
```

**FO 요구사항**:
- Vuetify 카드 컴포넌트 사용
- 상품 이미지 표시 (없으면 기본 이미지)
- 가격 포맷팅 (천 단위 콤마)

**검증**:
- Postman: 상품 CRUD API 테스트
- 브라우저: 상품 목록 → 생성 → 상세 → 수정 → 삭제 플로우

**컨텍스트 리셋** ✂️

---

### Task 2-3: RewardPoints 도메인

**컨텍스트**: 새로 시작 ✨

**참조**: Phase 1의 RewardPoints, PointHistory, Member

**API 생성**:
```
com.vibe.pay.backend.rewardpoints.RewardPointsService
  - getPointsByMemberId(Long memberId)      # 회원 포인트 조회
  - earnPoints(Long memberId, Double points) # 포인트 적립
  - usePoints(Long memberId, Double points)  # 포인트 사용
  - refundPoints(Long memberId, Double points) # 포인트 환불

com.vibe.pay.backend.pointhistory.PointHistoryService
  - getHistoryByMemberId(Long memberId)     # 포인트 이력 조회

com.vibe.pay.backend.rewardpoints.RewardPointsController
  - GET /api/members/{memberId}/points           # 포인트 조회
  - POST /api/members/{memberId}/points/earn     # 적립
  - POST /api/members/{memberId}/points/use      # 사용
  - GET /api/members/{memberId}/points/history   # 이력 조회
```

**비즈니스 규칙**:
- 포인트는 0 이상이어야 함
- 포인트 사용 시 잔액 부족하면 예외 발생
- 모든 포인트 변동은 PointHistory에 기록

**FO 생성**:
```
pages/members/[id].vue 수정
  - 포인트 정보 섹션 추가
  - 포인트 적립/사용 버튼 추가
  - 포인트 이력 테이블 추가
```

**검증**:
- Postman: 포인트 적립 → 잔액 조회 → 사용 → 이력 조회
- 브라우저: 회원 상세 페이지에서 포인트 조회/적립/사용

**컨텍스트 리셋** ✂️

---

### Task 2-4: Payment 도메인 (가장 복잡)

**컨텍스트**: 새로 시작 ✨

**참조**: Phase 1의 Payment, PaymentInterfaceRequestLog, Member, RewardPoints

**API 생성** (약 20개 파일):

#### 1. Factory 패턴
```
com.vibe.pay.backend.payment.factory.PaymentProcessorFactory
  - getProcessor(PaymentMethod method): PaymentProcessor

com.vibe.pay.backend.payment.factory.PaymentGatewayFactory
  - getGateway(PgCompany company): PaymentGateway
```

#### 2. Adapter 패턴 (PG사별)
```
com.vibe.pay.backend.payment.gateway.PaymentGateway (인터페이스)
  - generateParameters(PaymentInitRequest): Map<String, Object>
  - confirm(PaymentConfirmRequest): PaymentConfirmResponse
  - cancel(PaymentCancelRequest): PaymentCancelResponse
  - netCancel(PaymentNetCancelRequest): PaymentNetCancelResponse

com.vibe.pay.backend.payment.gateway.InicisAdapter
com.vibe.pay.backend.payment.gateway.NicePayAdapter
com.vibe.pay.backend.payment.gateway.TossAdapter
```

#### 3. Strategy 패턴 (결제 수단별)
```
com.vibe.pay.backend.payment.processor.PaymentProcessor (인터페이스)
  - process(PaymentRequest): PaymentResponse

com.vibe.pay.backend.payment.processor.CreditCardPaymentProcessor
  - PG사 선택 로직 포함
  - PgWeightSelector 활용

com.vibe.pay.backend.payment.processor.PointPaymentProcessor
  - 포인트 잔액 확인
  - RewardPointsService 호출
```

#### 4. Util 클래스
```
com.vibe.pay.backend.util.PgWeightSelector
  - selectPgCompany(): PgCompany (가중치 기반)

com.vibe.pay.backend.util.HashUtils
  - generateSignature(String data, String key): String

com.vibe.pay.backend.util.WebClientUtil
  - post(String url, Object body): Map<String, Object>
```

#### 5. Service & Controller
```
com.vibe.pay.backend.payment.PaymentService
  - initiate(PaymentInitRequest): PaymentInitResponse
  - confirm(PaymentConfirmRequest): PaymentConfirmResponse
  - netCancel(String paymentId): PaymentNetCancelResponse

com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogService
  - logRequest(String pgCompany, String endpoint, String request, String response)

com.vibe.pay.backend.payment.PaymentController
  - POST /api/payments/initiate   # 결제 초기화
  - POST /api/payments/confirm    # 결제 승인
  - POST /api/payments/cancel     # 결제 취소
```

**DTO 클래스** (약 10개):
```
PaymentInitRequest, PaymentInitResponse
PaymentConfirmRequest, PaymentConfirmResponse
PaymentCancelRequest, PaymentCancelResponse
PaymentNetCancelRequest, PaymentNetCancelResponse
InicisConfirmRequest, InicisConfirmResponse
NicePayConfirmRequest, NicePayConfirmResponse
```

**FO 생성**:
```
pages/order/index.vue
  - 주문서 작성 화면
  - 회원 선택 (드롭다운)
  - 상품 선택 (다중 선택, 수량 입력)
  - PG사 선택 (INICIS/NICEPAY/가중치 라디오 버튼)
  - 결제수단 선택 (카드/포인트 라디오 버튼)
  - 총액 계산 및 표시
  - "결제하기" 버튼 → 결제 초기화 API 호출 → 팝업 오픈

pages/order/popup.vue
  - PG 결제 팝업 화면
  - PG사별 팝업 크기 동적 조정
    - INICIS: 720x700
    - NICEPAY: 500x800
  - PostMessage로 부모 창과 통신
  - 결제 완료 시 progress-popup 오픈 후 자동 닫기

plugins/inicis.client.ts
  - INICIS SDK 로드 및 초기화
  - INIStdPay.pay() 호출

plugins/nicepay.client.ts
  - NicePay SDK 로드 및 초기화
  - 결제 폼 동적 생성 및 submit
```

**FO 요구사항**:
- PostMessage 통신 구현 필수
  - 부모 → 자식: 결제 파라미터 전달
  - 자식 → 부모: 결제 완료 알림
- 팝업 차단 방지 로직
- 로딩 스피너 표시

**검증**:
- Postman: 결제 초기화 → 승인 → 취소 플로우
- 브라우저:
  - INICIS 카드 결제 (테스트 카드)
  - NICEPAY 카드 결제 (테스트 카드)
  - 가중치 PG 선택 (랜덤 선택 확인)
  - 포인트 결제 (잔액 확인)

**컨텍스트 리셋** ✂️

---

### Task 2-5: Order 도메인

**컨텍스트**: 새로 시작 ✨

**참조**: Phase 1의 Order, OrderItem, Payment, Product, Member

**API 생성**:

#### 1. Command 패턴
```
com.vibe.pay.backend.order.command.OrderCommand (인터페이스)
  - execute(): String (orderId 반환)
  - rollback(): void

com.vibe.pay.backend.order.command.CreateOrderCommand
  - execute(): 주문 생성 + OrderItem 생성
  - rollback(): 주문 삭제 + 포인트 환불 (포인트 결제 시)

com.vibe.pay.backend.order.command.CancelOrderCommand
  - execute(): 주문 취소 + 결제 취소 + 포인트 환불
  - rollback(): N/A

com.vibe.pay.backend.order.command.OrderCommandInvoker
  - invoke(OrderCommand): String
  - rollback(OrderCommand): void
```

#### 2. Service & Controller
```
com.vibe.pay.backend.order.OrderService
  - createOrder(OrderRequest): OrderResponse
    - Payment 승인 확인
    - CreateOrderCommand 실행
    - 실패 시 PaymentService.netCancel() 호출 (망취소)

  - getOrder(String orderId): OrderDetailDto
  - cancelOrder(String orderId): void
    - CancelOrderCommand 실행

com.vibe.pay.backend.order.OrderController
  - POST /api/orders              # 주문 생성
  - GET /api/orders/{orderId}     # 주문 상세 조회
  - DELETE /api/orders/{orderId}  # 주문 취소
```

**비즈니스 로직**:
1. 주문 생성 시 Payment가 APPROVED 상태인지 확인
2. OrderItem은 최소 1개 이상 필요
3. 주문 생성 실패 시 자동 망취소 (PaymentService.netCancel)
4. 포인트 결제는 주문 생성과 동시에 포인트 차감

**FO 생성**:
```
pages/order/progress-popup.vue
  - 결제 진행 상태 표시
  - 결제 승인 완료 대기
  - 주문 생성 API 호출 (/api/orders)
  - 성공 시 부모 창을 /order/complete로 리디렉션
  - 실패 시 부모 창을 /order/failed로 리디렉션
  - PostMessage로 부모 창과 통신

pages/order/complete.vue
  - 주문 완료 화면
  - 주문 ID로 상세 정보 조회 (GET /api/orders/{orderId})
  - 주문 정보, 결제 정보, 주문 상품 목록 표시
  - "확인" 버튼 → 메인 페이지로 이동

pages/order/failed.vue
  - 주문 실패 화면
  - 실패 사유 표시
  - "다시 시도" 버튼 → 주문서로 이동

pages/order/close.vue
  - 팝업 닫기 전용 페이지 (빈 페이지)
```

**FO 요구사항**:
- progress-popup과 부모 창 간 PostMessage 통신
- 주문 생성 실패 시 에러 메시지 표시
- 로딩 스피너 표시

**검증**:
- 브라우저: 전체 플로우
  1. 주문서 작성 (회원, 상품 선택)
  2. 결제 팝업 (카드 정보 입력)
  3. 진행 팝업 (주문 생성 대기)
  4. 완료 페이지 (주문 정보 확인)
- 망취소 시나리오 (OrderService에서 의도적으로 예외 발생)

**컨텍스트 리셋** ✂️

---

## 🧪 Phase 3: 통합 테스트

**컨텍스트**: 새로 시작 ✨

### 테스트 시나리오 (7개)

#### 시나리오 1: INICIS 카드 결제 성공
1. 회원 생성 (포인트 10000)
2. 상품 생성 (5000원)
3. 주문서 작성 (INICIS, 카드)
4. 결제 팝업에서 테스트 카드 입력
5. 주문 생성 확인
6. 주문 상세 조회

**예상 결과**:
- 주문 상태: COMPLETED
- 결제 상태: COMPLETED
- 포인트 변동 없음

#### 시나리오 2: NICEPAY 카드 결제 성공
동일한 플로우를 NICEPAY로 진행

#### 시나리오 3: 포인트 결제 성공
1. 회원 생성 (포인트 10000)
2. 상품 생성 (5000원)
3. 주문서 작성 (포인트)
4. 주문 생성 확인

**예상 결과**:
- 주문 상태: COMPLETED
- 결제 상태: COMPLETED
- 포인트: 10000 → 5000 (차감)
- PointHistory에 USE 기록

#### 시나리오 4: 가중치 기반 PG 선택
1. 주문서에서 "가중치" 선택
2. 여러 번 반복하여 INICIS/NICEPAY가 확률적으로 선택되는지 확인

**예상 결과**:
- application.yml의 weight 설정에 따라 PG 선택
- PaymentInitResponse.selectedPgCompany에 실제 선택된 PG 반환

#### 시나리오 5: 망취소 (주문 생성 실패)
1. 결제 승인까지 정상 진행
2. OrderService.createOrder()에서 의도적으로 예외 발생
3. 자동 망취소 확인

**예상 결과**:
- 결제 상태: CANCELLED
- 주문 생성 안됨
- PaymentInterfaceRequestLog에 netCancel 요청 기록
- 포인트 결제였다면 포인트 환불

#### 시나리오 6: 회원/상품 CRUD
1. 회원 생성 → 조회 → 수정 → 삭제
2. 상품 생성 → 조회 → 수정 → 삭제

#### 시나리오 7: 주문 취소
1. 주문 완료 상태에서 취소 API 호출
2. 결제 취소 확인
3. 포인트 환불 확인 (포인트 결제인 경우)

**예상 결과**:
- 주문 상태: CANCELLED
- 결제 상태: CANCELLED
- 포인트 환불 완료

---

## 📝 실행 방법

### 🎯 실제 실행할 프롬프트는 `docs/prompts.md` 참고

`docs/prompts.md` 파일에 복붙 가능한 프롬프트가 순서대로 정리되어 있습니다.

**실행 순서**:
1. Phase 0: 리버스 엔지니어링 (요구사항 문서 생성)
2. Phase 1: 데이터 레이어
3. Phase 2-1 ~ 2-5: 5개 도메인 (Member, Product, RewardPoints, Payment, Order)
4. Phase 3: 통합 테스트

---

## 📚 요구사항 문서 구조

리버스 엔지니어링으로 생성될 문서들의 구조입니다.

**생성 문서 목록** (`docs/requirements/`):
1. `phase1-data-layer.md`
2. `phase2-member-domain.md`
3. `phase2-product-domain.md`
4. `phase2-rewardpoints-domain.md`
5. `phase2-payment-domain.md`
6. `phase2-order-domain.md`
7. `data-requirements.md`
8. `test-scenarios.md`

**각 문서의 표준 구조**:
```markdown
# [도메인명] 요구사항

## 개요
- 도메인 설명
- 주요 책임

## 데이터 모델
- Entity 정의
- 속성 및 타입
- 제약조건

## API 명세
### [HTTP METHOD] [URL]
- 요청 파라미터
- 요청 Body
- 응답 Body
- 비즈니스 로직
- 예외 처리

## 프론트엔드 요구사항
- 화면 구성
- 사용자 인터랙션
- API 호출 시점

## 검증 기준
- 단위 테스트
- 통합 테스트
- 예상 결과
```

**추출 방법**:
1. **Entity 분석**:
   - `vibe-pay-backend/src/main/java/com/vibe/pay/backend/**/[Entity].java` 읽기
   - 속성, 타입, 관계 추출

2. **Service 분석**:
   - `**/[Domain]Service.java` 읽기
   - 메서드 시그니처 및 비즈니스 로직 추출

3. **Controller 분석**:
   - `**/[Domain]Controller.java` 읽기
   - Endpoint, HTTP Method, Request/Response 추출

4. **Mapper 분석**:
   - `mapper/[Domain]Mapper.xml` 읽기
   - SQL 쿼리 패턴 추출

5. **Vue 페이지 분석**:
   - `vibe-pay-frontend/pages/**/[page].vue` 읽기
   - 화면 구성, 이벤트 핸들러, API 호출 추출

6. **디자인 패턴 추출**:
   - Factory, Adapter, Strategy, Command 패턴 식별
   - 클래스 다이어그램 문서화

---

## 🔨 재구축 전략

### 실행 전략: 반자동화 (최소한의 개입)

**핵심 원칙**:
- ✅ Phase 1은 **한 세션에서 연속 작업** (데이터 레이어 일관성 확보)
- ✅ Phase 2는 **도메인 복잡도에 따라 유연하게**:
  - 간단한 도메인(Member, Product): 연속 작업 가능
  - 복잡한 도메인(Payment, Order): Part로 분할하여 중간 검증
- ✅ **중요 검증 포인트**에서만 사람 개입
- ✅ 컨텍스트는 유연하게 관리 (필요 시 리셋 가능)

### 🎯 실제 실행 방법

**`docs/prompts.md` 파일을 열고 위에서 아래로 순서대로 프롬프트를 복붙하세요.**

각 프롬프트는:
- 생성 대상 명시
- 필수 준수 사항 명시
- 검증 포인트 포함

### 장점

1. **슬래시 커맨드 불필요**: 복붙만으로 실행
2. **유연한 컨텍스트**: 상황에 맞게 유지/리셋
3. **최소 개입**: 검증 포인트에서만 확인
4. **반자동화**: 대부분의 작업은 Claude가 연속 수행

---

## 📊 체크리스트

### Phase 1: 데이터 레이어
- [ ] schema.sql 생성 (8개 테이블)
- [ ] Entity 클래스 8개 생성
- [ ] Enum 클래스 6개 생성
- [ ] DTO 클래스 생성 (각 Entity별 2-3개)
- [ ] Mapper 인터페이스 8개 생성
- [ ] MyBatis XML 8개 생성
- [ ] 기본 CRUD 테스트 완료

### Phase 2-1: Member 도메인
- [ ] MemberService 생성
- [ ] MemberController 생성 (5개 엔드포인트)
- [ ] pages/members/index.vue 생성
- [ ] pages/members/[id].vue 생성
- [ ] 회원 CRUD 플로우 테스트 완료

### Phase 2-2: Product 도메인
- [ ] ProductService 생성
- [ ] ProductController 생성
- [ ] pages/products/index.vue 생성
- [ ] pages/products/[id].vue 생성
- [ ] 상품 CRUD 플로우 테스트 완료

### Phase 2-3: RewardPoints 도메인
- [ ] RewardPointsService 생성
- [ ] PointHistoryService 생성
- [ ] RewardPointsController 생성
- [ ] pages/members/[id].vue 포인트 섹션 추가
- [ ] 포인트 적립/사용/이력 테스트 완료

### Phase 2-4: Payment 도메인
- [ ] Factory 클래스 2개 생성
- [ ] Adapter 클래스 3개 생성
- [ ] Processor 클래스 2개 생성
- [ ] Util 클래스 3개 생성
- [ ] PaymentService 생성
- [ ] PaymentInterfaceRequestLogService 생성
- [ ] PaymentController 생성
- [ ] pages/order/index.vue 생성
- [ ] pages/order/popup.vue 생성
- [ ] plugins/inicis.client.ts 생성
- [ ] plugins/nicepay.client.ts 생성
- [ ] 결제 초기화/승인/취소 테스트 완료

### Phase 2-5: Order 도메인
- [ ] Command 클래스 4개 생성
- [ ] OrderService 생성 (망취소 로직 포함)
- [ ] OrderController 생성
- [ ] pages/order/progress-popup.vue 생성
- [ ] pages/order/complete.vue 생성
- [ ] pages/order/failed.vue 생성
- [ ] pages/order/close.vue 생성
- [ ] 전체 주문 플로우 테스트 완료

### Phase 3: 통합 테스트
- [ ] INICIS 카드 결제 성공
- [ ] NICEPAY 카드 결제 성공
- [ ] 포인트 결제 성공
- [ ] 가중치 PG 선택 확인
- [ ] 망취소 시나리오 성공
- [ ] 회원/상품 CRUD 성공
- [ ] 주문 취소 성공

---

## 🎯 성공 기준

### 코드 품질
- ✅ `docs/conventions/` 의 모든 컨벤션 100% 준수
- ✅ Lombok 일관성 있게 활용
- ✅ 네이밍 컨벤션 통일 (camelCase, PascalCase)
- ✅ 불필요한 주석 제거 (코드로 설명)

### 기능 동등성
- ✅ 원본 프로젝트의 모든 기능 구현
- ✅ API 응답 구조 동일
- ✅ 화면 플로우 동일
- ✅ 비즈니스 로직 동일

### 아키텍처 개선
- ✅ Factory, Adapter, Command, Strategy 패턴 정확히 적용
- ✅ 패키지 구조 명확히 분리
- ✅ 의존성 순환 없음
- ✅ 확장 가능한 구조

### 테스트 완료
- ✅ 7개 통합 테스트 시나리오 모두 성공
- ✅ 망취소 메커니즘 정상 동작
- ✅ 포인트 적립/사용 정확히 동작
- ✅ PG사별 결제 정상 동작

---

## 🚨 주의사항

### 컨텍스트 관리
- **반드시 각 Phase/Task 완료 후 컨텍스트 리셋**
- Phase 1의 데이터 레이어는 Phase 2에서 수정 금지 (읽기만)
- 새 컨텍스트에서는 이전 작업 내용을 명시적으로 참조

### 컨벤션 준수
- 모든 파일은 `docs/conventions/` 참고
- CLAUDE.md의 패키지 구조 엄격히 준수
- 일관성이 가장 중요

### API-FO 동기화
- 같은 컨텍스트에서 API와 FO를 동시에 개발
- DTO 구조와 FO 타입 정의 일치 필수
- API 응답 구조 변경 시 FO도 함께 수정

### 데이터 적재
- `data-requirements.md`에 정의된 초기 데이터 반드시 적재
- 테스트 시나리오 실행 전 데이터 준비 완료
- 회원 3명, 상품 10개는 필수

---

## 📚 참고 문서

### 프로젝트 기본
- `CLAUDE.md`: 프로젝트 개요, 아키텍처, 기술 스택
- `README.md`: 실행 방법

### 컨벤션
- `docs/conventions/api/`: API 개발 컨벤션
- `docs/conventions/fo/`: 프론트엔드 컨벤션
- `docs/conventions/sql/`: SQL 쿼리 작성 가이드

### 요구사항 (리버스 엔지니어링 후 생성)
- `docs/requirements/phase1-data-layer.md`
- `docs/requirements/phase2-*.md` (도메인별)
- `docs/requirements/data-requirements.md`
- `docs/requirements/test-scenarios.md`

---

## 🎉 기대 효과

1. **코드 품질 향상**
   - 컨벤션 100% 준수
   - 일관된 코딩 스타일
   - 가독성 및 유지보수성 향상

2. **아키텍처 개선**
   - 명확한 패키지 구조
   - 디자인 패턴 정확한 적용
   - 확장 가능한 구조

3. **문서화 자동화**
   - 코드와 동기화된 정확한 요구사항 문서
   - 신규 개발자 온보딩 시간 단축
   - 비즈니스 로직 명확한 이해

4. **재사용성**
   - 동일한 프로세스로 다른 프로젝트에도 적용 가능
   - 리버스 엔지니어링 방법론 체계화
   - 팀 내 지식 공유

5. **품질 보증**
   - 7개 통합 테스트 시나리오로 기능 검증
   - 컨텍스트 격리로 일관성 확보
   - API-FO 동시 개발로 불일치 방지

---

**이 계획서를 바탕으로 `/extract-requirements`와 `/rebuild` 슬래시 커맨드를 실행하면, 추가적인 프롬프트 없이 고품질 프로젝트가 재구축됩니다.**
