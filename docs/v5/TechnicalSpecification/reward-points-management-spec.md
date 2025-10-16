# Reward Points Management Technical Specification

## 1. 개요
- **목적**: 본 문서는 VibePay 시스템의 리워드 포인트 관리 기능에 대한 기술 사양을 정의합니다. 이 기능은 회원의 총 리워드 포인트를 관리하고, 포인트의 적립 및 사용을 처리합니다. 또한, 포인트 변동 내역은 `PointHistory` 모듈에 기록됩니다.
- **System Design에서의 위치**: VibePay 백엔드 서비스의 핵심 모듈 중 하나로, 회원(Member) 모듈과 연동하여 회원별 포인트를 관리하고, 포인트 내역(PointHistory) 모듈에 포인트 변동 이력을 기록합니다.
- **관련 컴포넌트 및 의존성**:
    - `RewardPointsController`: 리워드 포인트 관련 API 엔드포인트 제공
    - `RewardPointsService`: 리워드 포인트 비즈니스 로직 처리
    - `RewardPointsMapper`: 리워드 포인트 데이터베이스 CRUD
    - `RewardPoints`: 리워드 포인트 엔티티
    - `RewardPointsRequest`: 포인트 적립/사용 요청 DTO
    - `PointHistoryService`: 포인트 변동 내역 기록

## 2. 프로세스 흐름

### 2.1. 리워드 포인트 생성 (createRewardPoints)
```mermaid
sequenceDiagram
    participant Client
    participant RewardPointsController
    participant RewardPointsService
    participant RewardPointsMapper
    database Database

    Client->>RewardPointsController: POST /api/rewardpoints (RewardPoints)
    RewardPointsController->>RewardPointsService: createRewardPoints(rewardPoints)
    RewardPointsService->>RewardPoints: setLastUpdated(LocalDateTime.now())
    RewardPointsService->>RewardPointsMapper: insert(rewardPoints)
    RewardPointsMapper-->>Database: INSERT RewardPoints
    Database-->>RewardPointsMapper: (rewardPointsId generated)
    RewardPointsMapper-->>RewardPointsService: (rewardPoints with rewardPointsId)
    RewardPointsService-->>RewardPointsController: RewardPoints (created)
    RewardPointsController-->>Client: 200 OK (RewardPoints)
```

**단계별 상세 설명:**
1.  **Client -> RewardPointsController**: 클라이언트는 새로운 리워드 포인트 정보를 담은 `RewardPoints` 객체(주로 `memberId`와 초기 `points`)를 `/api/rewardpoints` 엔드포인트로 POST 요청합니다.
2.  **RewardPointsController -> RewardPointsService**: `RewardPointsController`는 `RewardPointsService.createRewardPoints()` 메서드를 호출합니다.
3.  **RewardPointsService -> RewardPoints**: `RewardPointsService`는 `RewardPoints` 객체의 `lastUpdated` 필드를 현재 시간으로 설정합니다.
4.  **RewardPointsService -> RewardPointsMapper**: `RewardPointsService`는 `RewardPointsMapper.insert()`를 호출하여 리워드 포인트 정보를 데이터베이스에 저장합니다. 이 과정에서 `rewardPointsId`가 자동 생성됩니다.
5.  **RewardPointsService -> RewardPointsController**: 생성된 `RewardPoints` 객체를 `RewardPointsController`로 반환합니다.
6.  **RewardPointsController -> Client**: `RewardPointsController`는 생성된 `RewardPoints` 객체를 클라이언트에게 200 OK 응답으로 반환합니다.

