# Member Management Technical Specification

## 1. 개요
- **목적**: 본 문서는 VibePay 시스템의 회원 관리 기능에 대한 기술 사양을 정의합니다. 이 기능은 회원 정보(이름, 주소, 연락처, 이메일 등)의 생성, 조회, 수정, 삭제를 담당하며, 회원의 포인트 내역 및 주문 내역 조회와 연동됩니다.
- **System Design에서의 위치**: VibePay 백엔드 서비스의 핵심 모듈 중 하나로, 사용자 인증 및 권한 부여의 기반이 됩니다. 포인트(RewardPoints) 모듈과 연동하여 회원 가입 시 초기 포인트를 지급하고, 포인트 내역을 조회합니다. 주문(Order) 모듈과 연동하여 회원별 주문 내역을 조회합니다.
- **관련 컴포넌트 및 의존성**:
    - `MemberController`: 회원 관련 API 엔드포인트 제공
    - `MemberService`: 회원 비즈니스 로직 처리
    - `MemberMapper`: 회원 데이터베이스 CRUD
    - `Member`: 회원 엔티티
    - `RewardPointsService`: 회원 가입 시 초기 포인트 지급, 포인트 내역 조회
    - `PointHistoryService`: 회원 포인트 내역 조회
    - `OrderService`: 회원 주문 내역 조회

## 2. 프로세스 흐름

### 2.1. 회원 생성 (createMember)
```mermaid
sequenceDiagram
    participant Client
    participant MemberController
    participant MemberService
    participant MemberMapper
    participant RewardPointsService
    database Database

    Client->>MemberController: POST /api/members (Member)
    MemberController->>MemberService: createMember(member)
    MemberService->>Member: setCreatedAt(LocalDateTime.now())
    MemberService->>MemberMapper: insert(member)
    MemberMapper-->>Database: INSERT Member
    Database-->>MemberMapper: (memberId generated)
    MemberMapper-->>MemberService: (member with memberId)
    MemberService->>RewardPointsService: createRewardPoints(new RewardPoints(memberId, 0L))
    RewardPointsService-->>MemberService: (void)
    MemberService-->>MemberController: Member (created)
    MemberController-->>Client: 200 OK (Member)
```

**단계별 상세 설명:**
1.  **Client -> MemberController**: 클라이언트는 새로운 회원 정보를 담은 `Member` 객체를 `/api/members` 엔드포인트로 POST 요청합니다.
2.  **MemberController -> MemberService**: `MemberController`는 `MemberService.createMember()` 메서드를 호출합니다.
3.  **MemberService -> Member**: `MemberService`는 `Member` 객체의 `createdAt` 필드가 `null`인 경우 현재 시간으로 설정합니다.
4.  **MemberService -> MemberMapper**: `MemberService`는 `MemberMapper.insert()`를 호출하여 회원 정보를 데이터베이스에 저장합니다. 이 과정에서 `memberId`가 자동 생성됩니다.
5.  **MemberService -> RewardPointsService**: 회원 정보 저장 후, `MemberService`는 `RewardPointsService.createRewardPoints()`를 호출하여 새로 생성된 회원에게 초기 0 포인트를 지급합니다.
6.  **MemberService -> MemberController**: 생성된 `Member` 객체를 `MemberController`로 반환합니다.
7.  **MemberController -> Client**: `MemberController`는 생성된 `Member` 객체를 클라이언트에게 200 OK 응답으로 반환합니다.

### 2.2. 회원 정보 조회 (getMemberById, getAllMembers)
```mermaid
sequenceDiagram
    participant Client
    participant MemberController
    participant MemberService
    participant MemberMapper
    database Database

    Client->>MemberController: GET /api/members/{memberId}
    MemberController->>MemberService: getMemberById(memberId)
    MemberService->>MemberMapper: findByMemberId(memberId)
    MemberMapper-->>Database: SELECT Member
    Database-->>MemberMapper: Member (or null)
    MemberMapper-->>MemberService: Optional<Member>
    MemberService-->>MemberController: Optional<Member>
    alt Member Found
        MemberController-->>Client: 200 OK (Member)
    else Member Not Found
        MemberController-->>Client: 404 Not Found
    end

    Client->>MemberController: GET /api/members
    MemberController->>MemberService: getAllMembers()
    MemberService->>MemberMapper: findAll()
    MemberMapper-->>Database: SELECT all Members
    Database-->>MemberMapper: List<Member>
    MemberMapper-->>MemberService: List<Member>
    MemberService-->>MemberController: List<Member>
    MemberController-->>Client: 200 OK (List<Member>)
```

