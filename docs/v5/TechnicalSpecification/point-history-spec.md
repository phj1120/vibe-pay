# Point History Technical Specification

## 1. 개요
- **목적**: 본 문서는 VibePay 시스템의 포인트 내역 관리 기능에 대한 기술 사양을 정의합니다. 이 기능은 회원의 포인트 변동(적립, 사용, 환불) 내역을 상세하게 기록하고, 기록된 내역을 조회하며, 포인트 사용 통계를 제공합니다.
- **System Design에서의 위치**: VibePay 백엔드 서비스의 핵심 모듈 중 하나로, 리워드 포인트(RewardPoints) 모듈과 연동하여 포인트 변동 시 내역을 기록합니다. 회원(Member) 모듈에서 회원별 포인트 내역을 조회할 때 의존합니다.
- **관련 컴포넌트 및 의존성**:
    - `PointHistoryController`: 포인트 내역 관련 API 엔드포인트 제공
    - `PointHistoryService`: 포인트 내역 비즈니스 로직 처리
    - `PointHistoryMapper`: 포인트 내역 데이터베이스 CRUD
    - `PointHistory`: 포인트 내역 엔티티
    - `PointStatistics`: 포인트 통계 응답 DTO
    - `RewardPointsMapper`: 현재 포인트 잔액 조회를 위해 `RewardPoints` 테이블에 접근

## 2. 프로세스 흐름

### 2.1. 포인트 내역 기록 (recordPointUsage, recordPointRefund, recordPointEarn)
```mermaid
sequenceDiagram
    participant CallerService
    participant PointHistoryService
    participant RewardPointsMapper
    participant PointHistoryMapper
    database Database

    CallerService->>PointHistoryService: recordPointUsage(memberId, usedPoints, referenceId, description)
    PointHistoryService->>RewardPointsMapper: findByMemberId(memberId)
    RewardPointsMapper-->>Database: SELECT RewardPoints
    Database-->>RewardPointsMapper: RewardPoints (current balance)
    RewardPointsMapper-->>PointHistoryService: RewardPoints
    PointHistoryService->>PointHistory: create PointHistory entity
    PointHistoryService->>PointHistoryMapper: insert(pointHistory)
    PointHistoryMapper-->>Database: INSERT PointHistory
    Database-->>PointHistoryMapper: (void)
    PointHistoryMapper-->>PointHistoryService: (void)
    PointHistoryService-->>CallerService: (void)

    Note right of CallerService: recordPointRefund, recordPointEarn도 유사한 흐름
```

**단계별 상세 설명:**
1.  **CallerService -> PointHistoryService**: `RewardPointsService` 또는 `OrderService` 등 다른 서비스에서 포인트 변동이 발생하면, `PointHistoryService`의 `recordPointUsage()`, `recordPointRefund()`, `recordPointEarn()` 메서드 중 하나를 호출합니다.
2.  **PointHistoryService -> RewardPointsMapper**: `PointHistoryService`는 `RewardPointsMapper.findByMemberId()`를 호출하여 해당 `memberId`의 현재 포인트 잔액을 조회합니다. 이 잔액은 `balanceAfter` 필드에 기록됩니다.
3.  **PointHistoryService -> PointHistory**: `PointHistoryService`는 전달받은 파라미터와 조회된 잔액을 바탕으로 `PointHistory` 엔티티를 생성합니다. 이때 `pointAmount`는 변동량(적립은 양수, 사용/환불은 음수), `transactionType`은 `USE`, `REFUND`, `EARN` 중 하나로 설정됩니다.
4.  **PointHistoryService -> PointHistoryMapper**: `PointHistoryService`는 `PointHistoryMapper.insert()`를 호출하여 생성된 `PointHistory` 엔티티를 데이터베이스에 저장합니다.
5.  **PointHistoryService -> CallerService**: 기록 처리 후 `void`를 반환합니다.

