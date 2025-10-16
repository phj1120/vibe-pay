# Payment Log Technical Specification

## 1. 개요
- **목적**: 본 문서는 VibePay 시스템의 결제 인터페이스 요청 로그 관리 기능에 대한 기술 사양을 정의합니다. 이 기능은 외부 PG사(Payment Gateway)와의 통신 내역(요청 및 응답 전문)을 기록하여 결제 관련 문제 발생 시 원인 분석 및 감사(Audit)에 활용됩니다.
- **System Design에서의 위치**: VibePay 백엔드 서비스의 보조 모듈로, 결제(Payment) 모듈에서 PG사와의 연동 시 호출되어 통신 내역을 기록합니다.
- **관련 컴포넌트 및 의존성**:
    - `PaymentInterfaceRequestLogController`: 결제 로그 관련 API 엔드포인트 제공
    - `PaymentInterfaceRequestLogService`: 결제 로그 비즈니스 로직 처리
    - `PaymentInterfaceRequestLogMapper`: 결제 로그 데이터베이스 CRUD
    - `PaymentInterfaceRequestLog`: 결제 로그 엔티티

## 2. 프로세스 흐름

### 2.1. 결제 인터페이스 요청 로그 기록 (createLog)
```mermaid
sequenceDiagram
    participant CallerService
    participant PaymentInterfaceRequestLogService
    participant PaymentInterfaceRequestLogMapper
    database Database

    CallerService->>PaymentInterfaceRequestLogService: createLog(log)
    PaymentInterfaceRequestLogService->>PaymentInterfaceRequestLog: setTimestamp(LocalDateTime.now())
    PaymentInterfaceRequestLogService->>PaymentInterfaceRequestLogMapper: insert(log)
    PaymentInterfaceRequestLogMapper-->>Database: INSERT PaymentInterfaceRequestLog
    Database-->>PaymentInterfaceRequestLogMapper: (logId generated)
    PaymentInterfaceRequestLogMapper-->>PaymentInterfaceRequestLogService: (log with logId)
    PaymentInterfaceRequestLogService-->>CallerService: PaymentInterfaceRequestLog (created)
```

**단계별 상세 설명:**
1.  **CallerService -> PaymentInterfaceRequestLogService**: `PaymentService` 또는 `PaymentGatewayAdapter` 등 PG사와의 통신이 발생하는 서비스에서 `PaymentInterfaceRequestLogService.createLog()` 메서드를 호출하여 통신 내역을 기록합니다.
2.  **PaymentInterfaceRequestLogService -> PaymentInterfaceRequestLog**: `PaymentInterfaceRequestLogService`는 `PaymentInterfaceRequestLog` 객체의 `timestamp` 필드가 `null`인 경우 현재 시간으로 설정합니다.
3.  **PaymentInterfaceRequestLogService -> PaymentInterfaceRequestLogMapper**: `PaymentInterfaceRequestLogService`는 `PaymentInterfaceRequestLogMapper.insert()`를 호출하여 로그 정보를 데이터베이스에 저장합니다. 이 과정에서 `logId`가 자동 생성됩니다.
4.  **PaymentInterfaceRequestLogService -> CallerService**: 생성된 `PaymentInterfaceRequestLog` 객체를 호출 서비스로 반환합니다.