**단계별 상세 설명:**
1.  **Client -> MemberController**: 특정 회원 조회를 위해 `/api/members/{memberId}`로 GET 요청을 보내거나, 모든 회원 조회를 위해 `/api/members`로 GET 요청을 보냅니다.
2.  **MemberController -> MemberService**: `MemberController`는 `MemberService.getMemberById()` 또는 `MemberService.getAllMembers()`를 호출합니다.
3.  **MemberService -> MemberMapper**: `MemberService`는 `MemberMapper.findByMemberId()` 또는 `MemberMapper.findAll()`를 호출하여 데이터베이스에서 회원 정보를 조회합니다.
4.  **MemberService -> MemberController**: 조회된 `Optional<Member>` 또는 `List<Member>`를 `MemberController`로 반환합니다.
5.  **MemberController -> Client**: `MemberController`는 조회 결과에 따라 200 OK (회원 정보) 또는 404 Not Found (회원 없음) 응답을 클라이언트에게 반환합니다.

### 2.3. 회원 정보 수정 (updateMember)
```mermaid
sequenceDiagram
    participant Client
    participant MemberController
    participant MemberService
    participant MemberMapper
    database Database

    Client->>MemberController: PUT /api/members/{memberId} (Member details)
    MemberController->>MemberService: updateMember(memberId, memberDetails)
    MemberService->>MemberMapper: findByMemberId(memberId)
    MemberMapper-->>Database: SELECT Member
    Database-->>MemberMapper: Member (existing)
    alt Member Not Found
        MemberMapper-->>MemberService: null
        MemberService--xMemberController: throws IllegalArgumentException
        MemberController--xClient: 404 Not Found
    else Member Found
        MemberService->>Member: setMemberId(memberId)
        MemberService->>MemberMapper: update(memberDetails)
        MemberMapper-->>Database: UPDATE Member
        Database-->>MemberMapper: (void)
        MemberMapper-->>MemberService: (void)
        MemberService-->>MemberController: Member (updated)
        MemberController-->>Client: 200 OK (Member)
    end
```

**단계별 상세 설명:**
1.  **Client -> MemberController**: 클라이언트는 수정할 회원 ID와 새로운 회원 정보를 담은 `Member` 객체를 `/api/members/{memberId}` 엔드포인트로 PUT 요청합니다.
2.  **MemberController -> MemberService**: `MemberController`는 `MemberService.updateMember()` 메서드를 호출합니다.
3.  **MemberService -> MemberMapper**: `MemberService`는 `MemberMapper.findByMemberId()`를 호출하여 해당 `memberId`의 기존 회원 정보를 조회합니다.
4.  **MemberService (회원 존재 여부 확인)**: 기존 회원이 없으면 `IllegalArgumentException`을 발생시킵니다.
5.  **MemberService -> MemberMapper**: 기존 회원이 존재하면, `memberDetails` 객체에 `memberId`를 설정한 후 `MemberMapper.update()`를 호출하여 데이터베이스의 회원 정보를 업데이트합니다.
6.  **MemberService -> MemberController**: 업데이트된 `Member` 객체를 `MemberController`로 반환합니다.
7.  **MemberController -> Client**: `MemberController`는 업데이트된 `Member` 객체를 클라이언트에게 200 OK 응답으로 반환합니다. 회원을 찾을 수 없으면 404 Not Found 응답을 반환합니다.

### 2.4. 회원 삭제 (deleteMember)
```mermaid
sequenceDiagram
    participant Client
    participant MemberController
    participant MemberService
    participant MemberMapper
    database Database

    Client->>MemberController: DELETE /api/members/{memberId}
    MemberController->>MemberService: deleteMember(memberId)
    MemberService->>MemberMapper: delete(memberId)
    MemberMapper-->>Database: DELETE Member
    Database-->>MemberMapper: (void)
    MemberMapper-->>MemberService: (void)
    MemberService-->>MemberController: (void)
    MemberController-->>Client: 204 No Content
```

