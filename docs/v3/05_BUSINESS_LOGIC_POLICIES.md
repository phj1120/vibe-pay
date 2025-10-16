# 5. 비즈니스 로직 정책

## 5.1. 계산 공식 및 알고리즘

### 5.1.1. 주문 총액 계산
- **정책**: 주문의 `totalAmount`는 모든 `OrderItem`의 `quantity`와 `priceAtOrder`를 곱한 값의 총합으로 계산됩니다.
- **알고리즘**:
    ```
    totalAmount = SUM(OrderItem.quantity * OrderItem.priceAtOrder) for all items in an order
    ```

### 5.1.2. 포인트 잔액 계산
- **정책**: 회원의 현재 포인트 잔액은 `REWARD_POINTS` 테이블에 저장된 `points` 값입니다. `POINT_HISTORY`는 포인트 변동 내역을 기록하며, `balanceAfter` 필드는 각 거래 후의 잔액을 나타냅니다.
- **알고리즘**:
    - `REWARD_POINTS.points` = `REWARD_POINTS.points` + `pointAmount` (적립 시)
    - `REWARD_POINTS.points` = `REWARD_POINTS.points` - `pointAmount` (사용 시)
    - `POINT_HISTORY.balanceAfter` = `REWARD_POINTS.points` (거래 후)

### 5.1.3. 주문 번호 및 클레임 번호 채번
- **정책**: 주문 번호와 클레임 번호는 날짜 기반의 고유한 문자열과 시스템 전역 시퀀스를 조합하여 생성됩니다.
- **알고리즘**:
    - **주문 번호**: `YYYYMMDDO` + `8자리_시퀀스` (예: `20251015O00000001`)
    - **클레임 번호**: `YYYYMMDDC` + `8자리_시퀀스` (예: `20251015C00000001`)
    - 시퀀스는 데이터베이스 시퀀스(`NEXTVAL`)를 통해 관리되며, 중복 없이 증가합니다.

## 5.2. 상태 전이 규칙 (State Machine)

### 5.2.1. 주문 상태 (`ORDERS.status`)
- **정의**: `OrderStatus` Enum (`ORDERED`, `CANCELLED`)
- **전이 규칙**:
    - `(초기)` -> `ORDERED`: 주문 생성 및 결제 승인 성공 시.
    - `ORDERED` -> `CANCELLED`: 주문 취소 요청 및 환불 처리 성공 시.
    - `CANCELLED` 상태의 주문은 더 이상 다른 상태로 전이될 수 없습니다.

### 5.2.2. 결제 상태 (`PAYMENT.status`)
- **정의**: `PaymentStatus` Enum (`SUCCESS`, `FAILED`, `CANCELLED`, `PENDING`)
- **전이 규칙**:
    - `(초기)` -> `PENDING`: 결제 시작 요청 시 (PG사 초기화 단계).
    - `PENDING` -> `SUCCESS`: PG사 결제 승인 및 내부 결제 데이터 기록 성공 시.
    - `PENDING` -> `FAILED`: PG사 결제 승인 실패 또는 내부 결제 처리 오류 시.
    - `SUCCESS` -> `CANCELLED`: 결제 취소(환불) 요청 및 PG사 환불 처리 성공 시.
    - `FAILED` 상태의 결제는 더 이상 다른 상태로 전이될 수 없습니다.

### 5.2.3. 포인트 거래 유형 (`POINT_HISTORY.transactionType`)
- **정의**: `TransactionType` Enum (`CHARGE`, `USE`, `REFUND`)
- **전이 규칙**: `REWARD_POINTS`의 `points` 값 변동에 따라 `POINT_HISTORY`에 기록됩니다.
    - `CHARGE`: 관리자에 의한 수동 적립 또는 특정 이벤트로 인한 적립.
    - `USE`: 포인트 결제 시 포인트 차감.
    - `REFUND`: 주문 취소 시 포인트 복원.

