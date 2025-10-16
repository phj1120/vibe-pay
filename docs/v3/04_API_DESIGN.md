# 4. API 설계

## 4.1. 공통 응답 형식

성공적인 응답은 HTTP 상태 코드 200 OK와 함께 요청된 데이터를 반환합니다. 오류 응답은 적절한 HTTP 상태 코드와 함께 `ErrorResponse` 객체를 반환합니다.

### 4.1.1. `ErrorResponse` 스키마
```json
{
  "timestamp": "2025-10-15T10:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error message details",
  "path": "/api/endpoint"
}
```

## 4.2. 회원 API (`/api/members`)

### 4.2.1. 회원 생성
- **HTTP 메서드**: `POST`
- **경로**: `/api/members`
- **요청 스키마**:
    ```json
    {
      "name": "홍길동",
      "shippingAddress": "서울시 강남구 테헤란로 123",
      "phoneNumber": "010-1234-5678",
      "email": "hong.gildong@example.com"
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    {
      "memberId": 1,
      "name": "홍길동",
      "shippingAddress": "서울시 강남구 테헤란로 123",
      "phoneNumber": "010-1234-5678",
      "email": "hong.gildong@example.com",
      "createdAt": "2025-10-15T10:00:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 회원 생성 성공.
    - `400 Bad Request`: 필수 필드 누락, 유효성 검증 실패 (예: 중복된 이메일/전화번호).

### 4.2.2. 모든 회원 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/members`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "memberId": 1,
        "name": "홍길동",
        "shippingAddress": "서울시 강남구 테헤란로 123",
        "phoneNumber": "010-1234-5678",
        "email": "hong.gildong@example.com",
        "createdAt": "2025-10-15T10:00:00"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 회원 목록 반환.

### 4.2.3. 특정 회원 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/members/{memberId}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    {
      "memberId": 1,
      "name": "홍길동",
      "shippingAddress": "서울시 강남구 테헤란로 123",
      "phoneNumber": "010-1234-5678",
      "email": "hong.gildong@example.com",
      "createdAt": "2025-10-15T10:00:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 회원 정보 반환.
    - `404 Not Found`: 해당 `memberId`의 회원이 존재하지 않음.

### 4.2.4. 회원 정보 수정
- **HTTP 메서드**: `PUT`
- **경로**: `/api/members/{memberId}`
- **요청 스키마**:
    ```json
    {
      "name": "홍길동 수정",
      "shippingAddress": "서울시 강남구 테헤란로 456",
      "phoneNumber": "010-9876-5432",
      "email": "hong.gildong.updated@example.com"
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    {
      "memberId": 1,
      "name": "홍길동 수정",
      "shippingAddress": "서울시 강남구 테헤란로 456",
      "phoneNumber": "010-9876-5432",
      "email": "hong.gildong.updated@example.com",
      "createdAt": "2025-10-15T10:00:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 회원 정보 수정 성공.
    - `404 Not Found`: 해당 `memberId`의 회원이 존재하지 않음.
    - `400 Bad Request`: 유효성 검증 실패 (예: 중복된 이메일/전화번호).

### 4.2.5. 회원 삭제
- **HTTP 메서드**: `DELETE`
- **경로**: `/api/members/{memberId}`
- **요청 스키마**: 없음
- **응답 스키마**: 없음
- **상태 코드별 응답 시나리오**:
    - `204 No Content`: 회원 삭제 성공.
    - `404 Not Found`: 해당 `memberId`의 회원이 존재하지 않음.

### 4.2.6. 회원별 포인트 내역 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/members/{memberId}/point-history`
- **쿼리 파라미터**:
    - `page` (int, optional, default: 0): 페이지 번호
    - `size` (int, optional, default: 10): 페이지당 항목 수
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "pointHistoryId": 101,
        "memberId": 1,
        "pointAmount": 1000,
        "balanceAfter": 1000,
        "transactionType": "EARN",
        "referenceType": "PAYMENT",
        "referenceId": "PAY12345",
        "description": "상품 구매 적립",
        "createdAt": "2025-10-15T10:05:00"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 포인트 내역 목록 반환.
    - `400 Bad Request`: `memberId`가 유효하지 않거나 기타 오류.

