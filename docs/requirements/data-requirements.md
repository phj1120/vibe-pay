# Data Requirements

## Overview
This document defines the initial data that must be loaded into the database for VibePay to function properly. It includes test members, products, initial reward points, and scenario-specific data for testing.

## Initial Data (Essential for System Operation)

### 1. Test Members (3)

#### Member 1: 일반 회원 (포인트 보유)
```sql
INSERT INTO member (member_id, name, email, phone_number, shipping_address, created_at)
VALUES (nextval('member_id_seq'), '현준', 'test@test.com', '010-1234-5678', '서울시 강남구 테헤란로 123', now());

INSERT INTO reward_points (reward_points_id, member_id, points, last_updated)
VALUES (nextval('reward_points_id_seq'), currval('member_id_seq'), 50000, now());
```

**Purpose:** Standard user with sufficient points for testing point payments

#### Member 2: 포인트 없는 회원
```sql
INSERT INTO member (member_id, name, email, phone_number, shipping_address, created_at)
VALUES (nextval('member_id_seq'), '민지', 'test2@test.com', '010-2345-6789', '서울시 서초구 강남대로 456', now());

INSERT INTO reward_points (reward_points_id, member_id, points, last_updated)
VALUES (nextval('reward_points_id_seq'), currval('member_id_seq'), 0, now());
```

**Purpose:** Testing card-only payment scenarios

#### Member 3: VIP 회원 (고포인트)
```sql
INSERT INTO member (member_id, name, email, phone_number, shipping_address, created_at)
VALUES (nextval('member_id_seq'), '지훈', 'vip@test.com', '010-3456-7890', '서울시 송파구 올림픽로 789', now());

INSERT INTO reward_points (reward_points_id, member_id, points, last_updated)
VALUES (nextval('reward_points_id_seq'), currval('member_id_seq'), 500000, now());
```

**Purpose:** Testing full point payment and large transactions

### 2. Products (10)

#### Low Price Range (1,000원 ~ 10,000원)
```sql
INSERT INTO product (product_id, name, price) VALUES (nextval('product_id_seq'), '텀블러', 5000);
INSERT INTO product (product_id, name, price) VALUES (nextval('product_id_seq'), '마우스 패드', 3000);
INSERT INTO product (product_id, name, price) VALUES (nextval('product_id_seq'), '볼펜 세트', 2000);
```

**Purpose:** Small transaction testing

#### Medium Price Range (10,000원 ~ 50,000원)
```sql
INSERT INTO product (product_id, name, price) VALUES (nextval('product_id_seq'), '무선 마우스', 25000);
INSERT INTO product (product_id, name, price) VALUES (nextval('product_id_seq'), '키보드', 35000);
INSERT INTO product (product_id, name, price) VALUES (nextval('product_id_seq'), '책상 스탠드', 15000);
INSERT INTO product (product_id, name, price) VALUES (nextval('product_id_seq'), 'USB 허브', 12000);
```

**Purpose:** Standard transaction testing

#### High Price Range (50,000원 ~ 100,000원+)
```sql
INSERT INTO product (product_id, name, price) VALUES (nextval('product_id_seq'), '모니터', 180000);
INSERT INTO product (product_id, name, price) VALUES (nextval('product_id_seq'), '웹캠', 89000);
INSERT INTO product (product_id, name, price) VALUES (nextval('product_id_seq'), '헤드셋', 65000);
```

**Purpose:** Large transaction and multi-item testing

### 3. Initial Point History (Optional)

Track initial point allocation:

```sql
-- 현준의 초기 포인트
INSERT INTO point_history (point_history_id, member_id, point_amount, balance_after, transaction_type, reference_type, reference_id, description, created_at)
VALUES (nextval('point_history_id_seq'), 1, 50000, 50000, 'EARN', 'MANUAL_CHARGE', '1', '초기 포인트 지급', now());

-- 지훈의 초기 포인트
INSERT INTO point_history (point_history_id, member_id, point_amount, balance_after, transaction_type, reference_type, reference_id, description, created_at)
VALUES (nextval('point_history_id_seq'), 3, 500000, 500000, 'EARN', 'MANUAL_CHARGE', '3', '초기 포인트 지급', now());
```

## Domain Relationship Data

### Member ↔ RewardPoints (1:1)
- **Rule:** Every member MUST have exactly one RewardPoints record
- **Creation:** Automatically created when member is created (MemberService)
- **Deletion:** Cascade delete when member is deleted

### Order ↔ OrderItem (1:N)
- **Rule:** Every order MUST have at least one order item
- **Constraint:** ord_seq differentiates items within same order
- **Example:**
  ```
  order_id: 20250115O00000001
  ├── ord_seq: 1 (product_id: 1, quantity: 2)
  ├── ord_seq: 2 (product_id: 2, quantity: 1)
  └── ord_seq: 3 (product_id: 3, quantity: 3)
  ```