### 2.2. 결제 인터페이스 요청 로그 조회 (getAllLogs, getLogById)
```mermaid
sequenceDiagram
    participant Client
    participant PaymentInterfaceRequestLogController
    participant PaymentInterfaceRequestLogService
    participant PaymentInterfaceRequestLogMapper
    database Database

    Client->>PaymentInterfaceRequestLogController: GET /api/paymentlogs/{id}
    PaymentInterfaceRequestLogController->>PaymentInterfaceRequestLogService: getLogById(id)
    PaymentInterfaceRequestLogService->>PaymentInterfaceRequestLogMapper: findByLogId(id)
    PaymentInterfaceRequestLogMapper-->>Database: SELECT PaymentInterfaceRequestLog
    Database-->>PaymentInterfaceRequestLogMapper: PaymentInterfaceRequestLog (or null)
    PaymentInterfaceRequestLogMapper-->>PaymentInterfaceRequestLogService: Optional<PaymentInterfaceRequestLog>
    PaymentInterfaceRequestLogService-->>PaymentInterfaceRequestLogController: Optional<PaymentInterfaceRequestLog>
    alt Log Found
        PaymentInterfaceRequestLogController-->>Client: 200 OK (PaymentInterfaceRequestLog)
    else Log Not Found
        PaymentInterfaceRequestLogController-->>Client: 404 Not Found
    end

    Client->>PaymentInterfaceRequestLogController: GET /api/paymentlogs
    PaymentInterfaceRequestLogController->>PaymentInterfaceRequestLogService: getAllLogs()
    PaymentInterfaceRequestLogService->>PaymentInterfaceRequestLogMapper: findAll()
    PaymentInterfaceRequestLogMapper-->>Database: SELECT all PaymentInterfaceRequestLog
    Database-->>PaymentInterfaceRequestLogMapper: List<PaymentInterfaceRequestLog>
    PaymentInterfaceRequestLogMapper-->>PaymentInterfaceRequestLogService: List<PaymentInterfaceRequestLog>
    PaymentInterfaceRequestLogService-->>PaymentInterfaceRequestLogController: List<PaymentInterfaceRequestLog>
    PaymentInterfaceRequestLogController-->>Client: 200 OK (List<PaymentInterfaceRequestLog>)
```

**단계별 상세 설명:**
1.  **Client -> PaymentInterfaceRequestLogController**: 특정 로그 조회를 위해 `/api/paymentlogs/{id}`로 GET 요청을 보내거나, 모든 로그 조회를 위해 `/api/paymentlogs`로 GET 요청을 보냅니다.
2.  **PaymentInterfaceRequestLogController -> PaymentInterfaceRequestLogService**: `PaymentInterfaceRequestLogController`는 `PaymentInterfaceRequestLogService.getLogById()` 또는 `PaymentInterfaceRequestLogService.getAllLogs()`를 호출합니다.
3.  **PaymentInterfaceRequestLogService -> PaymentInterfaceRequestLogMapper**: `PaymentInterfaceRequestLogService`는 `PaymentInterfaceRequestLogMapper.findByLogId()` 또는 `PaymentInterfaceRequestLogMapper.findAll()`를 호출하여 데이터베이스에서 로그 정보를 조회합니다.
4.  **PaymentInterfaceRequestLogService -> PaymentInterfaceRequestLogController**: 조회된 `Optional<PaymentInterfaceRequestLog>` 또는 `List<PaymentInterfaceRequestLog>`를 `PaymentInterfaceRequestLogController`로 반환합니다.
5.  **PaymentInterfaceRequestLogController -> Client**: 조회 결과에 따라 200 OK (로그 정보) 또는 404 Not Found (로그 없음) 응답을 클라이언트에게 반환합니다.

## 3. 데이터 구조

### 3.1. 데이터베이스
`schema.sql` 또는 `pay.sql` 파일이 없으므로, `PaymentInterfaceRequestLog` 엔티티와 `PaymentInterfaceRequestLogMapper.xml`을 기반으로 스키마를 유추합니다.

**`PaymentInterfaceRequestLog` 테이블 (유추)**
```sql
CREATE TABLE payment_interface_request_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id VARCHAR(255),
    request_type VARCHAR(100) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    timestamp DATETIME NOT NULL
);
```

**각 필드의 타입, 제약조건, 기본값, 인덱스:**
- `log_id`: `BIGINT`, `AUTO_INCREMENT`, `PRIMARY KEY`, 로그 고유 ID.
- `payment_id`: `VARCHAR(255)`, 연관된 결제 ID.
- `request_type`: `VARCHAR(100)`, `NOT NULL`, 요청 타입 (예: `INICIS_API_CALL`, `NICEPAY_CANCEL`, `NET_CANCEL_REQUEST`).
- `request_payload`: `TEXT`, 요청 전문 (JSON 또는 폼 데이터).
- `response_payload`: `TEXT`, 응답 전문 (JSON 또는 폼 데이터).
- `timestamp`: `DATETIME`, `NOT NULL`, 로그 기록 일시.
- `INDEX`: `payment_id`, `request_type`, `timestamp` 컬럼에 대한 인덱스.

### 3.2. DTO/API 모델