### 2.2. 포인트 내역 조회 (getPointHistoryByMember, getPointHistoryByMemberWithPaging 등)
```mermaid
sequenceDiagram
    participant Client
    participant PointHistoryController
    participant PointHistoryService
    participant PointHistoryMapper
    database Database

    Client->>PointHistoryController: GET /api/point-history/member/{memberId}
    PointHistoryController->>PointHistoryService: getPointHistoryByMember(memberId)
    PointHistoryService->>PointHistoryMapper: findByMemberId(memberId)
    PointHistoryMapper-->>Database: SELECT PointHistory
    Database-->>PointHistoryMapper: List<PointHistory>
    PointHistoryMapper-->>PointHistoryService: List<PointHistory>
    PointHistoryService-->>PointHistoryController: List<PointHistory>
    PointHistoryController-->>Client: 200 OK (List<PointHistory>)

    Note right of Client: 페이징, 타입별, 참조별 조회도 유사한 흐름
```

**단계별 상세 설명:**
1.  **Client -> PointHistoryController**: 클라이언트는 회원별, 타입별, 참조별 등 다양한 조건으로 포인트 내역을 조회하기 위해 `/api/point-history/...` 엔드포인트로 GET 요청을 보냅니다.
2.  **PointHistoryController -> PointHistoryService**: `PointHistoryController`는 `PointHistoryService`의 적절한 조회 메서드(예: `getPointHistoryByMember()`, `getPointHistoryByMemberWithPaging()`, `getPointHistoryByMemberAndType()`, `getPointHistoryByReference()`, `getAllPointHistory()`, `getPointHistoryById()`)를 호출합니다.
3.  **PointHistoryService -> PointHistoryMapper**: `PointHistoryService`는 `PointHistoryMapper`의 해당 조회 메서드를 호출하여 데이터베이스에서 포인트 내역을 조회합니다.
4.  **PointHistoryService -> PointHistoryController**: 조회된 `List<PointHistory>` 또는 `PointHistory` 객체를 `PointHistoryController`로 반환합니다.
5.  **PointHistoryController -> Client**: `PointHistoryController`는 조회된 데이터를 클라이언트에게 200 OK 응답으로 반환합니다. 데이터가 없으면 빈 리스트 또는 404 Not Found 응답을 반환합니다.

### 2.3. 포인트 통계 조회 (getPointStatistics)
```mermaid
sequenceDiagram
    participant Client
    participant PointHistoryController
    participant PointHistoryService
    participant PointHistoryMapper
    participant RewardPointsMapper
    database Database

    Client->>PointHistoryController: GET /api/point-history/member/{memberId}/statistics
    PointHistoryController->>PointHistoryService: getPointStatistics(memberId)
    PointHistoryService->>PointHistoryMapper: findByMemberId(memberId)
    PointHistoryMapper-->>Database: SELECT all PointHistory for member
    Database-->>PointHistoryMapper: List<PointHistory>
    PointHistoryMapper-->>PointHistoryService: List<PointHistory>
    PointHistoryService->>RewardPointsMapper: findByMemberId(memberId)
    RewardPointsMapper-->>Database: SELECT RewardPoints (current balance)
    Database-->>RewardPointsMapper: RewardPoints
    RewardPointsMapper-->>PointHistoryService: RewardPoints
    PointHistoryService->>PointStatistics: calculate statistics
    PointHistoryService-->>PointHistoryController: PointStatistics
    PointHistoryController-->>Client: 200 OK (PointStatistics)
```

**단계별 상세 설명:**
1.  **Client -> PointHistoryController**: 클라이언트는 특정 회원의 포인트 통계를 조회하기 위해 `/api/point-history/member/{memberId}/statistics` 엔드포인트로 GET 요청을 보냅니다.
2.  **PointHistoryController -> PointHistoryService**: `PointHistoryController`는 `PointHistoryService.getPointStatistics()` 메서드를 호출합니다.
3.  **PointHistoryService -> PointHistoryMapper**: `PointHistoryService`는 `PointHistoryMapper.findByMemberId()`를 호출하여 해당 `memberId`의 모든 포인트 내역을 조회합니다.
4.  **PointHistoryService -> RewardPointsMapper**: `PointHistoryService`는 `RewardPointsMapper.findByMemberId()`를 호출하여 해당 `memberId`의 현재 포인트 잔액을 조회합니다.
5.  **PointHistoryService (통계 계산)**: 조회된 모든 포인트 내역을 순회하며 `transactionType`에 따라 `totalEarned`, `totalUsed`, `totalRefunded`, `earnCount`, `useCount`, `refundCount`를 계산합니다. `currentBalance`는 `RewardPoints`에서 조회한 값을 사용합니다.
6.  **PointHistoryService -> PointHistoryController**: 계산된 `PointStatistics` 객체를 `PointHistoryController`로 반환합니다.
7.  **PointHistoryController -> Client**: `PointHistoryController`는 `PointStatistics` 객체를 클라이언트에게 200 OK 응답으로 반환합니다.