### 4.2.7. 회원별 주문 내역 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/members/{memberId}/order-history`
- **쿼리 파라미터**:
    - `page` (int, optional, default: 0): 페이지 번호
    - `size` (int, optional, default: 10): 페이지당 항목 수
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "orderId": "20251015O00000001",
        "ordSeq": 1,
        "ordProcSeq": 1,
        "claimId": null,
        "memberId": 1,
        "orderDate": "2025-10-15T10:10:00",
        "totalAmount": 15000.0,
        "status": "ORDERED",
        "orderItems": [
          {
            "orderItemId": 1,
            "orderId": "20251015O00000001",
            "ordSeq": 1,
            "ordProcSeq": 1,
            "productId": 10,
            "productName": "상품 A",
            "quantity": 1,
            "priceAtOrder": 15000.0
          }
        ],
        "payments": [
          {
            "paymentId": "PAY_INI_12345",
            "memberId": 1,
            "orderId": "20251015O00000001",
            "claimId": null,
            "amount": 15000,
            "paymentMethod": "CREDIT_CARD",
            "payType": "PAYMENT",
            "pgCompany": "INICIS",
            "status": "SUCCESS",
            "orderStatus": "ORDERED",
            "transactionId": "INI_TID_67890",
            "paymentDate": "2025-10-15T10:10:30"
          }
        ],
        "orderProcesses": [
          {
            "orderId": "20251015O00000001",
            "ordSeq": 1,
            "ordProcSeq": 1,
            "claimId": null,
            "memberId": 1,
            "orderDate": "2025-10-15T10:10:00",
            "totalAmount": 15000.0,
            "status": "ORDERED"
          }
        ]
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 주문 내역 목록 반환.
    - `400 Bad Request`: `memberId`가 유효하지 않거나 기타 오류.

## 4.3. 상품 API (`/api/products`)

### 4.3.1. 상품 생성
- **HTTP 메서드**: `POST`
- **경로**: `/api/products`
- **요청 스키마**:
    ```json
    {
      "name": "새로운 상품",
      "price": 25000.0
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    {
      "productId": 10,
      "name": "새로운 상품",
      "price": 25000.0
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 상품 생성 성공.
    - `400 Bad Request`: 필수 필드 누락, 유효성 검증 실패 (예: 중복된 상품명, 가격이 0 이하).

### 4.3.2. 모든 상품 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/products`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "productId": 10,
        "name": "상품 A",
        "price": 15000.0
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 상품 목록 반환.

### 4.3.3. 특정 상품 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/products/{productId}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    {
      "productId": 10,
      "name": "상품 A",
      "price": 15000.0
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 상품 정보 반환.
    - `404 Not Found`: 해당 `productId`의 상품이 존재하지 않음.