**단계별 상세 설명:**
1.  **Client -> MemberController**: 클라이언트는 삭제할 회원 ID를 포함하여 `/api/members/{memberId}` 엔드포인트로 DELETE 요청합니다.
2.  **MemberController -> MemberService**: `MemberController`는 `MemberService.deleteMember()` 메서드를 호출합니다.
3.  **MemberService -> MemberMapper**: `MemberService`는 `MemberMapper.delete()`를 호출하여 데이터베이스에서 회원 정보를 삭제합니다.
4.  **MemberService -> MemberController**: 삭제 처리 후 `void`를 반환합니다.
5.  **MemberController -> Client**: `MemberController`는 204 No Content 응답을 클라이언트에게 반환합니다.

### 2.5. 회원별 포인트 내역 조회 (getPointHistory)
```mermaid
sequenceDiagram
    participant Client
    participant MemberController
    participant PointHistoryService
    database Database

    Client->>MemberController: GET /api/members/{memberId}/point-history?page=0&size=10
    MemberController->>PointHistoryService: getPointHistoryByMemberWithPaging(memberId, page, size)
    PointHistoryService-->>Database: SELECT PointHistory with paging
    Database-->>PointHistoryService: List<PointHistory>
    PointHistoryService-->>MemberController: List<PointHistory>
    MemberController-->>Client: 200 OK (List<PointHistory>)
```

**단계별 상세 설명:**
1.  **Client -> MemberController**: 클라이언트는 특정 회원의 포인트 내역을 조회하기 위해 `/api/members/{memberId}/point-history` 엔드포인트로 GET 요청을 보냅니다. `page`와 `size` 파라미터를 통해 페이징을 지원합니다.
2.  **MemberController -> PointHistoryService**: `MemberController`는 `PointHistoryService.getPointHistoryByMemberWithPaging()` 메서드를 호출합니다.
3.  **PointHistoryService -> Database**: `PointHistoryService`는 데이터베이스에서 해당 `memberId`의 포인트 내역을 페이징 처리하여 조회합니다.
4.  **PointHistoryService -> MemberController**: 조회된 `List<PointHistory>`를 `MemberController`로 반환합니다.
5.  **MemberController -> Client**: `MemberController`는 조회된 `List<PointHistory>`를 클라이언트에게 200 OK 응답으로 반환합니다.

### 2.6. 회원별 주문 내역 조회 (getOrderHistory)
```mermaid
sequenceDiagram
    participant Client
    participant MemberController
    participant OrderService
    database Database

    Client->>MemberController: GET /api/members/{memberId}/order-history?page=0&size=10
    MemberController->>OrderService: getOrderDetailsWithPaymentsByMemberIdWithPaging(memberId, page, size)
    OrderService-->>Database: SELECT Order, OrderItem, Payment, Product with paging
    Database-->>OrderService: List<OrderDetailDto>
    OrderService-->>MemberController: List<OrderDetailDto>
    MemberController-->>Client: 200 OK (List<OrderDetailDto>)
```

**단계별 상세 설명:**
1.  **Client -> MemberController**: 클라이언트는 특정 회원의 주문 내역을 조회하기 위해 `/api/members/{memberId}/order-history` 엔드포인트로 GET 요청을 보냅니다. `page`와 `size` 파라미터를 통해 페이징을 지원합니다.
2.  **MemberController -> OrderService**: `MemberController`는 `OrderService.getOrderDetailsWithPaymentsByMemberIdWithPaging()` 메서드를 호출합니다.
3.  **OrderService -> Database**: `OrderService`는 데이터베이스에서 해당 `memberId`의 주문 상세 내역(주문, 주문 상품, 결제, 상품 정보 포함)을 페이징 처리하여 조회합니다.
4.  **OrderService -> MemberController**: 조회된 `List<OrderDetailDto>`를 `MemberController`로 반환합니다.
5.  **MemberController -> Client**: `MemberController`는 조회된 `List<OrderDetailDto>`를 클라이언트에게 200 OK 응답으로 반환합니다.

## 3. 데이터 구조