## 3. 데이터 구조

### 3.1. 데이터베이스
`schema.sql` 또는 `pay.sql` 파일이 없으므로, `PointHistory` 엔티티와 `PointHistoryMapper.xml`을 기반으로 스키마를 유추합니다.

**`PointHistory` 테이블 (유추)**
```sql
CREATE TABLE point_history (
    point_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    point_amount BIGINT NOT NULL,      -- 포인트 변동량 (+ 적립, - 사용)
    balance_after BIGINT NOT NULL,     -- 변동 후 잔액
    transaction_type VARCHAR(50) NOT NULL, -- EARN, USE, REFUND
    reference_type VARCHAR(50),        -- PAYMENT, CANCEL, MANUAL
    reference_id VARCHAR(255),         -- 연관된 ID (payment_id, order_id 등)
    description VARCHAR(500),
    created_at DATETIME NOT NULL
);
```

**각 필드의 타입, 제약조건, 기본값, 인덱스:**
- `point_history_id`: `BIGINT`, `AUTO_INCREMENT`, `PRIMARY KEY`, 포인트 내역 고유 ID.
- `member_id`: `BIGINT`, `NOT NULL`, 회원 ID.
- `point_amount`: `BIGINT`, `NOT NULL`, 포인트 변동량 (양수: 적립/환불, 음수: 사용).
- `balance_after`: `BIGINT`, `NOT NULL`, 해당 내역 기록 후의 최종 포인트 잔액.
- `transaction_type`: `VARCHAR(50)`, `NOT NULL`, 거래 유형 (`EARN`, `USE`, `REFUND`).
- `reference_type`: `VARCHAR(50)`, 연관된 엔티티 타입 (`PAYMENT`, `CANCEL`, `MANUAL`).
- `reference_id`: `VARCHAR(255)`, 연관된 엔티티의 ID (예: `payment_id`, `order_id`).
- `description`: `VARCHAR(500)`, 내역에 대한 상세 설명.
- `created_at`: `DATETIME`, `NOT NULL`, 내역 기록 일시.
- `INDEX`: `member_id`, `created_at`, `reference_type`, `reference_id` 컬럼에 대한 인덱스.

### 3.2. DTO/API 모델

#### `PointHistory` (Entity/Response DTO)
```java
package com.vibe.pay.backend.pointhistory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistory {
    private Long pointHistoryId;
    private Long memberId;
    private Long pointAmount; // 포인트 변동량 (+ 적립, - 사용)
    private Long balanceAfter; // 변동 후 잔액
    private String transactionType; // EARN(적립), USE(사용), REFUND(환불)
    private String referenceType; // PAYMENT(결제), CANCEL(취소), MANUAL(수동)
    private String referenceId; // 연관된 ID (payment_id, order_id 등)
    private String description; // 설명
    private LocalDateTime createdAt;
}
```

#### `PointStatistics` (Response DTO)
```java
package com.vibe.pay.backend.pointhistory;

// ... (getter/setter 생략)
public class PointStatistics {
    private Long memberId;
    private Double currentBalance; // 현재 포인트 잔액
    private Double totalEarned; // 총 적립 포인트
    private Double totalUsed; // 총 사용 포인트
    private Double totalRefunded; // 총 환불 포인트
    private Integer earnCount; // 적립 횟수
    private Integer useCount; // 사용 횟수
    private Integer refundCount; // 환불 횟수
    private Integer totalTransactions; // 총 거래 횟수
}
```
- **검증 규칙**: 없음 (내부 계산 결과).

## 4. API 명세