## 5.3. 권한 및 접근 제어 정책
- **회원 API**: `memberId`를 기반으로 본인의 정보만 조회/수정/삭제 가능해야 합니다. (현재는 별도의 인증/인가 로직이 구현되어 있지 않으며, `memberId`를 URL Path 또는 Request Body로 직접 전달받아 처리합니다. 실제 서비스에서는 JWT 또는 세션 기반의 인증/인가 시스템이 필요합니다.)
- **상품 API**: 상품 조회는 모든 사용자에게 허용되지만, 상품 생성/수정/삭제는 관리자 권한이 필요합니다. (현재는 별도의 권한 검증 로직 없음)
- **주문 API**: 본인의 주문 내역만 조회 가능해야 합니다. 주문 생성/취소는 인증된 회원만 가능합니다. (현재는 `memberId`를 직접 전달받아 처리)
- **결제 API**: 결제 시작 및 결과 처리는 모든 사용자에게 허용되지만, 결제 내역 조회/수정/삭제는 관리자 권한이 필요합니다. (현재는 별도의 권한 검증 로직 없음)
- **포인트 API**: 본인의 포인트 내역 및 통계만 조회 가능해야 합니다. 포인트 추가는 관리자 권한이 필요합니다. (현재는 `memberId`를 직접 전달받아 처리)
- **결제 인터페이스 로그 API**: 모든 로그 조회/생성은 관리자 권한이 필요합니다. (현재는 별도의 권한 검증 로직 없음)

## 5.4. 트랜잭션 경계 및 일관성 규칙
- **트랜잭션 단위**: Spring의 `@Transactional` 어노테이션을 사용하여 서비스 계층에서 비즈니스 로직의 원자성(Atomicity)을 보장합니다.
    - **회원 생성**: `Member` 생성과 `RewardPoints` 초기화는 하나의 트랜잭션으로 묶여야 합니다.
    - **주문 생성 및 결제 승인**: PG사 결제 승인, `ORDERS`, `ORDER_ITEM`, `PAYMENT` 레코드 생성, `REWARD_POINTS` 및 `POINT_HISTORY` 업데이트는 모두 하나의 트랜잭션으로 처리됩니다. 이 과정에서 오류 발생 시 전체 롤백됩니다.
    - **주문 취소**: 취소 주문 레코드 생성, `PAYMENT` 환불 처리, `REWARD_POINTS` 및 `POINT_HISTORY` 업데이트는 하나의 트랜잭션으로 처리됩니다.
    - **포인트 추가/사용**: `REWARD_POINTS` 업데이트와 `POINT_HISTORY` 기록은 하나의 트랜잭션으로 묶여야 합니다.
- **데이터 일관성**: 모든 트랜잭션은 ACID(원자성, 일관성, 고립성, 지속성) 원칙을 준수하여 데이터의 정합성을 유지합니다.

## 5.5. 동시성 처리 방식
- **낙관적 락 (Optimistic Locking)**: `REWARD_POINTS`와 같이 자주 업데이트되는 데이터에 대해서는 버전(version) 필드를 이용한 낙관적 락을 고려할 수 있습니다. 현재는 명시적인 낙관적 락 구현은 없으나, `UPDATE` 쿼리 시 `WHERE` 절에 `memberId`와 같은 고유 키를 사용하여 특정 레코드만 업데이트되도록 합니다.
- **트랜잭션 격리 수준**: PostgreSQL의 기본 트랜잭션 격리 수준(`Read Committed`)을 사용합니다. 이는 대부분의 비즈니스 요구사항을 충족하지만, 특정 동시성 문제가 발생할 경우 `Repeatable Read` 또는 `Serializable`과 같은 더 높은 격리 수준을 고려할 수 있습니다.
- **시퀀스 사용**: 주문 번호, 클레임 번호, 포인트 내역 ID 등 고유성이 보장되어야 하는 ID는 데이터베이스 시퀀스를 사용하여 동시성 문제를 회피합니다.