### Payment ↔ Order (N:1)
- **Rule:** Order can have multiple payments (card + point)
- **Constraint:** Same order_id, different payment_method
- **Example:**
  ```
  order_id: 20250115O00000001
  ├── payment_id: 20250115P00000001 (POINT, 5000)
  └── payment_id: 20250115P00000002 (CREDIT_CARD, 25000)
  ```

### Order → Product (N:N via OrderItem)
- **Rule:** Orders reference products via order_item table
- **price_at_order:** Stores product price at time of order (prevents historical data corruption)

## Test Scenario Data

### Scenario 1: INICIS 카드 결제 성공

**Prerequisites:**
- Member: 현준 (ID: 1)
- Product: 무선 마우스 (25,000원)
- Payment Method: INICIS credit card
- Expected Result: Order created, payment approved

**No additional data needed** - use initial data

### Scenario 2: NICEPAY 카드 결제 성공

**Prerequisites:**
- Member: 민지 (ID: 2)
- Product: 키보드 (35,000원)
- Payment Method: NICEPAY credit card
- Expected Result: Order created, payment approved

**No additional data needed** - use initial data

### Scenario 3: 포인트 결제 성공

**Prerequisites:**
- Member: 현준 (ID: 1, points: 50,000)
- Product: 텀블러 (5,000원)
- Payment Method: POINT
- Expected Result:
  - Order created
  - Points: 50,000 → 45,000
  - PointHistory: USE record created

**No additional data needed** - use initial data

### Scenario 4: 혼합 결제 (포인트 + 카드)

**Prerequisites:**
- Member: 현준 (ID: 1, points: 50,000)
- Products:
  - 모니터 (180,000원) × 1
  - 마우스패드 (3,000원) × 1
- Total: 183,000원
- Payment:
  - Points: 50,000
  - Card (INICIS): 133,000

**Expected Result:**
- 2 Payment records created
- Points: 50,000 → 0
- Order with 2 items created

**No additional data needed** - use initial data

### Scenario 5: 가중치 기반 PG 선택

**Configuration (application.yml):**
```yaml
payment:
  weight:
    inicis: 50
    nicepay: 50
```

**Test Method:**
- Create 10 orders with pgCompany=WEIGHTED
- Verify ~50% INICIS, ~50% NICEPAY

**No additional data needed** - use initial data

### Scenario 6: 망취소 (주문 생성 실패)

**Prerequisites:**
- Member: 현준 (ID: 1)
- Product: 무선 마우스 (25,000원)
- Payment: INICIS card
- Trigger: Set `netCancel: true` in order request

**Expected Result:**
- Payment approved
- Order creation fails intentionally
- Payment automatically cancelled (net cancel)
- Points refunded (if used)

**Flow:**
1. Payment initiated and approved ✓
2. Order creation attempted ✗ (forced failure)
3. Net cancel triggered automatically ✓
4. Payment status → CANCELLED ✓
5. PaymentInterfaceRequestLog shows net cancel request ✓

**No additional data needed** - use initial data + test flag

### Scenario 7: 주문 취소

**Prerequisites:**
- Existing completed order (from Scenario 1 or 2)
- Order ID: e.g., 20250115O00000001

**Expected Result:**
- Cancel orders created (ord_proc_seq=2, negative amounts)
- Payments refunded
- Points refunded (if used)
- claim_id generated

**Test Steps:**
1. Complete an order first (Scenario 1)
2. Call DELETE /api/orders/{orderId}
3. Verify cancellation

**No additional data needed** - creates data during test

### Scenario 8: 포인트 부족

**Prerequisites:**
- Member: 민지 (ID: 2, points: 0)
- Product: 무선 마우스 (25,000원)
- Payment Method: POINT only

**Expected Result:**
- Payment initiation succeeds
- Payment confirmation fails with InsufficientPointsException
- No order created

**No additional data needed** - use initial data

### Scenario 9: 다중 상품 주문

**Prerequisites:**
- Member: 지훈 (ID: 3)
- Products:
  - 텀블러 (5,000) × 2 = 10,000
  - 무선 마우스 (25,000) × 1 = 25,000
  - 볼펜 세트 (2,000) × 5 = 10,000
- Total: 45,000원
- Payment: NICEPAY card

**Expected Result:**
- 3 Order records created (same order_id, ord_seq 1,2,3)
- 3 OrderItem records created
- 1 Payment record
- Order detail shows all 3 products

**No additional data needed** - use initial data

### Scenario 10: 회원/상품 CRUD

**Test Coverage:**
- Create member → Verify reward_points created
- Update member info
- Delete member without orders
- Try delete member with orders → Fail
- Create product
- Update product price
- Delete product without orders
- Try delete product in orders → Fail

**No additional data needed** - creates/deletes data during test

## Data Validation Rules