### 3.1. 데이터베이스
`schema.sql` 또는 `pay.sql` 파일이 없으므로, `Member` 엔티티와 `MemberMapper.xml`을 기반으로 스키마를 유추합니다.

**`Member` 테이블 (유추)**
```sql
CREATE TABLE member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(500),
    phone_number VARCHAR(20) UNIQUE,
    email VARCHAR(255) UNIQUE,
    created_at DATETIME NOT NULL
);
```

**각 필드의 타입, 제약조건, 기본값, 인덱스:**
- `member_id`: `BIGINT`, `AUTO_INCREMENT`, `PRIMARY KEY`, 회원 고유 ID.
- `name`: `VARCHAR(255)`, `NOT NULL`, 회원 이름.
- `shipping_address`: `VARCHAR(500)`, 배송 주소.
- `phone_number`: `VARCHAR(20)`, `UNIQUE`, 전화번호.
- `email`: `VARCHAR(255)`, `UNIQUE`, 이메일 주소.
- `created_at`: `DATETIME`, `NOT NULL`, 회원 가입 일시.
- `INDEX`: `phone_number`, `email` 컬럼에 대한 인덱스.

### 3.2. DTO/API 모델

#### `Member` (Entity/Request/Response DTO)
```java
package com.vibe.pay.backend.member;

import java.time.LocalDateTime;

// ... (getter/setter 생략)
public class Member {
    private Long memberId;
    private String name;
    private String shippingAddress;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
}
```
- **검증 규칙 (생성 시)**:
    - `name`: `NOT NULL`, `String` 타입, 비어있지 않아야 함.
    - `phoneNumber`: `String` 타입, `UNIQUE`, 유효한 전화번호 형식.
    - `email`: `String` 타입, `UNIQUE`, 유효한 이메일 형식.
- **검증 규칙 (수정 시)**:
    - `memberId`: `NOT NULL`, `Long` 타입, 유효한 회원 ID.
    - `name`, `shippingAddress`, `phoneNumber`, `email`: `String` 타입, 유효성 검증.

## 4. API 명세

### 4.1. 회원 생성
- **Endpoint**: `/api/members`
- **HTTP Method**: `POST`
- **인증 요구사항**: 없음 (회원 가입 API)
- **Request 예시 (JSON)**:
    ```json
    {
        "name": "새로운 회원",
        "shippingAddress": "서울시 강남구 테헤란로",
        "phoneNumber": "01012345678",
        "email": "new.member@example.com"
    }
    ```
- **검증 규칙**: `Member` DTO 참조.
- **Success Response (예시)**:
    ```json
    {
        "memberId": 1,
        "name": "새로운 회원",
        "shippingAddress": "서울시 강남구 테헤란로",
        "phoneNumber": "01012345678",
        "email": "new.member@example.com",
        "createdAt": "2025-10-16T11:00:00"
    }
    ```
- **Error Response**:
    - **HTTP Status**: `400 Bad Request`
    - **메시지**: (내부 서버 오류 또는 유효성 검증 실패 메시지)
    - **상황**: 필수 파라미터 누락, 유효하지 않은 형식, `phoneNumber` 또는 `email` 중복.

### 4.2. 모든 회원 조회
- **Endpoint**: `/api/members`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (관리자 권한)
- **Request 예시**: 없음
- **검증 규칙**: 없음
- **Success Response (예시)**:
    ```json
    [
        {
            "memberId": 1,
            "name": "새로운 회원",
            "shippingAddress": "서울시 강남구 테헤란로",
            "phoneNumber": "01012345678",
            "email": "new.member@example.com",
            "createdAt": "2025-10-16T11:00:00"
        }
        // ...
    ]
    ```

### 4.3. 특정 회원 조회
- **Endpoint**: `/api/members/{memberId}`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (해당 회원 또는 관리자)
- **Request 예시**: 없음 (Path Variable `memberId` 사용)
- **검증 규칙**: `memberId`는 `Long` 타입의 양수여야 함.
- **Success Response (예시)**:
    ```json
    {
        "memberId": 1,
        "name": "새로운 회원",
        "shippingAddress": "서울시 강남구 테헤란로",
        "phoneNumber": "01012345678",
        "email": "new.member@example.com",
        "createdAt": "2025-10-16T11:00:00"
    }
    ```