#### `PaymentInterfaceRequestLog` (Entity/Request/Response DTO)
```java
package com.vibe.pay.backend.paymentlog;

import java.time.LocalDateTime;

// ... (getter/setter 생략)
public class PaymentInterfaceRequestLog {
    private Long logId;
    private String paymentId;
    private String requestType;
    private String requestPayload;
    private String responsePayload;
    private LocalDateTime timestamp;
}
```
- **검증 규칙 (생성 시)**:
    - `requestType`: `NOT NULL`, `String` 타입, 비어있지 않아야 함.
    - `requestPayload`: `NOT NULL`, `String` 타입, 비어있지 않아야 함.

## 4. API 명세

### 4.1. 결제 로그 생성
- **Endpoint**: `/api/paymentlogs`
- **HTTP Method**: `POST`
- **인증 요구사항**: 필요 (내부 시스템 호출 또는 관리자 권한)
- **Request 예시 (JSON)**:
    ```json
    {
        "paymentId": "20251016P00000001",
        "requestType": "INICIS_API_CALL",
        "requestPayload": "{\"mid\":\"INIpayTest\",\"authToken\":\"...\"}",
        "responsePayload": "{\"resultCode\":\"0000\",\"tid\":\"...\"}"
    }
    ```
- **검증 규칙**: `PaymentInterfaceRequestLog` DTO 참조.
- **Success Response (예시)**:
    ```json
    {
        "logId": 1,
        "paymentId": "20251016P00000001",
        "requestType": "INICIS_API_CALL",
        "requestPayload": "{\"mid\":\"INIpayTest\",\"authToken\":\"...\"}",
        "responsePayload": "{\"resultCode\":\"0000\",\"tid\":\"...\"}",
        "timestamp": "2025-10-16T12:00:00"
    }
    ```
- **Error Response**:
    - **HTTP Status**: `400 Bad Request`
    - **메시지**: (내부 서버 오류 또는 유효성 검증 실패 메시지)
    - **상황**: 필수 파라미터 누락, 유효하지 않은 형식.

### 4.2. 모든 결제 로그 조회
- **Endpoint**: `/api/paymentlogs`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (관리자 권한)
- **Request 예시**: 없음
- **검증 규칙**: 없음
- **Success Response (예시)**:
    ```json
    [
        {
            "logId": 1,
            "paymentId": "20251016P00000001",
            "requestType": "INICIS_API_CALL",
            "requestPayload": "{\"mid\":\"INIpayTest\",\"authToken\":\"...\"}",
            "responsePayload": "{\"resultCode\":\"0000\",\"tid\":\"...\"}",
            "timestamp": "2025-10-16T12:00:00"
        }
        // ...
    ]
    ```

### 4.3. 특정 결제 로그 조회
- **Endpoint**: `/api/paymentlogs/{id}`
- **HTTP Method**: `GET`
- **인증 요구사항**: 필요 (관리자 권한)
- **Request 예시**: 없음 (Path Variable `id` 사용)
- **검증 규칙**: `id`는 `Long` 타입의 양수여야 함.
- **Success Response (예시)**:
    ```json
    {
        "logId": 1,
        "paymentId": "20251016P00000001",
        "requestType": "INICIS_API_CALL",
        "requestPayload": "{\"mid\":\"INIpayTest\",\"authToken\":\"...\"}",
        "responsePayload": "{\"resultCode\":\"0000\",\"tid\":\"...\"}",
        "timestamp": "2025-10-16T12:00:00"
    }
    ```
- **Error Response**:
    - **HTTP Status**: `404 Not Found`
    - **메시지**: (응답 본문 없음)
    - **상황**: `id`에 해당하는 로그가 없을 때.

## 5. 비즈니스 로직 상세

### 5.1. 결제 인터페이스 요청 로그 기록 (`PaymentInterfaceRequestLogService.createLog`)
- **목적**: 외부 PG사와의 통신 내역(요청 및 응답 전문)을 데이터베이스에 기록합니다.
- **입력 파라미터**: `PaymentInterfaceRequestLog log`
- **계산 로직**:
    1.  `log.setTimestamp(LocalDateTime.now())`를 호출하여 `timestamp` 필드를 현재 시간으로 설정합니다.
    2.  `paymentInterfaceRequestLogMapper.insert(log)`를 호출하여 로그 정보를 DB에 저장합니다. 이 과정에서 `logId`가 자동 생성되어 `log` 객체에 설정됩니다.
- **제약조건**:
    - `requestType`, `requestPayload`는 필수.
