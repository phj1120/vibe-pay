# 3. 데이터 모델

## 3.1. 엔티티 정의 및 관계도

### 개요
Vibe Pay 시스템의 핵심 데이터 엔티티는 회원, 상품, 주문, 주문 항목, 결제, 포인트 내역, 리워드 포인트, 결제 인터페이스 요청 로그로 구성됩니다. 이들 엔티티는 상호 유기적으로 연결되어 결제 및 주문 처리의 전 과정을 지원합니다.

### 관계도 (ERD)

```mermaid
erDiagram
    MEMBER {
        BIGINT member_id PK
        VARCHAR(255) name
        VARCHAR(255) shipping_address
        VARCHAR(20) phone_number
        VARCHAR(255) email
        TIMESTAMP created_at
    }

    PRODUCT {
        BIGINT product_id PK
        VARCHAR(255) name
        DOUBLE PRECISION price
    }

    ORDERS {
        VARCHAR(255) order_id PK
        INTEGER ord_seq PK
        INTEGER ord_proc_seq PK
        VARCHAR(255) claim_id
        BIGINT member_id FK
        TIMESTAMP order_date
        DOUBLE PRECISION total_amount
        VARCHAR(50) status
    }

    ORDER_ITEM {
        BIGINT order_item_id PK
        VARCHAR(255) order_id FK
        INTEGER ord_seq FK
        INTEGER ord_proc_seq FK
        BIGINT product_id FK
        INTEGER quantity
        DOUBLE PRECISION price_at_order
    }

    PAYMENT {
        VARCHAR(255) payment_id PK
        BIGINT member_id FK
        VARCHAR(255) order_id FK
        VARCHAR(255) claim_id
        BIGINT amount
        VARCHAR(50) payment_method
        VARCHAR(50) pay_type
        VARCHAR(50) pg_company
        VARCHAR(50) status
        VARCHAR(50) order_status
        VARCHAR(255) transaction_id
        TIMESTAMP payment_date
    }

    POINT_HISTORY {
        BIGINT point_history_id PK
        BIGINT member_id FK
        BIGINT point_amount
        BIGINT balance_after
        VARCHAR(50) transaction_type
        VARCHAR(50) reference_type
        VARCHAR(255) reference_id
        VARCHAR(255) description
        TIMESTAMP created_at
    }

    REWARD_POINTS {
        BIGINT reward_points_id PK
        BIGINT member_id FK
        BIGINT points
        TIMESTAMP last_updated
    }

    PAYMENT_INTERFACE_REQUEST_LOG {
        BIGINT log_id PK
        VARCHAR(255) payment_id FK
        VARCHAR(50) request_type
        TEXT request_payload
        TEXT response_payload
        TIMESTAMP timestamp
    }

    MEMBER ||--o{ ORDERS : "places"
    MEMBER ||--o{ PAYMENT : "initiates"
    MEMBER ||--o{ POINT_HISTORY : "has"
    MEMBER ||--o{ REWARD_POINTS : "accumulates"
    ORDERS ||--o{ ORDER_ITEM : "contains"
    ORDERS ||--o{ PAYMENT : "related_to"
    PRODUCT ||--o{ ORDER_ITEM : "is_part_of"
    PAYMENT ||--o{ PAYMENT_INTERFACE_REQUEST_LOG : "logs"
    PAYMENT ||--o{ POINT_HISTORY : "affects"
```

### 3.2. 엔티티별 상세 정의

#### 3.2.1. `MEMBER` (회원)
- **목적**: 시스템 사용자의 기본 정보를 관리합니다.
- **필드**:
    - `member_id` (BIGINT, PK): 회원 고유 식별자. 자동 생성.
    - `name` (VARCHAR(255)): 회원 이름.
    - `shipping_address` (VARCHAR(255)): 배송 주소.
    - `phone_number` (VARCHAR(20)): 전화번호.
    - `email` (VARCHAR(255)): 이메일 주소.
    - `created_at` (TIMESTAMP): 회원 가입일시.
- **제약조건**: `member_id`는 고유하며 NULL을 허용하지 않습니다. `name`, `phone_number`, `email`은 필수 값입니다.
- **성능 고려사항**: `member_id`에 인덱스. `email` 또는 `phone_number`로 조회가 잦을 경우 인덱스 고려.

#### 3.2.2. `PRODUCT` (상품)
- **목적**: 판매되는 상품의 정보를 관리합니다.
- **필드**:
    - `product_id` (BIGINT, PK): 상품 고유 식별자. 자동 생성.
    - `name` (VARCHAR(255)): 상품명.
    - `price` (DOUBLE PRECISION): 상품 가격.