### 2.2. 리워드 포인트 조회 (getRewardPointsById, getRewardPointsByMemberId)
```mermaid
sequenceDiagram
    participant Client
    participant RewardPointsController
    participant RewardPointsService
    participant RewardPointsMapper
    database Database

    Client->>RewardPointsController: GET /api/rewardpoints/{rewardPointsId}
    RewardPointsController->>RewardPointsService: getRewardPointsById(rewardPointsId)
    RewardPointsService->>RewardPointsMapper: findByRewardPointsId(rewardPointsId)
    RewardPointsMapper-->>Database: SELECT RewardPoints
    Database-->>RewardPointsMapper: RewardPoints (or null)
    RewardPointsMapper-->>RewardPointsService: Optional<RewardPoints>
    RewardPointsService-->>RewardPointsController: Optional<RewardPoints>
    alt RewardPoints Found
        RewardPointsController-->>Client: 200 OK (RewardPoints)
    else RewardPoints Not Found
        RewardPointsController-->>Client: 404 Not Found
    end

    Client->>RewardPointsController: GET /api/rewardpoints/member/{memberId}
    RewardPointsController->>RewardPointsService: getRewardPointsByMemberId(memberId)
    RewardPointsService->>RewardPointsMapper: findByMemberId(memberId)
    RewardPointsMapper-->>Database: SELECT RewardPoints
    Database-->>RewardPointsMapper: RewardPoints (or null)
    RewardPointsMapper-->>RewardPointsService: RewardPoints
    RewardPointsService-->>RewardPointsController: RewardPoints
    alt RewardPoints Found
        RewardPointsController-->>Client: 200 OK (RewardPoints)
    else RewardPoints Not Found
        RewardPointsController-->>Client: 404 Not Found
    end
```

**단계별 상세 설명:**
1.  **Client -> RewardPointsController**: 특정 리워드 포인트 조회를 위해 `/api/rewardpoints/{rewardPointsId}` 또는 `/api/rewardpoints/member/{memberId}`로 GET 요청을 보냅니다.
2.  **RewardPointsController -> RewardPointsService**: `RewardPointsController`는 `RewardPointsService.getRewardPointsById()` 또는 `RewardPointsService.getRewardPointsByMemberId()`를 호출합니다.
3.  **RewardPointsService -> RewardPointsMapper**: `RewardPointsService`는 `RewardPointsMapper.findByRewardPointsId()` 또는 `RewardPointsMapper.findByMemberId()`를 호출하여 데이터베이스에서 리워드 포인트 정보를 조회합니다.
4.  **RewardPointsService -> RewardPointsController**: 조회된 `Optional<RewardPoints>` 또는 `RewardPoints` 객체를 `RewardPointsController`로 반환합니다.
5.  **RewardPointsController -> Client**: `RewardPointsController`는 조회 결과에 따라 200 OK (포인트 정보) 또는 404 Not Found (포인트 없음) 응답을 클라이언트에게 반환합니다.

### 2.3. 포인트 적립 (addPoints)
```mermaid
sequenceDiagram
    participant Client
    participant RewardPointsController
    participant RewardPointsService
    participant RewardPointsMapper
    participant PointHistoryService
    database Database

    Client->>RewardPointsController: PUT /api/rewardpoints/add (RewardPointsRequest)
    RewardPointsController->>RewardPointsService: addPoints(memberId, pointsToAdd)
    RewardPointsService->>RewardPointsMapper: findByMemberId(memberId)
    RewardPointsMapper-->>Database: SELECT RewardPoints
    Database-->>RewardPointsMapper: RewardPoints (existing or null)
    alt RewardPoints Not Found (first time earning)
        RewardPointsService->>RewardPoints: new RewardPoints(memberId, pointsToAdd)
        RewardPointsService->>RewardPointsMapper: insert(newRewardPoints)
        RewardPointsMapper-->>Database: INSERT RewardPoints
    else RewardPoints Found
        RewardPointsService->>RewardPoints: update points and lastUpdated
        RewardPointsService->>RewardPointsMapper: update(rewardPoints)
        RewardPointsMapper-->>Database: UPDATE RewardPoints
    end
    RewardPointsService->>PointHistoryService: recordPointEarn(memberId, pointsToAdd, ...)
    PointHistoryService-->>Database: INSERT PointHistory
    RewardPointsService-->>RewardPointsController: RewardPoints (updated)
    RewardPointsController-->>Client: 200 OK (RewardPoints)
```

