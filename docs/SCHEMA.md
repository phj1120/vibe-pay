# VibePay 데이터베이스 스키마

## 데이터베이스 정보

- **DBMS**: PostgreSQL
- **인코딩**: UTF-8
- **스키마 파일**: `vibe-pay-backend/src/main/resources/schema.sql`

## 테이블 구조

### 1. member (회원)
```sql
CREATE TABLE member (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255),
    phone_number VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**컬럼 설명**:
- `id`: 회원 고유 ID (자동 증가)
- `name`: 회원 이름 (필수)
- `shipping_address`: 배송지 주소
- `phone_number`: 전화번호
- `created_at`: 회원 가입일시

### 2. product (상품)
```sql
CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL
);
```

**컬럼 설명**:
- `id`: 상품 고유 ID (자동 증가)
- `name`: 상품명 (필수)
- `price`: 상품 가격

### 3. reward_points (적립금)
```sql
CREATE TABLE reward_points (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    points DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_updated TIMESTAMP NOT NULL,
    CONSTRAINT fk_member_reward_points FOREIGN KEY (member_id) REFERENCES member(id)
);
```

**컬럼 설명**:
- `id`: 적립금 기록 ID (자동 증가)
- `member_id`: 회원 ID (외래키)
- `points`: 적립금 잔액
- `last_updated`: 마지막 업데이트 일시

### 4. "order" (주문)
```sql
CREATE TABLE "order" (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    used_reward_points DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    final_payment_amount DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_id BIGINT,
    CONSTRAINT fk_member_order FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_payment_order FOREIGN KEY (payment_id) REFERENCES payment(id)
);
```

**컬럼 설명**:
- `id`: 주문 고유 ID (자동 증가)
- `member_id`: 주문 회원 ID (외래키)
- `order_date`: 주문 일시
- `total_amount`: 상품 총 금액
- `used_reward_points`: 사용한 적립금
- `final_payment_amount`: 최종 결제 금액
- `status`: 주문 상태 (PENDING, PAID, CANCELLED)
- `payment_id`: 결제 ID (외래키)

### 5. order_item (주문 상품)
```sql
CREATE TABLE order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_order DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES "order"(id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id)
);
```

**컬럼 설명**:
- `id`: 주문 상품 ID (자동 증가)
- `order_id`: 주문 ID (외래키)
- `product_id`: 상품 ID (외래키)
- `quantity`: 주문 수량
- `price_at_order`: 주문 당시 상품 가격

### 6. payment (결제)
```sql
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    pg_company VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_member_payment FOREIGN KEY (member_id) REFERENCES member(id)
);
```

**컬럼 설명**:
- `id`: 결제 고유 ID (자동 증가)
- `member_id`: 결제 회원 ID (외래키)
- `amount`: 결제 금액
- `payment_method`: 결제 수단 (CREDIT_CARD, REWARD_POINTS)
- `pg_company`: PG사 (NICEPAY, INICIS)
- `status`: 결제 상태 (PENDING, SUCCESS, FAILED, CANCELLED)
- `transaction_id`: PG사 거래 ID
- `payment_date`: 결제 일시

### 7. payment_interface_request_log (결제 인터페이스 요청 로그)
```sql
CREATE TABLE payment_interface_request_log (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT,
    request_type VARCHAR(50) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_payment_log FOREIGN KEY (payment_id) REFERENCES payment(id)
);
```

**컬럼 설명**:
- `id`: 로그 ID (자동 증가)
- `payment_id`: 결제 ID (외래키)
- `request_type`: 요청 타입 (INITIATE, CONFIRM, CANCEL)
- `request_payload`: 요청 데이터 (JSON)
- `response_payload`: 응답 데이터 (JSON)
- `timestamp`: 요청 일시

## 인덱스

### 성능 최적화를 위한 인덱스
```sql
-- 회원 조회 최적화
CREATE INDEX idx_member_name ON member(name);
CREATE INDEX idx_member_phone ON member(phone_number);

-- 주문 조회 최적화
CREATE INDEX idx_order_member_id ON "order"(member_id);
CREATE INDEX idx_order_date ON "order"(order_date);
CREATE INDEX idx_order_status ON "order"(status);

-- 결제 조회 최적화
CREATE INDEX idx_payment_member_id ON payment(member_id);
CREATE INDEX idx_payment_status ON payment(status);
CREATE INDEX idx_payment_date ON payment(payment_date);

-- 적립금 조회 최적화
CREATE INDEX idx_reward_points_member_id ON reward_points(member_id);
```

## 제약조건

### 데이터 무결성 제약조건
- 모든 외래키 관계 설정
- NOT NULL 제약조건 적용
- 기본값 설정 (적립금 0.0, 생성일시 등)

### 비즈니스 로직 제약조건
- 적립금은 0 이상
- 상품 가격은 0 이상
- 주문 수량은 1 이상
- 결제 금액은 0 이상

## 데이터 타입 규칙

### 금액 관련
- `DOUBLE PRECISION` 사용 (소수점 정밀도 보장)
- 통화 단위: 원화 (KRW)

### 날짜/시간
- `TIMESTAMP` 사용 (밀리초 단위)
- UTC 기준 저장, 표시 시 로컬 시간 변환

### 문자열
- `VARCHAR(255)`: 일반 텍스트
- `TEXT`: 긴 텍스트 (JSON 등)

## 마이그레이션

### 스키마 변경 시 주의사항
1. 기존 데이터 백업
2. 단계적 마이그레이션 (ALTER TABLE)
3. 데이터 검증
4. 롤백 계획 수립

### 예시 마이그레이션 스크립트
```sql
-- 컬럼 추가 예시
ALTER TABLE member ADD COLUMN email VARCHAR(255);

-- 인덱스 추가 예시
CREATE INDEX idx_member_email ON member(email);

-- 데이터 업데이트 예시
UPDATE member SET email = 'default@example.com' WHERE email IS NULL;
```