### 4.1. 회원별 포인트 내역 조회
- **Endpoint**: `/api/point-history/member/{memberId}`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (해당 회원 또는 관리자)
- **Request 예시**: `/api/point-history/member/1`
- **검증 규칙**: `memberId`는 `Long` 타입의 양수여야 함.
- **Success Response (예시)**:
    ```json
    [
        {
            "pointHistoryId": 1,
            "memberId": 1,
            "pointAmount": 1000,
            "balanceAfter": 1000,
            "transactionType": "EARN",
            "referenceType": "MANUAL",
            "referenceId": "1",
            "description": "마일리지 수동 충전",
            "createdAt": "2025-10-16T11:30:00"
        },
        {
            "pointHistoryId": 2,
            "memberId": 1,
            "pointAmount": -300,
            "balanceAfter": 700,
            "transactionType": "USE",
            "referenceType": "PAYMENT",
            "referenceId": "PAYMENT_123",
            "description": "포인트 결제",
            "createdAt": "2025-10-16T11:35:00"
        }
    ]
    ```

### 4.2. 회원별 포인트 통계 조회
- **Endpoint**: `/api/point-history/member/{memberId}/statistics`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (해당 회원 또는 관리자)
- **Request 예시**: `/api/point-history/member/1/statistics`
- **검증 규칙**: `memberId`는 `Long` 타입의 양수여야 함.
- **Success Response (예시)**:
    ```json
    {
        "memberId": 1,
        "currentBalance": 700.0,
        "totalEarned": 1000.0,
        "totalUsed": 300.0,
        "totalRefunded": 0.0,
        "earnCount": 1,
        "useCount": 1,
        "refundCount": 0,
        "totalTransactions": 2
    }
    ```

### 4.3. 회원별 특정 거래 타입 포인트 내역 조회
- **Endpoint**: `/api/point-history/member/{memberId}/type/{transactionType}`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (해당 회원 또는 관리자)
- **Request 예시**: `/api/point-history/member/1/type/USE`
- **검증 규칙**: `memberId`는 `Long` 타입의 양수여야 함. `transactionType`은 `EARN`, `USE`, `REFUND` 중 하나여야 함.
- **Success Response (예시)**: `4.1. 회원별 포인트 내역 조회`와 동일한 형식.

### 4.4. 특정 거래와 관련된 포인트 내역 조회
- **Endpoint**: `/api/point-history/reference/{referenceType}/{referenceId}`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (관리자 권한)
- **Request 예시**: `/api/point-history/reference/PAYMENT/PAYMENT_123`
- **검증 규칙**: `referenceType`은 `PAYMENT`, `CANCEL`, `MANUAL` 중 하나여야 함. `referenceId`는 `String` 타입.
- **Success Response (예시)**: `4.1. 회원별 포인트 내역 조회`와 동일한 형식.

### 4.5. 전체 포인트 내역 조회
- **Endpoint**: `/api/point-history/all`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (관리자 권한)
- **Request 예시**: 없음
- **검증 규칙**: 없음
- **Success Response (예시)**: `4.1. 회원별 포인트 내역 조회`와 동일한 형식.

### 4.6. 특정 포인트 히스토리 조회
- **Endpoint**: `/api/point-history/{pointHistoryId}`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (관리자 권한)
- **Request 예시**: `/api/point-history/1`
- **검증 규칙**: `pointHistoryId`는 `Long` 타입의 양수여야 함.
- **Success Response (예시)**: `4.1. 회원별 포인트 내역 조회`의 단일 객체 형식.
- **Error Response**:
    - **HTTP Status**: `404 Not Found`
    - **메시지**: (응답 본문 없음)
    - **상황**: `pointHistoryId`에 해당하는 내역이 없을 때.

## 5. 비즈니스 로직 상세