- **Error Response**:
    - **HTTP Status**: `404 Not Found`
    - **메시지**: (응답 본문 없음)
    - **상황**: `memberId`에 해당하는 회원이 없을 때.

### 4.4. 회원 정보 수정
- **Endpoint**: `/api/members/{memberId}`
- **HTTP Method**: `PUT`
- **인증 요구사항**: 필요 (해당 회원 또는 관리자)
- **Request 예시 (JSON)**:
    ```json
    {
        "name": "수정된 회원",
        "shippingAddress": "서울시 강남구 역삼동",
        "phoneNumber": "01098765432",
        "email": "updated.member@example.com"
    }
    ```
- **검증 규칙**: `Member` DTO 참조.
- **Success Response (예시)**:
    ```json
    {
        "memberId": 1,
        "name": "수정된 회원",
        "shippingAddress": "서울시 강남구 역삼동",
        "phoneNumber": "01098765432",
        "email": "updated.member@example.com",
        "createdAt": "2025-10-16T11:00:00"
    }
    ```
- **Error Response**:
    - **HTTP Status**: `404 Not Found`
    - **메시지**: (응답 본문 없음)
    - **상황**: `memberId`에 해당하는 회원이 없을 때.
    - **HTTP Status**: `400 Bad Request`
    - **메시지**: (유효성 검증 실패 메시지)
    - **상황**: `phoneNumber` 또는 `email` 중복, 유효하지 않은 형식.

### 4.5. 회원 삭제
- **Endpoint**: `/api/members/{memberId}`
- **HTTP Method**: `DELETE`
- **인증 요구사항**: 필요 (해당 회원 또는 관리자)
- **Request 예시**: 없음 (Path Variable `memberId` 사용)
- **검증 규칙**: `memberId`는 `Long` 타입의 양수여야 함.
- **Success Response**: `204 No Content`
- **Error Response**:
    - **HTTP Status**: `404 Not Found`
    - **메시지**: (응답 본문 없음)
    - **상황**: `memberId`에 해당하는 회원이 없을 때 (현재 구현은 404를 반환하지 않고 그냥 삭제 시도).

### 4.6. 회원별 포인트 내역 조회
- **Endpoint**: `/api/members/{memberId}/point-history`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (해당 회원 또는 관리자)
- **Request 예시**: `/api/members/1/point-history?page=0&size=10`
- **검증 규칙**: `memberId`는 `Long` 타입의 양수여야 함. `page`, `size`는 음수가 아니어야 함.
- **Success Response (예시)**:
    ```json
    [
        {
            "pointHistoryId": 1,
            "memberId": 1,
            "changeAmount": 1000,
            "currentPoints": 1000,
            "changeType": "CHARGE",
            "referenceId": "PAYMENT_123",
            "description": "포인트 충전",
            "changeDate": "2025-10-16T11:05:00"
        }
        // ...
    ]
    ```

### 4.7. 회원별 주문 내역 조회
- **Endpoint**: `/api/members/{memberId}/order-history`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (해당 회원 또는 관리자)
- **Request 예시**: `/api/members/1/order-history?page=0&size=10`
- **검증 규칙**: `memberId`는 `Long` 타입의 양수여야 함. `page`, `size`는 음수가 아니어야 함.
- **Success Response (예시)**: `Order Processing Technical Specification`의 `4.7. 회원별 주문 상세 조회`와 동일한 형식.

## 5. 비즈니스 로직 상세

### 5.1. 회원 생성 (`MemberService.createMember`)
- **목적**: 새로운 회원 정보를 데이터베이스에 저장하고, 동시에 해당 회원에게 초기 포인트를 지급합니다.
- **입력 파라미터**: `Member member`
- **계산 로직**:
    1.  `member.getCreatedAt()`이 `null`인 경우 `LocalDateTime.now()`로 설정합니다.
    2.  `memberMapper.insert(member)`를 호출하여 회원 정보를 DB에 저장합니다. 이 과정에서 `memberId`가 자동 생성되어 `member` 객체에 설정됩니다.
    3.  `rewardPointsService.createRewardPoints(new RewardPoints(member.getMemberId(), 0L))`를 호출하여 새로 생성된 `memberId`로 0 포인트를 가진 `RewardPoints` 엔티티를 생성합니다.