**단계별 상세 설명:**
1.  **Client -> RewardPointsController**: 클라이언트는 포인트를 적립할 `memberId`와 `points`를 담은 `RewardPointsRequest`를 `/api/rewardpoints/add` 엔드포인트로 PUT 요청합니다.
2.  **RewardPointsController -> RewardPointsService**: `RewardPointsController`는 `RewardPointsService.addPoints()` 메서드를 호출합니다.
3.  **RewardPointsService -> RewardPointsMapper**: `RewardPointsService`는 `RewardPointsMapper.findByMemberId()`를 호출하여 해당 `memberId`의 기존 리워드 포인트 정보를 조회합니다.
4.  **RewardPointsService (포인트 정보 처리)**:
    *   기존 포인트 정보가 없으면 (`rewardPoints == null`), 새로운 `RewardPoints` 객체를 생성하고 `RewardPointsMapper.insert()`를 통해 데이터베이스에 저장합니다.
    *   기존 포인트 정보가 있으면, `points`를 `pointsToAdd`만큼 증가시키고 `lastUpdated`를 현재 시간으로 업데이트한 후 `RewardPointsMapper.update()`를 통해 데이터베이스에 업데이트합니다.
5.  **RewardPointsService -> PointHistoryService**: `RewardPointsService`는 `PointHistoryService.recordPointEarn()`을 호출하여 포인트 적립 내역을 `PointHistory` 테이블에 기록합니다.
6.  **RewardPointsService -> RewardPointsController**: 업데이트된 `RewardPoints` 객체를 `RewardPointsController`로 반환합니다.
7.  **RewardPointsController -> Client**: `RewardPointsController`는 업데이트된 `RewardPoints` 객체를 클라이언트에게 200 OK 응답으로 반환합니다.

### 2.4. 포인트 사용 (usePoints)
```mermaid
sequenceDiagram
    participant Client
    participant RewardPointsController
    participant RewardPointsService
    participant RewardPointsMapper
    database Database

    Client->>RewardPointsController: PUT /api/rewardpoints/use (RewardPointsRequest)
    RewardPointsController->>RewardPointsService: usePoints(memberId, pointsToUse)
    RewardPointsService->>RewardPointsMapper: findByMemberId(memberId)
    RewardPointsMapper-->>Database: SELECT RewardPoints
    Database-->>RewardPointsMapper: RewardPoints (existing)
    alt Insufficient Points
        RewardPointsService--xRewardPointsController: throws IllegalStateException
        RewardPointsController--xClient: 400 Bad Request
    else Sufficient Points
        RewardPointsService->>RewardPoints: update points and lastUpdated
        RewardPointsService->>RewardPointsMapper: update(rewardPoints)
        RewardPointsMapper-->>Database: UPDATE RewardPoints
        RewardPointsService-->>RewardPointsController: RewardPoints (updated)
        RewardPointsController-->>Client: 200 OK (RewardPoints)
    end
```

**단계별 상세 설명:**
1.  **Client -> RewardPointsController**: 클라이언트는 포인트를 사용할 `memberId`와 `points`를 담은 `RewardPointsRequest`를 `/api/rewardpoints/use` 엔드포인트로 PUT 요청합니다.
2.  **RewardPointsController -> RewardPointsService**: `RewardPointsController`는 `RewardPointsService.usePoints()` 메서드를 호출합니다.
3.  **RewardPointsService -> RewardPointsMapper**: `RewardPointsService`는 `RewardPointsMapper.findByMemberId()`를 호출하여 해당 `memberId`의 기존 리워드 포인트 정보를 조회합니다.
4.  **RewardPointsService (포인트 잔액 확인)**: 조회된 `RewardPoints`가 없거나 (`rewardPoints == null`) 사용하려는 포인트(`pointsToUse`)가 현재 잔액(`rewardPoints.getPoints()`)보다 많으면 `IllegalStateException`을 발생시킵니다.
5.  **RewardPointsService -> RewardPointsMapper**: 포인트 사용이 가능하면, `points`를 `pointsToUse`만큼 감소시키고 `lastUpdated`를 현재 시간으로 업데이트한 후 `RewardPointsMapper.update()`를 통해 데이터베이스에 업데이트합니다.
6.  **RewardPointsService -> RewardPointsController**: 업데이트된 `RewardPoints` 객체를 `RewardPointsController`로 반환합니다.
7.  **RewardPointsController -> Client**: `RewardPointsController`는 업데이트된 `RewardPoints` 객체를 클라이언트에게 200 OK 응답으로 반환합니다.