### 5.1. 포인트 내역 기록 (`PointHistoryService.recordPointUsage`, `recordPointRefund`, `recordPointEarn`)
- **목적**: 회원의 포인트 변동 내역을 상세하게 기록합니다.
- **입력 파라미터**: `Long memberId`, `Long pointAmount` (변동량), `String referenceId`, `String description`, `String referenceType` (적립 시)
- **계산 로직**:
    1.  `pointAmount`가 0 이하인 경우 (적립 시) 또는 0 이상인 경우 (사용/환불 시) 로그를 남기고 기록을 건너뜁니다.
    2.  `rewardPointsMapper.findByMemberId(memberId)`를 호출하여 해당 `memberId`의 현재 `RewardPoints` 정보를 조회합니다. 회원의 `RewardPoints` 정보가 없으면 `IllegalStateException`을 발생시킵니다.
    3.  `PointHistory` 엔티티를 생성하고 다음 필드를 설정합니다:
        *   `memberId`: 전달받은 `memberId`.
        *   `pointAmount`: `recordPointUsage`는 `-usedPoints`, `recordPointRefund`는 `refundPoints`, `recordPointEarn`은 `earnedPoints`.
        *   `balanceAfter`: 조회된 `currentRewardPoints.getPoints()`.
        *   `transactionType`: `USE`, `REFUND`, `EARN` 중 해당 값.
        *   `referenceType`: `PAYMENT`, `CANCEL`, `MANUAL` 등 연관된 타입.
        *   `referenceId`: 연관된 ID (예: `payment_id`, `order_id`).
        *   `description`: 전달받은 설명.
        *   `createdAt`: `LocalDateTime.now()`.
    4.  `pointHistoryMapper.insert(pointHistory)`를 호출하여 DB에 저장합니다.
- **제약조건**:
    - `memberId`에 해당하는 `RewardPoints` 정보가 존재해야 합니다.
    - `pointAmount`는 0이 아니어야 합니다.
- **에러 케이스**:
    - `RewardPoints` 정보가 없을 때 `IllegalStateException` 발생.
    - DB 저장 실패 시 예외 발생.

### 5.2. 포인트 통계 조회 (`PointHistoryService.getPointStatistics`)
- **목적**: 특정 회원의 총 적립, 사용, 환불 포인트 및 횟수, 현재 잔액 등의 통계 정보를 제공합니다.
- **입력 파라미터**: `Long memberId`
- **계산 로직**:
    1.  `pointHistoryMapper.findByMemberId(memberId)`를 호출하여 해당 `memberId`의 모든 포인트 내역을 조회합니다.
    2.  조회된 `PointHistory` 목록을 순회하며 각 내역의 `transactionType`에 따라 `totalEarned`, `totalUsed`, `totalRefunded`, `earnCount`, `useCount`, `refundCount`를 집계합니다. `USE` 타입의 `pointAmount`는 음수로 저장되므로 `Math.abs()`를 사용하여 양수로 변환하여 `totalUsed`에 더합니다.
    3.  `rewardPointsMapper.findByMemberId(memberId)`를 호출하여 현재 포인트 잔액(`currentBalance`)을 조회합니다.
    4.  집계된 통계 정보와 현재 잔액을 바탕으로 `PointStatistics` 객체를 생성하여 반환합니다.
- **제약조건**:
    - `memberId`에 해당하는 `RewardPoints` 정보가 존재해야 합니다.
- **에러 케이스**:
    - `RewardPoints` 정보가 없을 때 `currentBalance`는 0.0으로 설정됩니다.
    - DB 조회 실패 시 예외 발생.

## 6. 에러 처리

- **에러 코드 체계**: 현재 `PointHistoryService`에서는 `IllegalStateException`을 사용하고 있습니다. `PointHistoryController`에서는 대부분 `ResponseEntity.internalServerError().build()`를 반환하고 있습니다.
    - **개선 방향**: `PointHistoryException`과 같은 커스텀 예외를 정의하고, 구체적인 에러 코드를 포함하여 에러 처리를 표준화해야 합니다.
- **각 에러별 HTTP Status, 처리 방법, 사용자 메시지**:
    - **포인트 내역 기록 실패 (회원 포인트 정보 없음)**:
        - HTTP Status: `500 Internal Server Error` (내부 서비스 호출 시 발생)
        - 처리 방법: 호출 서비스에서 예외 처리, 로그 기록.
        - 사용자 메시지: "포인트 내역 기록 중 오류가 발생했습니다."
    - **포인트 내역 조회 실패 (내역 없음)**:
        - HTTP Status: `404 Not Found` (특정 `pointHistoryId` 조회 시)
        - 처리 방법: 클라이언트에게 오류 메시지 반환 (또는 응답 본문 없음), 로그 기록.
        - 사용자 메시지: "포인트 내역을 찾을 수 없습니다."
    - **일반적인 DB 오류**: `500 Internal Server Error`

## 7. 트랜잭션 및 동시성

- **트랜잭션 경계**:
    - `PointHistoryService`의 `recordPointUsage()`, `recordPointRefund()`, `recordPointEarn()` 메서드에는 `@Transactional` 어노테이션이 적용되어 있습니다. 이는 `PointHistory` 기록 작업이 하나의 트랜잭션으로 처리됨을 의미합니다.