- **제약조건**:
    - `name`은 필수.
    - `phoneNumber`, `email`은 `UNIQUE` 제약조건을 가집니다.
    - `RewardPointsService`가 정상적으로 동작해야 합니다.
- **에러 케이스**:
    - `phoneNumber` 또는 `email` 중복 시 DB `UNIQUE` 제약조건 위반으로 예외 발생.
    - `RewardPointsService.createRewardPoints()` 실패 시 예외 발생.

### 5.2. 회원 정보 수정 (`MemberService.updateMember`)
- **목적**: 기존 회원의 정보를 업데이트합니다.
- **입력 파라미터**: `Long memberId`, `Member memberDetails`
- **계산 로직**:
    1.  `memberMapper.findByMemberId(memberId)`를 호출하여 `memberId`에 해당하는 회원이 존재하는지 확인합니다.
    2.  회원이 존재하지 않으면 `IllegalArgumentException`을 발생시킵니다.
    3.  `memberDetails.setMemberId(memberId)`를 호출하여 업데이트할 `Member` 객체에 정확한 `memberId`를 설정합니다.
    4.  `memberMapper.update(memberDetails)`를 호출하여 DB의 회원 정보를 업데이트합니다.
- **제약조건**:
    - `memberId`에 해당하는 회원이 존재해야 합니다.
    - `memberDetails`의 `phoneNumber` 또는 `email`이 다른 기존 회원과 중복되지 않아야 합니다.
- **에러 케이스**:
    - 회원을 찾을 수 없을 때 `IllegalArgumentException` 발생.
    - `phoneNumber` 또는 `email` 중복 시 DB `UNIQUE` 제약조건 위반으로 예외 발생.

### 5.3. 회원 삭제 (`MemberService.deleteMember`)
- **목적**: 특정 회원 정보를 데이터베이스에서 삭제합니다.
- **입력 파라미터**: `Long memberId`
- **계산 로직**:
    1.  `memberMapper.delete(memberId)`를 호출하여 `memberId`에 해당하는 회원 정보를 DB에서 삭제합니다.
- **제약조건**:
    - `memberId`에 해당하는 회원이 존재해야 합니다. (현재 구현은 존재하지 않아도 예외를 던지지 않고 삭제 시도)
- **에러 케이스**:
    - DB 삭제 실패 시 예외 발생.

## 6. 에러 처리

- **에러 코드 체계**: 현재 `MemberService`에서는 `IllegalArgumentException`을 주로 사용하고 있습니다. `MemberController`에서는 이를 `404 Not Found`로 매핑하고 있습니다.
    - **개선 방향**: `MemberException`과 같은 커스텀 예외를 정의하고, 구체적인 에러 코드를 포함하여 에러 처리를 표준화해야 합니다.
- **각 에러별 HTTP Status, 처리 방법, 사용자 메시지**:
    - **회원 생성 실패 (중복)**:
        - HTTP Status: `400 Bad Request`
        - 처리 방법: 클라이언트에게 오류 메시지 반환, 로그 기록.
        - 사용자 메시지: "이미 등록된 전화번호 또는 이메일입니다."
    - **회원 조회/수정/삭제 실패 (회원 없음)**:
        - HTTP Status: `404 Not Found`
        - 처리 방법: 클라이언트에게 오류 메시지 반환 (또는 응답 본문 없음), 로그 기록.
        - 사용자 메시지: "회원을 찾을 수 없습니다."
    - **일반적인 DB 오류**: `500 Internal Server Error`

## 7. 트랜잭션 및 동시성

- **트랜잭션 경계**:
    - `MemberService.createMember()`: `@Transactional` 어노테이션이 적용되어 있으며, 회원 정보 저장과 초기 포인트 지급이 하나의 트랜잭션으로 묶여 있습니다. 둘 중 하나라도 실패하면 롤백됩니다.
    - `MemberService.updateMember()`, `MemberService.deleteMember()`: `@Transactional` 어노테이션은 없지만, 단일 DB 작업이므로 암묵적으로 트랜잭션이 적용될 수 있습니다. 명시적으로 `@Transactional`을 추가하는 것이 좋습니다.