## 3. 데이터 구조

### 3.1. 데이터베이스
`schema.sql` 또는 `pay.sql` 파일이 없으므로, `RewardPoints` 엔티티와 `RewardPointsMapper.xml`을 기반으로 스키마를 유추합니다.

**`RewardPoints` 테이블 (유추)**
```sql
CREATE TABLE reward_points (
    reward_points_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT UNIQUE NOT NULL,
    points BIGINT NOT NULL DEFAULT 0,
    last_updated DATETIME NOT NULL
);
```

**각 필드의 타입, 제약조건, 기본값, 인덱스:**
- `reward_points_id`: `BIGINT`, `AUTO_INCREMENT`, `PRIMARY KEY`, 리워드 포인트 고유 ID.
- `member_id`: `BIGINT`, `UNIQUE`, `NOT NULL`, 회원 ID (Member 테이블의 FK). 한 회원당 하나의 리워드 포인트 레코드만 가짐.
- `points`: `BIGINT`, `NOT NULL`, `DEFAULT 0`, 현재 보유 포인트.
- `last_updated`: `DATETIME`, `NOT NULL`, 마지막 업데이트 일시.
- `INDEX`: `member_id` 컬럼에 `UNIQUE INDEX`.

### 3.2. DTO/API 모델

#### `RewardPoints` (Entity/Request/Response DTO)
```java
package com.vibe.pay.backend.rewardpoints;

import java.time.LocalDateTime;

// ... (getter/setter 생략)
public class RewardPoints {
    private Long rewardPointsId;
    private Long memberId;
    private Long points;
    private LocalDateTime lastUpdated;
}
```
- **검증 규칙 (생성 시)**:
    - `memberId`: `NOT NULL`, `Long` 타입, 유효한 회원 ID.
    - `points`: `NOT NULL`, `Long` 타입, 0 이상.

#### `RewardPointsRequest` (Request DTO)
```java
package com.vibe.pay.backend.rewardpoints;

// ... (getter/setter 생략)
public class RewardPointsRequest {
    private Long memberId;
    private Long points;
}
```
- **검증 규칙**:
    - `memberId`: `NOT NULL`, `Long` 타입, 유효한 회원 ID.
    - `points`: `NOT NULL`, `Long` 타입, 0보다 커야 함 (적립/사용 시).

## 4. API 명세

### 4.1. 리워드 포인트 생성
- **Endpoint**: `/api/rewardpoints`
- **HTTP Method**: `POST`
- **인증 요구사항**: 필요 (관리자 권한 또는 내부 시스템 호출)
- **Request 예시 (JSON)**:
    ```json
    {
        "memberId": 1,
        "points": 0
    }
    ```
- **검증 규칙**: `RewardPoints` DTO 참조.
- **Success Response (예시)**:
    ```json
    {
        "rewardPointsId": 1,
        "memberId": 1,
        "points": 0,
        "lastUpdated": "2025-10-16T11:30:00"
    }
    ```
- **Error Response**:
    - **HTTP Status**: `400 Bad Request`
    - **메시지**: (내부 서버 오류 또는 유효성 검증 실패 메시지)
    - **상황**: 필수 파라미터 누락, `memberId` 중복 (이미 포인트 정보가 있는 회원).