- **동시성 문제 및 해결 방법**:
    - **`balanceAfter` 필드의 정합성**: `PointHistory` 기록 시 `RewardPoints`에서 현재 잔액을 조회하여 `balanceAfter`에 기록합니다. `RewardPoints`의 잔액 업데이트와 `PointHistory` 기록 사이에 Race Condition이 발생하면 `balanceAfter` 값이 부정확해질 수 있습니다.
        - **개선 방향**: `RewardPoints` 업데이트와 `PointHistory` 기록을 하나의 트랜잭션으로 묶고, `RewardPoints` 엔티티에 대한 비관적 락(Pessimistic Lock)을 사용하여 `balanceAfter`의 정합성을 보장해야 합니다.

## 8. 성능 최적화

- **쿼리 최적화**:
    - `PointHistoryMapper.findByMemberIdWithPaging()`: 페이징 처리를 통해 대량의 포인트 내역 조회 시 성능 저하를 방지합니다.
    - `PointHistoryService.getPointStatistics()`: 모든 포인트 내역을 조회한 후 메모리에서 집계하므로, 내역이 많아질 경우 성능 문제가 발생할 수 있습니다.
        - **개선 방향**: 통계 집계 쿼리를 데이터베이스 레벨에서 직접 수행하도록 `PointHistoryMapper`에 통계 전용 메서드를 추가하는 것이 더 효율적입니다.
- **인덱스 전략**:
    - `point_history` 테이블의 `member_id`, `created_at`, `transaction_type`, `reference_type`, `reference_id` 컬럼에 대한 인덱스 추가를 고려하여 조회 성능을 향상시킬 수 있습니다.
- **캐싱 전략**: 현재 코드에는 명시적인 캐싱 전략이 적용되어 있지 않습니다.
    - **개선 방향**: 자주 조회되는 포인트 통계 정보(예: `getPointStatistics()`)에 대해 캐싱(예: Redis, Ehcache)을 적용하여 DB 부하를 줄일 수 있습니다.

## 9. 보안

- **입력 검증**:
    - API 엔드포인트의 Path Variable (`memberId`, `pointHistoryId`, `transactionType`, `referenceType`, `referenceId`)에 대한 유효성 검증이 필요합니다.
    - **개선 방향**: Spring `Validation` API를 사용하여 Path Variable에 대한 `@Min`, `@Pattern` 등의 어노테이션을 적용하고, `PointHistoryController`에서 `@Validated`를 사용하여 자동 검증을 수행해야 합니다.
- **인증/권한 체크**:
    - `PointHistoryController`의 `getAllPointHistory`, `getPointHistoryByReference`, `getPointHistoryById` API는 관리자 권한으로 제한되어야 합니다.
    - `getPointHistoryByMember`, `getPointStatistics`, `getPointHistoryByMemberAndType` API는 해당 회원 또는 관리자만 접근 가능하도록 인증 및 권한 체크 로직이 필요합니다.
    - **개선 방향**: `@PreAuthorize` 또는 인터셉터/필터를 사용하여 적절한 권한을 가진 사용자만 해당 API에 접근할 수 있도록 구현해야 합니다.

## 10. 테스트 케이스

### 10.1. 정상 시나리오 (Happy Path)
- **포인트 내역 기록**: `recordPointUsage`, `recordPointRefund`, `recordPointEarn` 호출 시 `PointHistory` 엔티티가 올바른 `pointAmount`, `balanceAfter`, `transactionType`, `referenceType`, `referenceId`, `description`으로 DB에 저장되는지 확인.
- **포인트 내역 조회**: `memberId`로 포인트 내역 조회 시 올바른 내역 목록이 반환되는지 확인. 페이징, 타입별, 참조별 조회도 정상 동작하는지 확인.
- **포인트 통계 조회**: `memberId`로 포인트 통계 조회 시 `currentBalance`, `totalEarned`, `totalUsed`, `totalRefunded`, `earnCount`, `useCount`, `refundCount`, `totalTransactions`가 올바르게 계산되어 반환되는지 확인.