- **제약조건**: `product_id`는 고유하며 NULL을 허용하지 않습니다. `name`, `price`는 필수 값입니다. `price`는 0보다 커야 합니다.
- **성능 고려사항**: `product_id`에 인덱스.

#### 3.2.3. `ORDERS` (주문)
- **목적**: 회원의 주문 정보를 관리합니다. 주문은 `order_id`, `ord_seq`, `ord_proc_seq`의 복합 키로 관리되어 주문 변경(취소, 부분 취소 등) 이력을 추적할 수 있습니다.
- **필드**:
    - `order_id` (VARCHAR(255), PK): 주문 고유 식별자.
    - `ord_seq` (INTEGER, PK): 주문 내 순번. 동일 `order_id` 내에서 주문 항목의 순서를 나타냅니다.
    - `ord_proc_seq` (INTEGER, PK): 주문 처리 순번. 주문의 변경 이력(예: 최초 주문, 취소, 부분 취소)을 나타냅니다. 1부터 시작하며 변경될 때마다 증가합니다.
    - `claim_id` (VARCHAR(255)): 클레임 ID (취소/환불 등 클레임 발생 시 연결).
    - `member_id` (BIGINT, FK): 주문을 한 회원의 ID.
    - `order_date` (TIMESTAMP): 주문 일시.
    - `total_amount` (DOUBLE PRECISION): 총 주문 금액.
    - `status` (VARCHAR(50)): 주문 상태 (예: `ORDERED`, `CANCELLED`). `OrderStatus` Enum 참조.
- **제약조건**: 복합 PK (`order_id`, `ord_seq`, `ord_proc_seq`)는 고유하며 NULL을 허용하지 않습니다. `member_id`, `order_date`, `total_amount`, `status`는 필수 값입니다. `total_amount`는 0보다 크거나 같아야 합니다.
- **성능 고려사항**: `order_id`, `member_id`에 인덱스.

#### 3.2.4. `ORDER_ITEM` (주문 항목)
- **목적**: 특정 주문에 포함된 개별 상품 정보를 관리합니다.
- **필드**:
    - `order_item_id` (BIGINT, PK): 주문 항목 고유 식별자. 자동 생성.
    - `order_id` (VARCHAR(255), FK): 연관된 주문의 ID.
    - `ord_seq` (INTEGER, FK): 연관된 주문의 순번.
    - `ord_proc_seq` (INTEGER, FK): 연관된 주문의 처리 순번.
    - `product_id` (BIGINT, FK): 주문된 상품의 ID.
    - `quantity` (INTEGER): 주문된 상품의 수량.
    - `price_at_order` (DOUBLE PRECISION): 주문 당시의 상품 단가.
- **제약조건**: `order_item_id`는 고유하며 NULL을 허용하지 않습니다. `order_id`, `ord_seq`, `ord_proc_seq`, `product_id`, `quantity`, `price_at_order`는 필수 값입니다. `quantity`는 1 이상이어야 합니다.
- **성능 고려사항**: `order_item_id`, `order_id`에 인덱스.

#### 3.2.5. `PAYMENT` (결제)
- **목적**: 주문에 대한 결제 정보를 관리합니다.
- **필드**:
    - `payment_id` (VARCHAR(255), PK): 결제 고유 식별자. PG사에서 발급하는 거래 ID와 연동될 수 있습니다.
    - `member_id` (BIGINT, FK): 결제를 수행한 회원의 ID.
    - `order_id` (VARCHAR(255), FK): 연관된 주문의 ID.
    - `claim_id` (VARCHAR(255)): 클레임 ID (환불 등 클레임 발생 시 연결).
    - `amount` (BIGINT): 결제 금액.
    - `payment_method` (VARCHAR(50)): 결제 수단 (예: `CREDIT_CARD`, `POINT`). `PaymentMethod` Enum 참조.
    - `pay_type` (VARCHAR(50)): 결제 유형 (예: `PAYMENT`, `REFUND`). `PayType` Enum 참조.
    - `pg_company` (VARCHAR(50)): 결제를 처리한 PG사 (예: `INICIS`, `NICEPAY`, `TOSS`). `PgCompany` Enum 참조.
    - `status` (VARCHAR(50)): 결제 상태 (예: `SUCCESS`, `FAILED`, `CANCELLED`, `PENDING`). `PaymentStatus` Enum 참조.
    - `order_status` (VARCHAR(50)): 결제 시점의 주문 상태 (예: `ORDERED`, `CANCELED`). `OrderStatus` Enum 참조.
    - `transaction_id` (VARCHAR(255)): PG사에서 발급하는 거래 고유 ID.
    - `payment_date` (TIMESTAMP): 결제 일시.