### 4.2. 특정 리워드 포인트 조회 (by rewardPointsId)
- **Endpoint**: `/api/rewardpoints/{rewardPointsId}`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (관리자 권한)
- **Request 예시**: 없음 (Path Variable `rewardPointsId` 사용)
- **검증 규칙**: `rewardPointsId`는 `Long` 타입의 양수여야 함.
- **Success Response (예시)**:
    ```json
    {
        "rewardPointsId": 1,
        "memberId": 1,
        "points": 1000,
        "lastUpdated": "2025-10-16T11:35:00"
    }
    ```
- **Error Response**:
    - **HTTP Status**: `404 Not Found`
    - **메시지**: (응답 본문 없음)
    - **상황**: `rewardPointsId`에 해당하는 리워드 포인트 정보가 없을 때.

### 4.3. 특정 회원 리워드 포인트 조회 (by memberId)
- **Endpoint**: `/api/rewardpoints/member/{memberId}`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (해당 회원 또는 관리자)
- **Request 예시**: 없음 (Path Variable `memberId` 사용)
- **검증 규칙**: `memberId`는 `Long` 타입의 양수여야 함.
- **Success Response (예시)**:
    ```json
    {
        "rewardPointsId": 1,
        "memberId": 1,
        "points": 1000,
        "lastUpdated": "2025-10-16T11:35:00"
    }
    ```
- **Error Response**:
    - **HTTP Status**: `404 Not Found`
    - **메시지**: (응답 본문 없음)
    - **상황**: `memberId`에 해당하는 리워드 포인트 정보가 없을 때.

### 4.4. 포인트 적립
- **Endpoint**: `/api/rewardpoints/add`
- **HTTP Method**: `PUT`
- **인증 요구사항**: 필요 (관리자 권한 또는 내부 시스템 호출)
- **Request 예시 (JSON)**:
    ```json
    {
        "memberId": 1,
        "points": 500
    }
    ```
- **검증 규칙**: `RewardPointsRequest` DTO 참조.
- **Success Response (예시)**:
    ```json
    {
        "rewardPointsId": 1,
        "memberId": 1,
        "points": 1500,
        "lastUpdated": "2025-10-16T11:40:00"
    }
    ```
- **Error Response**:
    - **HTTP Status**: `400 Bad Request`
    - **메시지**: (내부 서버 오류 또는 유효성 검증 실패 메시지)
    - **상황**: `memberId`에 해당하는 포인트 정보가 없거나, `points`가 음수 또는 0.

### 4.5. 포인트 사용
- **Endpoint**: `/api/rewardpoints/use`
- **HTTP Method**: `PUT`
- **인증 요구사항**: 필요 (해당 회원 또는 내부 시스템 호출)
- **Request 예시 (JSON)**:
    ```json
    {
        "memberId": 1,
        "points": 300
    }
    ```
- **검증 규칙**: `RewardPointsRequest` DTO 참조.
- **Success Response (예시)**:
    ```json
    {
        "rewardPointsId": 1,
        "memberId": 1,
        "points": 1200,
        "lastUpdated": "2025-10-16T11:45:00"
    }
    ```
- **Error Response**:
    - **HTTP Status**: `400 Bad Request`
    - **메시지**: "Insufficient reward points for member [memberId]"
    - **상황**: `memberId`에 해당하는 포인트 정보가 없거나, 사용하려는 포인트가 현재 잔액보다 많을 때, `points`가 음수 또는 0.

## 5. 비즈니스 로직 상세

### 5.1. 리워드 포인트 생성 (`RewardPointsService.createRewardPoints`)
- **목적**: 새로운 회원이 가입하거나 특정 이벤트로 인해 초기 포인트 정보가 필요할 때, 해당 회원의 리워드 포인트 레코드를 생성합니다.
- **입력 파라미터**: `RewardPoints rewardPoints`
- **계산 로직**:
    1.  `rewardPoints.setLastUpdated(LocalDateTime.now())`를 호출하여 `lastUpdated` 필드를 현재 시간으로 설정합니다.
    2.  `rewardPointsMapper.insert(rewardPoints)`를 호출하여 리워드 포인트 정보를 DB에 저장합니다. 이 과정에서 `rewardPointsId`가 자동 생성되어 `rewardPoints` 객체에 설정됩니다.