### 10.2. 예외 시나리오 (각 에러 케이스)
- **포인트 내역 기록 실패**:
    - 존재하지 않는 `memberId`로 기록 시 `IllegalStateException` 발생.
    - `pointAmount`가 0인 경우 기록이 건너뛰어지는지 확인.
- **포인트 내역 조회 실패**: 존재하지 않는 `pointHistoryId`로 조회 시 `404 Not Found` 응답.
- **포인트 통계 조회 실패**: 존재하지 않는 `memberId`로 통계 조회 시 `currentBalance`가 0.0으로 반환되는지 확인.

### 10.3. 경계값 테스트
- `pointAmount`가 매우 크거나 작은 값인 경우.
- `memberId`, `pointHistoryId`가 음수 또는 0인 경우.
- 페이징 파라미터 `page`, `size`가 음수 또는 매우 큰 값인 경우.

### 10.4. 동시성 테스트
- 동일한 회원의 포인트에 대해 `RewardPoints` 업데이트와 `PointHistory` 기록이 동시에 발생할 때 `balanceAfter` 필드의 정합성 유지 여부 확인.

## 11. 알려진 이슈 및 개선 방향

### 11.1. 코드 품질 및 구조
- **`PointStatistics`의 `Double` 타입**: `currentBalance`, `totalEarned`, `totalUsed`, `totalRefunded` 필드가 `Double` 타입으로 되어 있습니다. 금액 계산 시 부동소수점 오차가 발생할 수 있습니다.
    - **개선 방향**: `Long` (최소 단위를 정수로 저장) 또는 `BigDecimal` 타입을 사용하여 정확한 금액 계산을 보장해야 합니다.
- **`PointHistoryService.recordPointEarn`의 `transactionType` 및 `referenceType` 오류**: `recordPointEarn` 메서드 내부에서 `pointHistory.setTransactionType("REFUND")` 및 `pointHistory.setReferenceType("CANCEL")`로 하드코딩되어 있습니다. 이는 적립 내역을 환불/취소로 잘못 기록하게 만듭니다.
    - **개선 방향**: `transactionType`은 `EARN`으로, `referenceType`은 전달받은 값 또는 `MANUAL` 등으로 올바르게 설정해야 합니다.
- **`PointHistoryService.recordPointUsage`의 `balanceAfter` 계산 오류**: `balanceAfter`를 `currentRewardPoints.getPoints()`로 설정하고 있는데, 이는 포인트 사용 전의 잔액입니다. 사용 후의 잔액을 기록해야 합니다.
    - **개선 방향**: `balanceAfter`를 `currentRewardPoints.getPoints() - usedPoints`로 계산하여 설정해야 합니다.
- **`PointHistoryService.recordPointRefund`의 `balanceAfter` 계산 오류**: `balanceAfter`를 `currentRewardPoints.getPoints()`로 설정하고 있는데, 이는 포인트 환불 전의 잔액입니다. 환불 후의 잔액을 기록해야 합니다.
    - **개선 방향**: `balanceAfter`를 `currentRewardPoints.getPoints() + refundPoints`로 계산하여 설정해야 합니다.



### 11.3. 리팩토링 포인트
- **`PointHistoryService`의 `recordPointEarn` 메서드 시그니처**: `referenceType`과 `referenceId`를 파라미터로 받고 있지만, `RewardPointsService.addPoints`에서 호출할 때는 `referenceType`을 "MANUAL_CHARGE"로 하드코딩하고 `referenceId`로 `rewardPointsId`를 사용하고 있습니다. 이는 `PointHistoryService`의 유연성을 저해합니다.
    - **개선 방향**: `recordPointEarn` 메서드에서 `referenceType`과 `referenceId`를 더 명확하게 사용하거나, `RewardPointsService`에서 `PointHistory` 엔티티를 직접 생성하여 `PointHistoryService`에 전달하는 방식으로 변경하여 `PointHistoryService`의 책임을 줄일 수 있습니다.
- **`PointHistoryService`의 `PointStatistics` 계산 로직**: 모든 내역을 조회한 후 메모리에서 집계하는 방식은 비효율적입니다.
    - **개선 방향**: `PointHistoryMapper`에 `getPointStatisticsByMemberId`와 같은 SQL 쿼리를 추가하여 DB에서 직접 집계된 통계 데이터를 가져오도록 변경해야 합니다.