### 4.3.4. 상품 정보 수정
- **HTTP 메서드**: `PUT`
- **경로**: `/api/products/{productId}`
- **요청 스키마**:
    ```json
    {
      "name": "상품 A 수정",
      "price": 16000.0
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    {
      "productId": 10,
      "name": "상품 A 수정",
      "price": 16000.0
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 상품 정보 수정 성공.
    - `404 Not Found`: 해당 `productId`의 상품이 존재하지 않음.
    - `400 Bad Request`: 유효성 검증 실패 (예: 중복된 상품명, 가격이 0 이하).

### 4.3.5. 상품 삭제
- **HTTP 메서드**: `DELETE`
- **경로**: `/api/products/{productId}`
- **요청 스키마**: 없음
- **응답 스키마**: 없음
- **상태 코드별 응답 시나리오**:
    - `204 No Content`: 상품 삭제 성공.
    - `404 Not Found`: 해당 `productId`의 상품이 존재하지 않음.
    - `400 Bad Request`: 상품이 주문에 사용되어 삭제할 수 없는 경우.

## 4.4. 주문 API (`/api/orders`)

### 4.4.1. 주문 번호 채번
- **HTTP 메서드**: `GET`
- **경로**: `/api/orders/generateOrderNumber`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    "20251015O00000001"
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 주문 번호 반환.
    - `500 Internal Server Error`: 내부 서버 오류.

### 4.4.2. 주문 생성 및 결제 승인
- **HTTP 메서드**: `POST`
- **경로**: `/api/orders`
- **요청 스키마**:
    ```json
    {
      "orderNumber": "20251015O00000001",
      "memberId": 1,
      "items": [
        {
          "productId": 10,
          "quantity": 1
        }
      ],
      "paymentMethods": [
        {
          "authToken": "AUTH_TOKEN_FROM_PG",
          "authUrl": "https://pg.example.com/auth",
          "netCancelUrl": "https://pg.example.com/netcancel",
          "mid": "MERCHANT_ID",
          "amount": 15000,
          "paymentMethod": "CREDIT_CARD",
          "pgCompany": "INICIS",
          "txTid": "PG_TX_TID",
          "nextAppUrl": "https://your-service.com/payment/next"
        }
      ],
      "netCancel": false
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "orderId": "20251015O00000001",
        "ordSeq": 1,
        "ordProcSeq": 1,
        "claimId": null,
        "memberId": 1,
        "orderDate": "2025-10-15T10:10:00",
        "totalAmount": 15000.0,
        "status": "ORDERED"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 주문 생성 및 결제 승인 성공.
    - `400 Bad Request`: 결제 승인 실패 (예: `Payment approval failed` 메시지).
    - `500 Internal Server Error`: 결제 승인 후 주문 생성 실패 (예: `Order creation failed after payment approval` 메시지) 및 망취소 시도.

### 4.4.3. 모든 주문 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/orders`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "orderId": "20251015O00000001",
        "ordSeq": 1,
        "ordProcSeq": 1,
        "claimId": null,
        "memberId": 1,
        "orderDate": "2025-10-15T10:10:00",
        "totalAmount": 15000.0,
        "status": "ORDERED"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 주문 목록 반환.

### 4.4.4. 특정 주문 조회 (orderId 기준)
- **HTTP 메서드**: `GET`
- **경로**: `/api/orders/{id}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "orderId": "20251015O00000001",
        "ordSeq": 1,
        "ordProcSeq": 1,
        "claimId": null,
        "memberId": 1,
        "orderDate": "2025-10-15T10:10:00",
        "totalAmount": 15000.0,
        "status": "ORDERED"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 주문 정보 리스트 반환 (동일 `orderId`의 모든 `ordProcSeq` 포함).
    - `404 Not Found`: 해당 `orderId`의 주문이 존재하지 않음.

### 4.4.5. 회원별 주문 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/orders/member/{memberId}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "orderId": "20251015O00000001",
        "ordSeq": 1,
        "ordProcSeq": 1,
        "claimId": null,
        "memberId": 1,
        "orderDate": "2025-10-15T10:10:00",
        "totalAmount": 15000.0,
        "status": "ORDERED"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 회원별 주문 목록 반환.

### 4.4.6. 회원별 주문 상세 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/orders/member/{memberId}/details`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "orderId": "20251015O00000001",
        "ordSeq": 1,
        "ordProcSeq": 1,
        "claimId": null,
        "memberId": 1,
        "orderDate": "2025-10-15T10:10:00",
        "totalAmount": 15000.0,
        "status": "ORDERED",
        "orderItems": [...],
        "payments": [...],
        "orderProcesses": [...] 
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 회원별 주문 상세 목록 반환.

### 4.4.7. 주문 상세 조회 (orderId 기준)
- **HTTP 메서드**: `GET`
- **경로**: `/api/orders/details/{orderId}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "orderId": "20251015O00000001",
        "ordSeq": 1,
        "ordProcSeq": 1,
        "claimId": null,
        "memberId": 1,
        "orderDate": "2025-10-15T10:10:00",
        "totalAmount": 15000.0,
        "status": "ORDERED",
        "orderItems": [...],
        "payments": [...],
        "orderProcesses": [...] 
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 주문 상세 정보 반환.
    - `404 Not Found`: 해당 `orderId`의 주문이 존재하지 않음.
    - `500 Internal Server Error`: 내부 서버 오류.

### 4.4.8. 주문 취소
- **HTTP 메서드**: `POST`
- **경로**: `/api/orders/{id}/cancel`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    {
      "orderId": "20251015O00000001",
      "ordSeq": 1,
      "ordProcSeq": 2, // 취소된 주문의 처리 순번
      "claimId": "20251015C00000001",
      "memberId": 1,
      "orderDate": "2025-10-15T10:30:00",
      "totalAmount": -15000.0,
      "status": "CANCELLED"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 주문 취소 및 환불 성공.
    - `400 Bad Request`: 주문이 존재하지 않거나, 이미 취소된 주문이거나, 환불 실패.

## 4.5. 결제 API (`/api/payments`)

### 4.5.1. 모든 결제 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/payments`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "paymentId": "PAY_INI_12345",
        "memberId": 1,
        "orderId": "20251015O00000001",
        "claimId": null,
        "amount": 15000,
        "paymentMethod": "CREDIT_CARD",
        "payType": "PAYMENT",
        "pgCompany": "INICIS",
        "status": "SUCCESS",
        "orderStatus": "ORDERED",
        "transactionId": "INI_TID_67890",
        "paymentDate": "2025-10-15T10:10:30"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 결제 목록 반환.

### 4.5.2. 특정 결제 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/payments/{id}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    {
      "paymentId": "PAY_INI_12345",
      "memberId": 1,
      "orderId": "20251015O00000001",
      "claimId": null,
      "amount": 15000,
      "paymentMethod": "CREDIT_CARD",
      "payType": "PAYMENT",
      "pgCompany": "INICIS",
      "status": "SUCCESS",
      "orderStatus": "ORDERED",
      "transactionId": "INI_TID_67890",
      "paymentDate": "2025-10-15T10:10:30"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 결제 정보 반환.
    - `404 Not Found`: 해당 `id`의 결제가 존재하지 않음.

### 4.5.3. 결제 정보 수정
- **HTTP 메서드**: `PUT`
- **경로**: `/api/payments/{id}`
- **요청 스키마**:
    ```json
    {
      "status": "CANCELLED",
      "claimId": "20251015C00000001"
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    {
      "paymentId": "PAY_INI_12345",
      "memberId": 1,
      "orderId": "20251015O00000001",
      "claimId": "20251015C00000001",
      "amount": 15000,
      "paymentMethod": "CREDIT_CARD",
      "payType": "PAYMENT",
      "pgCompany": "INICIS",
      "status": "CANCELLED",
      "orderStatus": "ORDERED",
      "transactionId": "INI_TID_67890",
      "paymentDate": "2025-10-15T10:10:30"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 결제 정보 수정 성공.
    - `404 Not Found`: 해당 `id`의 결제가 존재하지 않음.

### 4.5.4. 결제 삭제
- **HTTP 메서드**: `DELETE`
- **경로**: `/api/payments/{id}`
- **요청 스키마**: 없음
- **응답 스키마**: 없음
- **상태 코드별 응답 시나리오**:
    - `204 No Content`: 결제 삭제 성공.
    - `404 Not Found`: 해당 `id`의 결제가 존재하지 않음.

### 4.5.5. 결제 시작
- **HTTP 메서드**: `POST`
- **경로**: `/api/payments/initiate`
- **요청 스키마**:
    ```json
    {
      "memberId": 1,
      "amount": 15000,
      "paymentMethod": "CREDIT_CARD",
      "pgCompany": "INICIS",
      "goodName": "상품 A",
      "buyerName": "홍길동",
      "buyerTel": "010-1234-5678",
      "buyerEmail": "hong.gildong@example.com",
      "orderId": "20251015O00000001"
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    {
      "success": true,
      "paymentUrl": "https://pg.example.com/payment/init",
      "paymentParams": "<form action=\"https://pg.example.com/payment/init\" method=\"post\">...",
      "errorMessage": null,
      "paymentId": "PAY_INI_INIT_123",
      "mid": "MERCHANT_ID",
      "oid": "20251015O00000001",
      "price": 15000,
      "goodName": "상품 A",
      "moId": "MERCHANT_ID",
      "orderId": "20251015O00000001",
      "amt": 15000,
      "productName": "상품 A",
      "buyerName": "홍길동",
      "buyerTel": "010-1234-5678",
      "buyerEmail": "hong.gildong@example.com",
      "timestamp": "20251015101000",
      "mKey": "...",
      "signature": "...",
      "verification": "...",
      "returnUrl": "https://your-service.com/api/payments/return",
      "closeUrl": "https://your-service.com/payment/close",
      "version": "1.0",
      "currency": "KRW",
      "gopaymethod": "Card",
      "acceptmethod": "HPP(2):no_receipt",
      "ediDate": "20251015101000",
      "SignData": "...",
      "selectedPgCompany": "INICIS"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 결제 초기화 성공.
    - `400 Bad Request`: 필수 필드 누락, 유효성 검증 실패, PG사 초기화 실패.

### 4.5.6. 결제 결과 처리 (PG사 리턴 URL)
- **HTTP 메서드**: `POST`
- **경로**: `/api/payments/return`
- **요청 스키마**: PG사로부터 전달되는 폼 데이터 (예: `resultCode`, `authToken`, `authUrl`, `netCancelUrl`, `mid`, `oid`, `price`, `resultMsg` 등)
- **응답 스키마 (성공/실패)**:
    ```json
    {
      "success": true,
      "message": "결제가 성공적으로 완료되었습니다.",
      "resultCode": "0000",
      "payment": {
        "paymentId": "PAY_INI_12345",
        "memberId": 1,
        "orderId": "20251015O00000001",
        "claimId": null,
        "amount": 15000,
        "paymentMethod": "CREDIT_CARD",
        "payType": "PAYMENT",
        "pgCompany": "INICIS",
        "status": "SUCCESS",
        "orderStatus": "ORDERED",
        "transactionId": "INI_TID_67890",
        "paymentDate": "2025-10-15T10:10:30"
      }
    }
    ```
    ```json
    {
      "success": false,
      "message": "결제가 실패했습니다.",
      "resultCode": "XXXX",
      "payment": null
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 결제 처리 성공 또는 실패 (PG사 규약에 따라 항상 200 OK).
    - `400 Bad Request`: 필수 파라미터 누락, 잘못된 결제 금액 등 요청 데이터 문제.
    - `500 Internal Server Error`: 결제 처리 중 예상치 못한 서버 오류.

## 4.6. 포인트 내역 API (`/api/point-history`)

### 4.6.1. 회원별 포인트 내역 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/point-history/member/{memberId}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "pointHistoryId": 101,
        "memberId": 1,
        "pointAmount": 1000,
        "balanceAfter": 1000,
        "transactionType": "EARN",
        "referenceType": "PAYMENT",
        "referenceId": "PAY12345",
        "description": "상품 구매 적립",
        "createdAt": "2025-10-15T10:05:00"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 포인트 내역 목록 반환.
    - `500 Internal Server Error`: 내부 서버 오류.

### 4.6.2. 회원별 포인트 통계 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/point-history/member/{memberId}/statistics`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    {
      "memberId": 1,
      "currentBalance": 5000.0,
      "totalEarned": 10000.0,
      "totalUsed": 5000.0,
      "totalRefunded": 0.0,
      "earnCount": 2,
      "useCount": 1,
      "refundCount": 0,
      "totalTransactions": 3
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 포인트 통계 정보 반환.
    - `500 Internal Server Error`: 내부 서버 오류.

### 4.6.3. 회원별 특정 거래 타입 포인트 내역 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/point-history/member/{memberId}/type/{transactionType}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "pointHistoryId": 101,
        "memberId": 1,
        "pointAmount": 1000,
        "balanceAfter": 1000,
        "transactionType": "EARN",
        "referenceType": "PAYMENT",
        "referenceId": "PAY12345",
        "description": "상품 구매 적립",
        "createdAt": "2025-10-15T10:05:00"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 특정 거래 타입 포인트 내역 목록 반환.
    - `500 Internal Server Error`: 내부 서버 오류.

### 4.6.4. 특정 거래 관련 포인트 내역 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/point-history/reference/{referenceType}/{referenceId}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "pointHistoryId": 101,
        "memberId": 1,
        "pointAmount": 1000,
        "balanceAfter": 1000,
        "transactionType": "EARN",
        "referenceType": "PAYMENT",
        "referenceId": "PAY12345",
        "description": "상품 구매 적립",
        "createdAt": "2025-10-15T10:05:00"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 특정 거래 관련 포인트 내역 목록 반환.
    - `500 Internal Server Error`: 내부 서버 오류.

### 4.6.5. 전체 포인트 내역 조회 (관리자용)
- **HTTP 메서드**: `GET`
- **경로**: `/api/point-history/all`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "pointHistoryId": 101,
        "memberId": 1,
        "pointAmount": 1000,
        "balanceAfter": 1000,
        "transactionType": "EARN",
        "referenceType": "PAYMENT",
        "referenceId": "PAY12345",
        "description": "상품 구매 적립",
        "createdAt": "2025-10-15T10:05:00"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 전체 포인트 내역 목록 반환.
    - `500 Internal Server Error`: 내부 서버 오류.

### 4.6.6. 특정 포인트 내역 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/point-history/{pointHistoryId}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    {
      "pointHistoryId": 101,
      "memberId": 1,
      "pointAmount": 1000,
      "balanceAfter": 1000,
      "transactionType": "EARN",
      "referenceType": "PAYMENT",
      "referenceId": "PAY12345",
      "description": "상품 구매 적립",
      "createdAt": "2025-10-15T10:05:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 포인트 내역 반환.
    - `404 Not Found`: 해당 `pointHistoryId`의 내역이 존재하지 않음.

## 4.7. 리워드 포인트 API (`/api/rewardpoints`)

### 4.7.1. 리워드 포인트 생성
- **HTTP 메서드**: `POST`
- **경로**: `/api/rewardpoints`
- **요청 스키마**:
    ```json
    {
      "memberId": 1,
      "points": 0
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    {
      "rewardPointsId": 1,
      "memberId": 1,
      "points": 0,
      "lastUpdated": "2025-10-15T10:00:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 리워드 포인트 생성 성공.
    - `400 Bad Request`: 필수 필드 누락, 유효성 검증 실패.

### 4.7.2. 특정 리워드 포인트 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/rewardpoints/{rewardPointsId}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    {
      "rewardPointsId": 1,
      "memberId": 1,
      "points": 1000,
      "lastUpdated": "2025-10-15T10:05:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 리워드 포인트 정보 반환.
    - `404 Not Found`: 해당 `rewardPointsId`의 리워드 포인트가 존재하지 않음.

### 4.7.3. 회원별 리워드 포인트 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/rewardpoints/member/{memberId}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    {
      "rewardPointsId": 1,
      "memberId": 1,
      "points": 1000,
      "lastUpdated": "2025-10-15T10:05:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 회원별 리워드 포인트 정보 반환.
    - `404 Not Found`: 해당 `memberId`의 리워드 포인트가 존재하지 않음.

### 4.7.4. 리워드 포인트 추가
- **HTTP 메서드**: `PUT`
- **경로**: `/api/rewardpoints/add`
- **요청 스키마**:
    ```json
    {
      "memberId": 1,
      "points": 500
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    {
      "rewardPointsId": 1,
      "memberId": 1,
      "points": 1500,
      "lastUpdated": "2025-10-15T10:06:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 포인트 추가 성공.
    - `400 Bad Request`: `memberId`가 유효하지 않거나, `points`가 0 이하인 경우.

### 4.7.5. 리워드 포인트 사용
- **HTTP 메서드**: `PUT`
- **경로**: `/api/rewardpoints/use`
- **요청 스키마**:
    ```json
    {
      "memberId": 1,
      "points": 300
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    {
      "rewardPointsId": 1,
      "memberId": 1,
      "points": 1200,
      "lastUpdated": "2025-10-15T10:07:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 포인트 사용 성공.
    - `400 Bad Request`: `memberId`가 유효하지 않거나, `points`가 0 이하인 경우, 포인트 잔액 부족.

## 4.8. 결제 인터페이스 로그 API (`/api/paymentlogs`)

### 4.8.1. 결제 인터페이스 로그 생성
- **HTTP 메서드**: `POST`
- **경로**: `/api/paymentlogs`
- **요청 스키마**:
    ```json
    {
      "paymentId": "PAY_INI_12345",
      "requestType": "INIT",
      "requestPayload": "{\"param1\":\"value1\"}",
      "responsePayload": "{\"resultCode\":\"0000\"}"
    }
    ```
- **응답 스키마 (성공)**:
    ```json
    {
      "logId": 1,
      "paymentId": "PAY_INI_12345",
      "requestType": "INIT",
      "requestPayload": "{\"param1\":\"value1\"}",
      "responsePayload": "{\"resultCode\":\"0000\"}",
      "timestamp": "2025-10-15T10:00:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 로그 생성 성공.
    - `400 Bad Request`: 필수 필드 누락, 유효성 검증 실패.

### 4.8.2. 모든 결제 인터페이스 로그 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/paymentlogs`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    [
      {
        "logId": 1,
        "paymentId": "PAY_INI_12345",
        "requestType": "INIT",
        "requestPayload": "{\"param1\":\"value1\"}",
        "responsePayload": "{\"resultCode\":\"0000\"}",
        "timestamp": "2025-10-15T10:00:00"
      }
    ]
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 로그 목록 반환.

### 4.8.3. 특정 결제 인터페이스 로그 조회
- **HTTP 메서드**: `GET`
- **경로**: `/api/paymentlogs/{id}`
- **요청 스키마**: 없음
- **응답 스키마 (성공)**:
    ```json
    {
      "logId": 1,
      "paymentId": "PAY_INI_12345",
      "requestType": "INIT",
      "requestPayload": "{\"param1\":\"value1\"}",
      "responsePayload": "{\"resultCode\":\"0000\"}",
      "timestamp": "2025-10-15T10:00:00"
    }
    ```
- **상태 코드별 응답 시나리오**:
    - `200 OK`: 로그 정보 반환.
    - `404 Not Found`: 해당 `id`의 로그가 존재하지 않음.