- **제약조건**:
    - `memberId`는 `UNIQUE` 제약조건을 가집니다. 이미 포인트 정보가 있는 회원은 생성할 수 없습니다.
    - `points`는 0 이상이어야 합니다.
- **에러 케이스**:
    - `memberId` 중복 시 DB `UNIQUE` 제약조건 위반으로 예외 발생.
    - DB 저장 실패 시 예외 발생.

### 5.2. 포인트 적립 (`RewardPointsService.addPoints`)
- **목적**: 특정 회원에게 포인트를 적립하고, 포인트 변동 내역을 기록합니다.
- **입력 파라미터**: `Long memberId`, `Long pointsToAdd`
- **계산 로직**:
    1.  `rewardPointsMapper.findByMemberId(memberId)`를 호출하여 해당 `memberId`의 기존 리워드 포인트 정보를 조회합니다.
    2.  **포인트 정보가 없는 경우**: 새로운 `RewardPoints` 객체를 `memberId`와 `pointsToAdd`로 생성하고 `rewardPointsMapper.insert()`를 통해 DB에 저장합니다.
    3.  **포인트 정보가 있는 경우**: 기존 `RewardPoints` 객체의 `points`에 `pointsToAdd`를 더하고, `lastUpdated`를 현재 시간으로 업데이트한 후 `rewardPointsMapper.update()`를 통해 DB에 업데이트합니다.
    4.  `pointHistoryService.recordPointEarn()`을 호출하여 포인트 적립 내역을 `PointHistory` 테이블에 기록합니다. (참조 타입: "MANUAL_CHARGE", 참조 ID: `rewardPointsId`, 설명: "마일리지 수동 충전")
- **제약조건**:
    - `pointsToAdd`는 0보다 커야 합니다.
- **에러 케이스**:
    - DB 조회/저장/업데이트 실패 시 예외 발생.
    - `PointHistoryService.recordPointEarn()` 실패 시 로그를 남기지만, 트랜잭션은 롤백되지 않고 포인트 적립은 계속 진행됩니다. (개선 필요)

### 5.3. 포인트 사용 (`RewardPointsService.usePoints`)
- **목적**: 특정 회원의 포인트를 사용하고, 포인트 변동 내역을 기록합니다.
- **입력 파라미터**: `Long memberId`, `Long pointsToUse`
- **계산 로직**:
    1.  `rewardPointsMapper.findByMemberId(memberId)`를 호출하여 해당 `memberId`의 기존 리워드 포인트 정보를 조회합니다.
    2.  **잔액 확인**: 조회된 `RewardPoints`가 없거나 (`rewardPoints == null`) 현재 잔액(`rewardPoints.getPoints()`)이 `pointsToUse`보다 적으면 `IllegalStateException`을 발생시킵니다.
    3.  **포인트 차감**: 기존 `RewardPoints` 객체의 `points`에서 `pointsToUse`를 빼고, `lastUpdated`를 현재 시간으로 업데이트한 후 `rewardPointsMapper.update()`를 통해 DB에 업데이트합니다.
    4.  (현재 코드에는 `PointHistoryService.recordPointUse()` 호출이 누락되어 있습니다. 개선 필요)
- **제약조건**:
    - `pointsToUse`는 0보다 커야 합니다.
    - `memberId`에 해당하는 포인트 정보가 존재하고, 잔액이 충분해야 합니다.
- **에러 케이스**:
    - 포인트 잔액 부족 시 `IllegalStateException` 발생.
    - DB 조회/업데이트 실패 시 예외 발생.

## 6. 에러 처리