- **동시성 문제 및 해결 방법**:
    - **회원 생성 시 `phoneNumber`, `email` 중복**: DB의 `UNIQUE` 제약조건으로 동시성 문제를 방지합니다. 중복 시 예외가 발생합니다.
    - **회원 정보 수정 시 동시성**: 동일한 회원의 정보를 여러 사용자가 동시에 수정하려 할 때, 마지막으로 업데이트한 내용만 반영될 수 있습니다 (Last-write-wins). `updateMember`는 `memberId`를 기준으로 업데이트하므로, 특정 필드에 대한 동시성 제어는 없습니다.
        - **개선 방향**: 낙관적 락(Optimistic Lock)을 사용하여 동시성 문제를 해결할 수 있습니다. `Member` 엔티티에 `version` 필드를 추가하고 업데이트 시 버전을 체크하는 방식입니다.

## 8. 성능 최적화

- **쿼리 최적화**:
    - `MemberMapper`의 `findByMemberId`, `findByName`, `findByEmail`, `findByPhoneNumber` 등은 특정 컬럼을 기준으로 조회하므로 해당 컬럼에 인덱스가 있다면 효율적입니다.
    - `getAllMembers()`는 모든 회원을 조회하므로, 회원 수가 많아질 경우 성능 문제가 발생할 수 있습니다.
        - **개선 방향**: 페이징 처리를 도입하여 필요한 만큼의 데이터만 조회하도록 변경해야 합니다.
    - `MemberController`에서 `PointHistoryService`와 `OrderService`를 호출하여 회원별 내역을 조회할 때, 해당 서비스 내부에서 페이징 처리가 되어 있습니다.
- **인덱스 전략**:
    - `member` 테이블의 `phone_number`, `email` 컬럼에 `UNIQUE INDEX`가 필요합니다.
    - `name` 컬럼에 대한 인덱스도 조회 성능 향상에 도움이 될 수 있습니다.
- **캐싱 전략**: 현재 코드에는 명시적인 캐싱 전략이 적용되어 있지 않습니다.
    - **개선 방향**: 자주 조회되는 회원 정보(예: `getMemberById()`)에 대해 캐싱(예: Redis, Ehcache)을 적용하여 DB 부하를 줄일 수 있습니다.

## 9. 보안

- **입력 검증**:
    - `Member` DTO에 대한 `@Valid` 어노테이션을 통한 입력값 검증이 현재는 명시적으로 보이지 않습니다. (Lombok `@Getter`, `@Setter`만 사용)
    - **개선 방향**: Spring `Validation` API를 사용하여 DTO 필드에 `@NotBlank`, `@Email`, `@Pattern` 등의 어노테이션을 적용하고, `MemberController`에서 `@Valid`를 사용하여 자동 검증을 수행해야 합니다.
- **민감정보 처리**: `phoneNumber`, `email`, `shippingAddress` 등은 개인 식별 정보이므로 안전하게 관리되어야 합니다. 데이터베이스 저장 시 암호화 또는 마스킹 처리를 고려할 수 있습니다.
- **인증/권한 체크**:
    - `MemberController`의 API 엔드포인트에 대한 인증(Authentication) 및 권한(Authorization) 체크 로직이 명시적으로 보이지 않습니다. (예: `@PreAuthorize` 또는 인터셉터/필터)
    - **개선 방향**: `createMember`를 제외한 모든 API에 대해 로그인한 사용자만 접근 가능하도록 인증을 적용하고, `getMemberById`, `updateMember`, `deleteMember` 등은 해당 `memberId`의 소유자 또는 관리자만 접근할 수 있도록 권한 체크를 구현해야 합니다.

## 10. 테스트 케이스