### Member
- ✓ name: required
- ✓ email: optional, unique
- ✓ phone_number: optional
- ✓ created_at: auto-generated

### Product
- ✓ name: required
- ✓ price: required, > 0
- ✓ Cannot delete if referenced in orders

### RewardPoints
- ✓ One per member
- ✓ points: default 0, cannot be negative
- ✓ last_updated: auto-updated on change

### Order
- ✓ order_id: YYYYMMDDOXXXXXXXX format
- ✓ ord_seq: ≥ 1
- ✓ ord_proc_seq: 1 (original), 2 (cancel)
- ✓ total_amount: sum of order items
- ✓ status: ORDERED or CANCELLED

### Payment
- ✓ payment_id: YYYYMMDDPXXXXXXXX format
- ✓ amount: > 0
- ✓ paymentMethod: CARD or POINT
- ✓ pgCompany: null for POINT payments
- ✓ status: READY, APPROVED, CANCELLED, COMPLETED

### PointHistory
- ✓ All point changes logged
- ✓ pointAmount: + for EARN/REFUND, - for USE
- ✓ balanceAfter: calculated balance
- ✓ Immutable (no updates/deletes)

## SQL Script for Initial Data

```sql
-- schema.sql 실행 후 다음 스크립트 실행

-- 회원 3명
INSERT INTO member VALUES (nextval('member_id_seq'), '현준', '서울시 강남구 테헤란로 123', '010-1234-5678', 'test@test.com', now());
INSERT INTO reward_points VALUES (nextval('reward_points_id_seq'), currval('member_id_seq'), 50000, now());

INSERT INTO member VALUES (nextval('member_id_seq'), '민지', '서울시 서초구 강남대로 456', '010-2345-6789', 'test2@test.com', now());
INSERT INTO reward_points VALUES (nextval('reward_points_id_seq'), currval('member_id_seq'), 0, now());

INSERT INTO member VALUES (nextval('member_id_seq'), '지훈', '서울시 송파구 올림픽로 789', '010-3456-7890', 'vip@test.com', now());
INSERT INTO reward_points VALUES (nextval('reward_points_id_seq'), currval('member_id_seq'), 500000, now());

-- 상품 10개
INSERT INTO product VALUES (nextval('product_id_seq'), '텀블러', 5000);
INSERT INTO product VALUES (nextval('product_id_seq'), '마우스 패드', 3000);
INSERT INTO product VALUES (nextval('product_id_seq'), '볼펜 세트', 2000);
INSERT INTO product VALUES (nextval('product_id_seq'), '무선 마우스', 25000);
INSERT INTO product VALUES (nextval('product_id_seq'), '키보드', 35000);
INSERT INTO product VALUES (nextval('product_id_seq'), '책상 스탠드', 15000);
INSERT INTO product VALUES (nextval('product_id_seq'), 'USB 허브', 12000);
INSERT INTO product VALUES (nextval('product_id_seq'), '모니터', 180000);
INSERT INTO product VALUES (nextval('product_id_seq'), '웹캠', 89000);
INSERT INTO product VALUES (nextval('product_id_seq'), '헤드셋', 65000);

-- 초기 포인트 이력
INSERT INTO point_history VALUES (nextval('point_history_id_seq'), 1, 50000, 50000, 'EARN', 'MANUAL_CHARGE', '1', '초기 포인트 지급', now());
INSERT INTO point_history VALUES (nextval('point_history_id_seq'), 3, 500000, 500000, 'EARN', 'MANUAL_CHARGE', '3', '초기 포인트 지급', now());
```

## Data Cleanup (for Re-testing)

```sql
-- 모든 테스트 데이터 삭제 (순서 중요 - FK 제약조건)
DELETE FROM payment_interface_request_log;
DELETE FROM point_history WHERE point_history_id > 2; -- 초기 데이터 제외
DELETE FROM order_item;
DELETE FROM payment;
DELETE FROM orders;
DELETE FROM reward_points;
DELETE FROM product;
DELETE FROM member;

-- 시퀀스 초기화
ALTER SEQUENCE member_id_seq RESTART WITH 1;
ALTER SEQUENCE product_id_seq RESTART WITH 1;
ALTER SEQUENCE reward_points_id_seq RESTART WITH 1;
ALTER SEQUENCE point_history_id_seq RESTART WITH 1;
ALTER SEQUENCE order_id_seq RESTART WITH 1;
ALTER SEQUENCE payment_id_seq RESTART WITH 1;
ALTER SEQUENCE claim_id_seq RESTART WITH 1;

-- 초기 데이터 재생성 (위 SQL 스크립트 다시 실행)
```

## Notes

- Initial data covers all basic test scenarios
- No need to manually create orders/payments - test flow creates them
- PG test credentials already in application.yml
- Point balances designed for various test cases
- Product prices vary for different transaction sizes
- All test scenarios can run independently with initial data
- Cleanup script enables fresh testing environment