- **에러 코드 체계**: 현재 `RewardPointsService`에서는 `IllegalStateException`을 주로 사용하고 있습니다. `RewardPointsController`에서는 이를 `400 Bad Request`로 매핑하고 있습니다.
    - **개선 방향**: `RewardPointsException`과 같은 커스텀 예외를 정의하고, 구체적인 에러 코드를 포함하여 에러 처리를 표준화해야 합니다.
- **각 에러별 HTTP Status, 처리 방법, 사용자 메시지**:
    - **포인트 적립/사용 실패 (회원 포인트 정보 없음)**:
        - HTTP Status: `400 Bad Request`
        - 처리 방법: 클라이언트에게 오류 메시지 반환, 로그 기록.
        - 사용자 메시지: "회원 [memberId]의 포인트 정보를 찾을 수 없습니다."
    - **포인트 사용 실패 (잔액 부족)**:
        - HTTP Status: `400 Bad Request`
        - 처리 방법: 클라이언트에게 오류 메시지 반환, 로그 기록.
        - 사용자 메시지: "포인트 잔액이 부족합니다."
    - **일반적인 DB 오류**: `500 Internal Server Error`

## 7. 트랜잭션 및 동시성

- **트랜잭션 경계**:
    - `RewardPointsService.addPoints()`: `@Transactional` 어노테이션이 적용되어 있으며, `RewardPoints` 업데이트와 `PointHistory` 기록이 하나의 트랜잭션으로 묶여 있습니다. (단, `PointHistory` 기록 실패 시 롤백되지 않는 문제점 있음)
    - `RewardPointsService.usePoints()`: `@Transactional` 어노테이션이 적용되어 있으며, `RewardPoints` 업데이트가 트랜잭션으로 처리됩니다.
- **동시성 문제 및 해결 방법**:
    - **포인트 적립/사용 시 동시성**: 동일한 회원의 포인트에 대해 여러 적립/사용 요청이 동시에 들어올 경우, 최종 포인트 잔액이 부정확해질 수 있습니다 (Lost Update).
        - **개선 방향**: `RewardPoints` 엔티티에 대한 비관적 락(Pessimistic Lock, `SELECT ... FOR UPDATE`) 또는 낙관적 락(Optimistic Lock, `version` 필드)을 사용하여 동시성 문제를 해결해야 합니다. 특히 `usePoints` 메서드에서 잔액 확인 후 차감하는 로직은 Race Condition에 취약합니다.

## 8. 성능 최적화

- **쿼리 최적화**:
    - `RewardPointsMapper.findByMemberId()`는 `member_id` 컬럼에 `UNIQUE INDEX`가 있으므로 효율적입니다.
- **인덱스 전략**:
    - `reward_points` 테이블의 `member_id` 컬럼에 `UNIQUE INDEX`가 필수적입니다.
- **캐싱 전략**: 현재 코드에는 명시적인 캐싱 전략이 적용되어 있지 않습니다.
    - **개선 방향**: 자주 조회되는 회원의 포인트 정보(예: `getRewardPointsByMemberId()`)에 대해 캐싱(예: Redis, Ehcache)을 적용하여 DB 부하를 줄일 수 있습니다.

## 9. 보안

- **입력 검증**:
    - `RewardPointsRequest` DTO에 대한 `@Valid` 어노테이션을 통한 입력값 검증이 현재는 명시적으로 보이지 않습니다.
    - **개선 방향**: Spring `Validation` API를 사용하여 DTO 필드에 `@NotNull`, `@Min` 등의 어노테이션을 적용하고, `RewardPointsController`에서 `@Valid`를 사용하여 자동 검증을 수행해야 합니다.
- **인증/권한 체크**:
    - `RewardPointsController`의 `createRewardPoints`, `addPoints` API는 관리자 권한 또는 내부 시스템 호출로 제한되어야 합니다.
    - `usePoints`, `getRewardPointsByMemberId` API는 해당 회원 또는 관리자만 접근 가능하도록 인증 및 권한 체크 로직이 필요합니다.
    - **개선 방향**: `@PreAuthorize` 또는 인터셉터/필터를 사용하여 적절한 권한을 가진 사용자만 해당 API에 접근할 수 있도록 구현해야 합니다.