### 10.1. 정상 시나리오 (Happy Path)
- **회원 생성**: 유효한 `Member` 정보로 회원 생성 시, DB에 회원 정보가 저장되고 `memberId`가 할당되며, 초기 0 포인트가 성공적으로 지급되는지 확인.
- **회원 조회**: `memberId`로 회원 조회 시 올바른 회원 정보가 반환되는지 확인. 모든 회원 조회 시 전체 목록이 반환되는지 확인.
- **회원 수정**: `memberId`와 유효한 `Member` 정보로 회원 수정 시, DB에 회원 정보가 업데이트되고 업데이트된 정보가 반환되는지 확인.
- **회원 삭제**: `memberId`로 회원 삭제 시, DB에서 회원 정보가 성공적으로 삭제되고 `204 No Content` 응답이 반환되는지 확인.
- **포인트/주문 내역 조회**: `memberId`로 포인트 내역 및 주문 내역 조회 시, 페이징이 적용되어 올바른 데이터가 반환되는지 확인.

### 10.2. 예외 시나리오 (각 에러 케이스)
- **회원 생성 실패**:
    - `name` 누락 시 `400 Bad Request` 응답.
    - `phoneNumber` 또는 `email`이 이미 존재하는 경우 `400 Bad Request` 응답.
    - 유효하지 않은 `phoneNumber` 또는 `email` 형식 시 `400 Bad Request` 응답.
- **회원 조회 실패**: 존재하지 않는 `memberId`로 회원 조회 시 `404 Not Found` 응답.
- **회원 수정 실패**:
    - 존재하지 않는 `memberId`로 회원 수정 시 `404 Not Found` 응답.
    - `phoneNumber` 또는 `email`이 다른 회원과 중복되는 경우 `400 Bad Request` 응답.
- **회원 삭제 실패**: 존재하지 않는 `memberId`로 회원 삭제 시 (현재 구현은 404를 반환하지 않음).

### 10.3. 경계값 테스트
- `name`, `shippingAddress`, `phoneNumber`, `email` 필드의 최대 길이 테스트.
- `memberId`가 음수 또는 0인 경우.
- 페이징 파라미터 `page`, `size`가 음수 또는 매우 큰 값인 경우.

### 10.4. 동시성 테스트
- 동일한 `phoneNumber` 또는 `email`로 여러 회원이 동시에 가입을 시도할 때 중복 방지 로직 동작 확인.
- 동일한 회원의 정보를 여러 사용자가 동시에 수정할 때 데이터 일관성 유지 여부 확인.

## 11. 알려진 이슈 및 개선 방향

### 11.1. 코드 품질 및 구조
- **Lombok 사용 일관성**: `Member` 엔티티에 `@Getter`, `@Setter` 어노테이션이 누락되어 수동으로 getter/setter가 구현되어 있습니다.
    - **개선 방향**: `Member` 엔티티에도 `@Getter`, `@Setter` 어노테이션을 적용하여 코드 간결성을 높여야 합니다.
- **`MemberController`의 에러 처리**: `updateMember` 메서드에서 `IllegalArgumentException`을 catch하여 `ResponseEntity.notFound().build()`를 반환하고 있습니다. 이는 `MemberService`에서 던지는 예외를 컨트롤러에서 직접 처리하는 방식입니다.
    - **개선 방향**: `GlobalExceptionHandler`를 통해 예외를 중앙 집중식으로 처리하고, `MemberException`과 같은 커스텀 예외를 사용하여 에러 처리를 표준화해야 합니다.
- **`deleteMember`의 응답**: `deleteMember`는 회원이 존재하지 않아도 `204 No Content`를 반환합니다. 이는 클라이언트에게 혼란을 줄 수 있습니다.
    - **개선 방향**: 삭제 전 `memberId`로 회원을 조회하여 존재하지 않으면 `404 Not Found`를 반환하도록 변경해야 합니다.



### 11.3. 리팩토링 포인트
- **`MemberController`의 의존성**: `PointHistoryService`와 `OrderService`를 직접 주입받아 사용하고 있습니다. 이는 컨트롤러가 너무 많은 책임을 지게 만들 수 있습니다.
    - **개선 방향**: `MemberService` 내부에 관련 로직을 캡슐화하거나, `MemberFacade`와 같은 파사드 패턴을 도입하여 컨트롤러의 의존성을 줄여야 합니다.
- **`Member` 엔티티의 역할**: `Member` 클래스가 엔티티 역할과 DTO 역할을 겸하고 있습니다.
    - **개선 방향**: 엔티티는 영속성 계층에만 사용하고, API 요청/응답을 위한 DTO를 별도로 정의하여 계층 간의 관심사를 분리해야 합니다.


