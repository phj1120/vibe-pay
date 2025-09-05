# VibePay API 명세서

## 기본 정보

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **인코딩**: UTF-8

## 공통 응답 형식

### 성공 응답
```json
{
  "data": { ... },
  "message": "Success"
}
```

### 에러 응답
```json
{
  "error": "Error message",
  "code": "ERROR_CODE"
}
```

## 회원 관리 API

### 1. 회원 목록 조회
- **GET** `/members`
- **설명**: 모든 회원 목록 조회
- **응답**: `Member[]`

### 2. 회원 상세 조회
- **GET** `/members/{id}`
- **설명**: 특정 회원 정보 조회
- **응답**: `Member`

### 3. 회원 생성
- **POST** `/members`
- **설명**: 새 회원 등록
- **요청**: `Member`
- **응답**: `Member`

### 4. 회원 수정
- **PUT** `/members/{id}`
- **설명**: 회원 정보 수정
- **요청**: `Member`
- **응답**: `Member`

### 5. 회원 삭제
- **DELETE** `/members/{id}`
- **설명**: 회원 삭제
- **응답**: `204 No Content`

## 상품 관리 API

### 1. 상품 목록 조회
- **GET** `/products`
- **설명**: 모든 상품 목록 조회
- **응답**: `Product[]`

### 2. 상품 상세 조회
- **GET** `/products/{id}`
- **설명**: 특정 상품 정보 조회
- **응답**: `Product`

### 3. 상품 생성
- **POST** `/products`
- **설명**: 새 상품 등록
- **요청**: `Product`
- **응답**: `Product`

### 4. 상품 수정
- **PUT** `/products/{id}`
- **설명**: 상품 정보 수정
- **요청**: `Product`
- **응답**: `Product`

### 5. 상품 삭제
- **DELETE** `/products/{id}`
- **설명**: 상품 삭제
- **응답**: `204 No Content`

## 주문 관리 API

### 1. 주문 생성
- **POST** `/orders`
- **설명**: 새 주문 생성
- **요청**: `OrderRequest`
- **응답**: `Order`

### 2. 주문 목록 조회
- **GET** `/orders`
- **설명**: 모든 주문 목록 조회
- **응답**: `Order[]`

### 3. 주문 상세 조회
- **GET** `/orders/{id}`
- **설명**: 특정 주문 정보 조회
- **응답**: `Order`

### 4. 회원별 주문 조회
- **GET** `/orders/member/{memberId}`
- **설명**: 특정 회원의 주문 목록 조회
- **응답**: `Order[]`

### 5. 주문 취소
- **POST** `/orders/{id}/cancel`
- **설명**: 주문 취소 (PG 승인취소 포함)
- **응답**: `Order`

## 결제 관리 API

### 1. 결제 시작
- **POST** `/payments/initiate`
- **설명**: 결제 프로세스 시작 (PG사 파라미터 생성)
- **요청**: `PaymentInitiateRequest`
- **응답**: `InicisPaymentParameters`

### 2. 결제 승인
- **POST** `/payments/confirm`
- **설명**: PG사 결제 승인 처리
- **요청**: `PaymentConfirmRequest`
- **응답**: `Payment`

### 3. 결제 목록 조회
- **GET** `/payments`
- **설명**: 모든 결제 목록 조회
- **응답**: `Payment[]`

### 4. 결제 상세 조회
- **GET** `/payments/{id}`
- **설명**: 특정 결제 정보 조회
- **응답**: `Payment`

### 5. 결제 취소
- **POST** `/payments/{id}/cancel`
- **설명**: 결제 취소 (PG 승인취소)
- **응답**: `Payment`

### 6. PG사 리턴 처리
- **POST** `/payments/return`
- **설명**: PG사 결제 완료 후 리턴 처리
- **Content-Type**: `application/x-www-form-urlencoded`
- **응답**: `String`

## 적립금 관리 API

### 1. 회원 적립금 조회
- **GET** `/rewardpoints/member/{memberId}`
- **설명**: 특정 회원의 적립금 조회
- **응답**: `RewardPoints`

### 2. 적립금 추가
- **PUT** `/rewardpoints/add`
- **설명**: 회원 적립금 추가
- **요청**: `RewardPointsRequest`
- **응답**: `RewardPoints`

### 3. 적립금 사용
- **PUT** `/rewardpoints/use`
- **설명**: 회원 적립금 사용
- **요청**: `RewardPointsRequest`
- **응답**: `RewardPoints`

## 데이터 모델

### Member
```json
{
  "id": "number",
  "name": "string",
  "shippingAddress": "string",
  "phoneNumber": "string",
  "createdAt": "string (ISO 8601)"
}
```

### Product
```json
{
  "id": "number",
  "name": "string",
  "price": "number"
}
```

### Order
```json
{
  "id": "number",
  "memberId": "number",
  "orderDate": "string (ISO 8601)",
  "totalAmount": "number",
  "usedRewardPoints": "number",
  "finalPaymentAmount": "number",
  "status": "string",
  "paymentId": "number"
}
```

### Payment
```json
{
  "id": "number",
  "memberId": "number",
  "amount": "number",
  "paymentMethod": "string",
  "pgCompany": "string",
  "status": "string",
  "transactionId": "string",
  "paymentDate": "string (ISO 8601)"
}
```

### RewardPoints
```json
{
  "id": "number",
  "memberId": "number",
  "points": "number",
  "lastUpdated": "string (ISO 8601)"
}
```

## 에러 코드

| 코드 | 설명 |
|------|------|
| `MEMBER_NOT_FOUND` | 회원을 찾을 수 없음 |
| `PRODUCT_NOT_FOUND` | 상품을 찾을 수 없음 |
| `ORDER_NOT_FOUND` | 주문을 찾을 수 없음 |
| `PAYMENT_FAILED` | 결제 실패 |
| `INSUFFICIENT_POINTS` | 적립금 부족 |
| `INVALID_REQUEST` | 잘못된 요청 |
