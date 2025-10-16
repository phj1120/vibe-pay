# Vibe Pay 개발 사양서: 3. API 설계

이 문서는 Vibe Pay 백엔드 시스템이 제공하는 REST API의 명세를 정의한다. 각 엔드포인트는 HTTP 메서드, 경로, 요청/응답 스키마, 상태 코드 시나리오로 설명된다.

## 1. 공통 사항

### 1.1. 기본 경로 (Base Path)
모든 API의 기본 경로는 `/api` 이다.

### 1.2. 데이터 형식
모든 요청과 응답의 본문(body)은 `application/json` 형식을 사용한다.

### 1.3. 에러 응답 형식
API 호출 실패 시, 모든 에러 응답은 아래와 같은 표준 형식을 따른다. 이는 `GlobalExceptionHandler`에 의해 관리된다.

- **에러 응답 (Error Response) 스키마**

```json
{
  "timestamp": "2025-10-15T12:00:00.123456Z", // 에러 발생 시각 (ISO-8601)
  "status": 400, // HTTP 상태 코드
  "error": "ORDER_CREATION_FAILED", // 에러를 식별하는 코드 문자열
  "message": "Order creation failed after payment approval: ...", // 개발자가 이해할 수 있는 상세 메시지
  "path": "/api/orders", // 요청된 경로
  "traceId": "a1b2c3d4e5f67890..." // 에러 추적을 위한 고유 ID
}
```

---

## 2. 핵심 API 엔드포인트

### 2.1. 결제 준비

-   **Endpoint**: `POST /api/payments/initiate`
-   **설명**: PG 결제창 호출에 필요한 파라미터를 생성하여 반환한다.

-   **요청 스키마** (`PaymentInitiateRequest`)

    ```json
    {
      "memberId": 1,
      "amount": 10000,
      "usedMileage": 1000,
      "pgCompany": "WEIGHTED",
      "goodName": "샘플 상품",
      "buyerName": "구매자",
      "buyerTel": "010-0000-0000",
      "buyerEmail": "test@example.com",
      "orderId": "20251015O00000001"
    }
    ```

-   **성공 응답 스키마** (`200 OK`, `PaymentInitResponse`)

    ```json
    {
        "success": true,
        "selectedPgCompany": "INICIS",
        "paymentId": "20251015P00000002",
        "inicis": {
            "mid": "INIpayTest",
            "oid": "20251015O00000001",
            "price": "9000",
            "timestamp": "1665806400000",
            "signature": "...",
            "gopaymethod": "Card",
            "returnUrl": "...",
            "closeUrl": "..."
        },
        "nicepay": null
    }
    ```

-   **상태 코드 시나리오**
    -   `200 OK`: 성공적으로 파라미터가 생성됨.
    -   `400 Bad Request`: 요청 파라미터가 유효하지 않음. (예: 필수 필드 누락)
    -   `402 Payment Required`: PG사 연동 파라미터 생성 중 오류 발생. `error` 필드에 `PAYMENT_INITIATION_FAILED` 등 상세 코드가 포함됨.

### 2.2. 주문 생성 및 결제 승인

-   **Endpoint**: `POST /api/orders`
-   **설명**: PG 인증 완료 후, 서버 간 통신으로 결제를 최종 승인하고 주문 데이터를 생성한다.

-   **요청 스키마** (`OrderRequest`)

    ```json
    {
      "orderNumber": "20251015O00000001",
      "memberId": 1,
      "items": [
        { "productId": 101, "quantity": 2 }
      ],
      "paymentMethods": [
        {
          "paymentMethod": "POINT",
          "amount": 1000
        },
        {
          "paymentMethod": "CREDIT_CARD",
          "amount": 9000,
          "pgCompany": "INICIS",
          "authToken": "..."
        }
      ],
      "netCancel": false
    }
    ```

-   **성공 응답 스키마** (`200 OK`, `List<Order>`)

    ```json
    [
      {
        "orderId": "20251015O00000001",
        "ordSeq": 1,
        "ordProcSeq": 1,
        "claimId": null,
        "memberId": 1,
        "orderDate": "2025-10-15T14:30:00",
        "totalAmount": 9000,
        "status": "ORDERED"
      }
    ]
    ```

-   **상태 코드 시나리오**
    -   `200 OK`: 주문 및 결제 처리가 모두 성공적으로 완료됨.
    -   `400 Bad Request`: PG사 최종 승인 실패, 금액 불일치 등 비즈니스 로직 오류. 에러 응답의 `error` 필드에 `PAYMENT_APPROVAL_FAILED` 등 상세 코드가 포함됨.
    -   `500 Internal Server Error`: PG사 최종 승인은 성공했으나, DB 데이터 생성 중 오류가 발생한 경우. 서버는 망취소를 시도하며, 클라이언트에게는 서버 내부 오류를 알림.

### 2.3. 주문 취소

-   **Endpoint**: `POST /api/orders/{id}/cancel`
-   **설명**: 지정된 주문(`id`)을 취소하고, 결제된 금액을 환불/복원한다.

-   **요청 스키마**: 없음 (Request Body가 비어있음)

-   **성공 응답 스키마** (`200 OK`, `Order`)

    ```json
    {
      "orderId": "20251015O00000001",
      "ordSeq": 1,
      "ordProcSeq": 2, // ord_proc_seq가 2로 증가
      "claimId": "20251015C00000003", // 클레임 ID가 채번됨
      "memberId": 1,
      "orderDate": "2025-10-15T18:00:00", // 취소 시점의 시간
      "totalAmount": -9000, // 금액이 음수로 기록됨
      "status": "CANCELLED"
    }
    ```

-   **상태 코드 시나리오**
    -   `200 OK`: 성공적으로 주문이 취소됨.
    -   `400 Bad Request`: 주문을 찾을 수 없거나, 이미 취소된 주문을 다시 취소하려 할 경우. `error` 필드에 `ORDER_NOT_FOUND`, `ALREADY_CANCELLED_ORDER` 등 상세 코드가 포함됨.

---

## 3. 보조 API 엔드포인트

### 3.1. 주문번호 채번

-   **Endpoint**: `GET /api/orders/generateOrderNumber`
-   **설명**: 시스템에서 사용할 새로운 주문번호를 미리 채번하여 반환한다.
-   **성공 응답** (`200 OK`, `text/plain`)
    ```
    20251015O00000004
    ```

### 3.2. 주문 조회

-   **Endpoint**: `GET /api/orders/{id}`
-   **설명**: 특정 `orderId`에 해당하는 모든 주문 처리 내역(원본, 취소 등)을 조회한다.
-   **성공 응답** (`200 OK`, `List<Order>`)
-   **실패 응답**: `404 Not Found` (주문이 존재하지 않을 경우)

### 3.3. 회원별 주문 목록 조회

-   **Endpoint**: `GET /api/orders/member/{memberId}`
-   **설명**: 특정 회원의 모든 주문 목록을 조회한다.
-   **성공 응답** (`200 OK`, `List<Order>`)
-   **실패 응답**: `404 Not Found` (회원이 존재하지 않거나 주문이 없을 경우, 빈 리스트를 반환하므로 404는 발생하지 않을 수 있음)