- **제약조건**: `payment_id`는 고유하며 NULL을 허용하지 않습니다. `member_id`, `order_id`, `amount`, `payment_method`, `pay_type`, `pg_company`, `status`, `payment_date`는 필수 값입니다. `amount`는 0보다 커야 합니다.
- **성능 고려사항**: `payment_id`, `order_id`, `member_id`에 인덱스.

#### 3.2.6. `POINT_HISTORY` (포인트 내역)
- **목적**: 회원의 포인트 변동 내역을 상세히 기록합니다.
- **필드**:
    - `point_history_id` (BIGINT, PK): 포인트 내역 고유 식별자. 자동 생성.
    - `member_id` (BIGINT, FK): 포인트 변동이 발생한 회원의 ID.
    - `point_amount` (BIGINT): 변동된 포인트 양 (+는 적립, -는 사용/차감).
    - `balance_after` (BIGINT): 변동 후 회원의 총 포인트 잔액.
    - `transaction_type` (VARCHAR(50)): 거래 유형 (예: `CHARGE`, `USE`, `REFUND`). `TransactionType` Enum 참조.
    - `reference_type` (VARCHAR(50)): 참조 유형 (예: `PAYMENT`, `CANCEL`, `MANUAL`).
    - `reference_id` (VARCHAR(255)): 연관된 엔티티의 ID (예: `payment_id`, `order_id`).
    - `description` (VARCHAR(255)): 포인트 변동에 대한 상세 설명.
    - `created_at` (TIMESTAMP): 내역 생성 일시.
- **제약조건**: `point_history_id`는 고유하며 NULL을 허용하지 않습니다. `member_id`, `point_amount`, `balance_after`, `transaction_type`, `created_at`는 필수 값입니다.
- **성능 고려사항**: `point_history_id`, `member_id`에 인덱스. `member_id`와 `created_at` 조합으로 조회 시 복합 인덱스 고려.

#### 3.2.7. `REWARD_POINTS` (리워드 포인트)
- **목적**: 회원의 현재 총 리워드 포인트를 관리합니다.
- **필드**:
    - `reward_points_id` (BIGINT, PK): 리워드 포인트 고유 식별자. 자동 생성.
    - `member_id` (BIGINT, FK): 리워드 포인트를 소유한 회원의 ID.
    - `points` (BIGINT): 현재 보유 중인 총 포인트.
    - `last_updated` (TIMESTAMP): 마지막 포인트 변동 일시.
- **제약조건**: `reward_points_id`는 고유하며 NULL을 허용하지 않습니다. `member_id`는 고유하며 NULL을 허용하지 않습니다 (회원당 하나의 리워드 포인트 레코드). `points`, `last_updated`는 필수 값입니다. `points`는 0 이상이어야 합니다.
- **성능 고려사항**: `reward_points_id`, `member_id`에 인덱스.

#### 3.2.8. `PAYMENT_INTERFACE_REQUEST_LOG` (결제 인터페이스 요청 로그)
- **목적**: PG사와의 통신 내역(요청/응답 페이로드)을 기록하여 문제 발생 시 추적 및 분석에 활용합니다.
- **필드**:
    - `log_id` (BIGINT, PK): 로그 고유 식별자. 자동 생성.
    - `payment_id` (VARCHAR(255), FK): 연관된 결제의 ID.
    - `request_type` (VARCHAR(50)): 요청 유형 (예: `INIT`, `CONFIRM`, `CANCEL`, `REFUND`).
    - `request_payload` (TEXT): PG사로 전송된 요청 데이터.
    - `response_payload` (TEXT): PG사로부터 수신된 응답 데이터.
    - `timestamp` (TIMESTAMP): 로그 기록 일시.
- **제약조건**: `log_id`는 고유하며 NULL을 허용하지 않습니다. `payment_id`, `request_type`, `timestamp`는 필수 값입니다.
- **성능 고려사항**: `log_id`, `payment_id`에 인덱스. `timestamp`로 조회 시 인덱스 고려.

## 3.3. 데이터 생명주기 관리 정책
- **로그 데이터**: `PAYMENT_INTERFACE_REQUEST_LOG`와 같은 로그성 데이터는 일정 기간(예: 1년) 보관 후 아카이빙 또는 삭제 정책을 수립합니다.
- **주문/결제 데이터**: `ORDERS`, `ORDER_ITEM`, `PAYMENT`, `POINT_HISTORY` 등 핵심 거래 데이터는 법적 요구사항 및 비즈니스 정책에 따라 영구 보관 또는 장기 아카이빙을 고려합니다.
- **회원 데이터**: `MEMBER`, `REWARD_POINTS` 등 회원 관련 데이터는 회원 탈퇴 시 개인정보 보호 정책에 따라 즉시 삭제 또는 마스킹 처리합니다.