## 10. 테스트 케이스

### 10.1. 정상 시나리오 (Happy Path)
- **포인트 생성**: 유효한 `memberId`와 초기 `points`로 포인트 정보 생성 시, DB에 `RewardPoints` 레코드가 저장되는지 확인.
- **포인트 조회**: `memberId`로 포인트 조회 시 올바른 포인트 잔액이 반환되는지 확인.
- **포인트 적립**: `memberId`와 `pointsToAdd`로 포인트 적립 시, 포인트 잔액이 올바르게 증가하고 `PointHistory`에 적립 내역이 기록되는지 확인.
- **포인트 사용**: `memberId`와 `pointsToUse`로 포인트 사용 시, 포인트 잔액이 올바르게 감소하는지 확인.

### 10.2. 예외 시나리오 (각 에러 케이스)
- **포인트 생성 실패**: 이미 포인트 정보가 있는 `memberId`로 생성 시 `400 Bad Request` 응답.
- **포인트 조회 실패**: 존재하지 않는 `memberId`로 포인트 조회 시 `404 Not Found` 응답.
- **포인트 적립 실패**: 존재하지 않는 `memberId`로 적립 시 `400 Bad Request` 응답.
- **포인트 사용 실패**:
    - 존재하지 않는 `memberId`로 사용 시 `400 Bad Request` 응답.
    - 잔액보다 많은 포인트를 사용하려 할 때 `400 Bad Request` 응답.
    - `points`가 음수 또는 0인 경우 `400 Bad Request` 응답.

### 10.3. 경계값 테스트
- `points`가 0인 경우 (생성, 적립, 사용).
- `points`가 매우 크거나 작은 값인 경우.
- `memberId`가 음수 또는 0인 경우.

### 10.4. 동시성 테스트
- 동일한 회원의 포인트에 대해 여러 적립/사용 요청이 동시에 들어왔을 때 최종 포인트 잔액의 정확성 확인.

## 11. 알려진 이슈 및 개선 방향

### 11.1. 코드 품질 및 구조
- **`RewardPoints` 엔티티의 역할**: `RewardPoints` 클래스가 엔티티 역할과 DTO 역할을 겸하고 있습니다.
    - **개선 방향**: 엔티티는 영속성 계층에만 사용하고, API 요청/응답을 위한 DTO를 별도로 정의하여 계층 간의 관심사를 분리해야 합니다.
- **`RewardPointsService.addPoints`의 `PointHistory` 기록 실패 처리**: `PointHistoryService.recordPointEarn()` 호출 중 예외 발생 시 로그만 남기고 트랜잭션이 롤백되지 않습니다. 이는 `RewardPoints` 잔액과 `PointHistory` 기록의 불일치를 야기할 수 있습니다.
    - **개선 방향**: `PointHistory` 기록 실패 시 `RewardPoints` 업데이트도 롤백되도록 트랜잭션 처리를 강화하거나, 비동기 메시징을 통해 재시도 로직을 구현해야 합니다.
- **`RewardPointsService.usePoints`의 `PointHistory` 기록 누락**: 포인트 사용 시 `PointHistory`에 내역을 기록하는 로직이 누락되어 있습니다.
    - **개선 방향**: `PointHistoryService.recordPointUse()`와 같은 메서드를 호출하여 포인트 사용 내역을 기록해야 합니다.



### 11.3. 리팩토링 포인트
- **`RewardPointsController`의 에러 처리**: `addPoints`, `usePoints` 메서드에서 `RuntimeException`을 catch하여 `ResponseEntity.badRequest().body(null)`를 반환하고 있습니다.
    - **개선 방향**: `GlobalExceptionHandler`를 통해 예외를 중앙 집중식으로 처리하고, `RewardPointsException`과 같은 커스텀 예외를 사용하여 에러 처리를 표준화해야 합니다.