- **에러 케이스**:
    - DB 저장 실패 시 예외 발생.

## 6. 에러 처리

- **에러 코드 체계**: 현재 `PaymentInterfaceRequestLogService`에서는 명시적인 커스텀 예외를 사용하지 않고 있습니다. `PaymentInterfaceRequestLogController`에서는 `ResponseEntity.notFound().build()` 또는 `ResponseEntity.internalServerError().build()`를 반환하고 있습니다.
    - **개선 방향**: `PaymentLogException`과 같은 커스텀 예외를 정의하고, 구체적인 에러 코드를 포함하여 에러 처리를 표준화해야 합니다.
- **각 에러별 HTTP Status, 처리 방법, 사용자 메시지**:
    - **로그 생성 실패**:
        - HTTP Status: `400 Bad Request` 또는 `500 Internal Server Error`
        - 처리 방법: 호출 서비스에서 예외 처리, 로그 기록.
        - 사용자 메시지: "결제 로그 기록 중 오류가 발생했습니다."
    - **로그 조회 실패 (로그 없음)**:
        - HTTP Status: `404 Not Found`
        - 처리 방법: 클라이언트에게 오류 메시지 반환 (또는 응답 본문 없음), 로그 기록.
        - 사용자 메시지: "결제 로그를 찾을 수 없습니다."

## 7. 트랜잭션 및 동시성

- **트랜잭션 경계**:
    - `PaymentInterfaceRequestLogService`의 모든 메서드에는 `@Transactional` 어노테이션이 명시적으로 적용되어 있지 않습니다. 하지만 `createLog`는 `PaymentService` 또는 `PaymentGatewayAdapter`의 트랜잭션 내에서 호출될 수 있습니다.
- **동시성 문제 및 해결 방법**:
    - **로그 기록 동시성**: 여러 결제 요청이 동시에 발생하여 로그 기록 요청이 동시에 들어올 수 있습니다. `AUTO_INCREMENT` `logId`를 사용하므로 DB 레벨에서 동시성 문제가 처리됩니다.

## 8. 성능 최적화

- **쿼리 최적화**:
    - `PaymentInterfaceRequestLogMapper.findAll()`는 모든 로그를 조회하므로, 로그 데이터가 많아질 경우 성능 문제가 발생할 수 있습니다.
        - **개선 방향**: 페이징 처리를 도입하여 필요한 만큼의 데이터만 조회하도록 변경해야 합니다.
    - `request_payload`와 `response_payload`가 `TEXT` 타입으로 저장되므로, 대량의 데이터 조회 시 I/O 부하가 커질 수 있습니다.
- **인덱스 전략**:
    - `payment_interface_request_log` 테이블의 `payment_id`, `request_type`, `timestamp` 컬럼에 대한 인덱스 추가를 고려하여 조회 성능을 향상시킬 수 있습니다.
- **캐싱 전략**: 로그 데이터는 실시간으로 발생하고 조회 빈도가 높지 않으므로 캐싱은 적합하지 않습니다.

## 9. 보안

- **입력 검증**:
    - API 엔드포인트의 Path Variable (`id`)에 대한 유효성 검증이 필요합니다.
    - **개선 방향**: Spring `Validation` API를 사용하여 Path Variable에 대한 `@Min` 등의 어노테이션을 적용하고, `PaymentInterfaceRequestLogController`에서 `@Validated`를 사용하여 자동 검증을 수행해야 합니다.
- **민감정보 처리**: `request_payload` 및 `response_payload`에 카드 번호, 개인 식별 정보 등 민감한 정보가 포함될 수 있습니다. 현재는 전문을 그대로 저장하고 있습니다.
    - **개선 방향**: 민감 정보가 로그에 기록되지 않도록 마스킹 처리하거나, 암호화하여 저장해야 합니다. 특히 운영 환경에서는 민감 정보가 평문으로 로그에 남지 않도록 강력한 정책이 필요합니다.
- **인증/권한 체크**:
    - `PaymentInterfaceRequestLogController`의 모든 API는 관리자 권한으로 제한되어야 합니다.
    - **개선 방향**: `@PreAuthorize` 또는 인터셉터/필터를 사용하여 관리자 권한을 가진 사용자만 해당 API에 접근할 수 있도록 구현해야 합니다.

## 10. 테스트 케이스

### 10.1. 정상 시나리오 (Happy Path)
- **로그 기록**: 유효한 `PaymentInterfaceRequestLog` 정보로 로그 기록 시, DB에 로그 정보가 저장되고 `logId`가 할당되는지 확인.
- **로그 조회**: `logId`로 로그 조회 시 올바른 로그 정보가 반환되는지 확인. 모든 로그 조회 시 전체 목록이 반환되는지 확인.

### 10.2. 예외 시나리오 (각 에러 케이스)
- **로그 생성 실패**:
    - `requestType` 또는 `requestPayload` 누락 시 `400 Bad Request` 응답.
- **로그 조회 실패**: 존재하지 않는 `logId`로 로그 조회 시 `404 Not Found` 응답.

### 10.3. 경계값 테스트
- `request_payload` 및 `response_payload`의 길이가 매우 긴 경우.
- `logId`가 음수 또는 0인 경우.

### 10.4. 동시성 테스트
- 여러 결제 요청이 동시에 발생하여 로그 기록 요청이 동시에 들어올 때 데이터 일관성 유지 여부 확인.

## 11. 알려진 이슈 및 개선 방향

### 11.1. 코드 품질 및 구조
- **`PaymentInterfaceRequestLog` 엔티티의 역할**: `PaymentInterfaceRequestLog` 클래스가 엔티티 역할과 DTO 역할을 겸하고 있습니다.
    - **개선 방향**: 엔티티는 영속성 계층에만 사용하고, API 요청/응답을 위한 DTO를 별도로 정의하여 계층 간의 관심사를 분리해야 합니다.
- **`PaymentInterfaceRequestLogService`의 `@Transactional` 누락**: 모든 DB 작업 메서드에 `@Transactional` 어노테이션을 명시적으로 추가하여 트랜잭션 관리를 명확히 해야 합니다.

### 11.2. 기능적 개선
- **로그 보관 정책**: 로그 데이터는 시간이 지남에 따라 매우 커질 수 있습니다. 오래된 로그를 자동으로 삭제하거나 아카이빙하는 정책이 필요합니다.
    - **개선 방향**: 일정 기간이 지난 로그를 삭제하는 배치 작업 또는 데이터베이스 파티셔닝 전략을 도입해야 합니다.
- **로그 검색 및 필터링 기능**: 현재는 `logId` 또는 `paymentId`로만 조회 가능합니다.
    - **개선 방향**: 기간별, `requestType`별, `paymentId`별 등 다양한 조건으로 로그를 검색하고 필터링하는 기능을 제공해야 합니다.
- **로그 중요도/레벨**: 모든 로그를 동일하게 처리하고 있습니다.
    - **개선 방향**: `logLevel` 필드를 추가하여 로그의 중요도 (예: `INFO`, `WARN`, `ERROR`)를 구분하고, 중요도에 따라 보관 기간이나 알림 정책을 다르게 가져갈 수 있습니다.

### 11.3. 리팩토링 포인트
- **`PaymentInterfaceRequestLogController`의 에러 처리**: `getLogById` 메서드에서 `Optional`을 사용하여 `ResponseEntity.notFound().build()`를 반환하고 있습니다. `createLog`에서는 `RuntimeException` 발생 시 `500 Internal Server Error`를 반환할 수 있습니다.
    - **개선 방향**: `GlobalExceptionHandler`를 통해 예외를 중앙 집중식으로 처리하고, `PaymentLogException`과 같은 커스텀 예외를 사용하여 에러 처리를 표준화해야 합니다.

### 11.4. 재구현 시 개선 제안
- **로그 시스템 분리**: 결제 로그와 같은 대량의 데이터를 처리하기 위해 별도의 로그 시스템(예: ELK Stack, Splunk)을 구축하고, 비동기적으로 로그를 전송하도록 변경합니다. 이는 메인 애플리케이션의 성능에 영향을 주지 않으면서 로그를 효율적으로 관리할 수 있게 합니다.
- **데이터베이스 최적화**: `TEXT` 타입의 `request_payload`와 `response_payload`는 데이터베이스 성능에 영향을 줄 수 있습니다. 대량의 로그를 저장하기 위해 NoSQL 데이터베이스(예: MongoDB)를 고려하거나, RDBMS에서는 JSONB 타입 등을 활용하여 저장 효율을 높일 수 있습니다.
