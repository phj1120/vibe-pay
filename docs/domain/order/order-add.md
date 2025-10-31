# 주문/결제 프로세스 추가 설명서

## 문서 목적

`order.md` 파일을 기반으로 실제 FO와 API 구현을 진행한 결과, 추가로 필요한 설명사항들을 정리한 문서입니다.

---

## 1. 구현 완료 내역

### 1.1 Frontend (FO) 구현 완료 ✅

**구현 위치**: `/fo/src/app/order/`

#### 구현된 페이지
- `/order/sheet` - 주문서 페이지
- `/order/popup` - PG 결제 팝업
- `/order/return` - PG 리다이렉트 처리
- `/order/complete` - 주문 완료 페이지
- `/order/error.tsx` - 에러 바운더리

#### 구현된 유틸리티
- `/lib/order-api.ts` - API 클라이언트 함수
- `/lib/order-cookie.ts` - 쿠키 관리
- `/lib/pg-utils.ts` - PG 통합 유틸리티

#### 구현된 타입
- `/types/order.types.ts` - 주문/결제 타입 정의 (Zod 스키마 포함)
- `/types/pg-external.d.ts` - PG 외부 스크립트 타입

**상세 문서**: `/fo/src/app/order/README.md` 참조

---

## 2. Backend (API) 구현 필요 사항

### 2.1 핵심 API 엔드포인트 (3개)

#### API 1: 주문번호 생성
```
GET /api/order/generateOrderNumber
```

**구현 내용**:
- `SEQ_ORDER_NO` 시퀀스 사용
- 형식: `YYYYMMDD` + `O` + `6자리 시퀀스`
- 예: `20251031O000001`

**Mapper 메서드**:
```java
// OrderBaseTrxMapper.java
String generateOrderNo();
```

**Mapper XML**:
```xml
<select id="generateOrderNo" resultType="string">
    /* OrderBaseTrxMapper.generateOrderNo */
    SELECT TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || 'O' || LPAD(NEXTVAL('SEQ_ORDER_NO')::TEXT, 6, '0')
</select>
```

---

#### API 2: 결제 초기화
```
POST /api/payments/initiate
```

**요청 DTO** (`PaymentInitiateRequest.java`):
```java
@Getter
@Setter
public class PaymentInitiateRequest {
    private String orderNumber;      // 주문번호
    private String paymentMethod;    // 결제수단 (CARD, VIRTUAL_ACCOUNT 등)
    private String pgType;           // PG사 (INICIS, NICE)
    private Long amount;             // 결제금액
    private String productName;      // 상품명
    private String buyerName;        // 구매자명
    private String buyerEmail;       // 구매자 이메일
    private String buyerTel;         // 구매자 전화번호
}
```

**응답 DTO** (`PaymentInitiateResponse.java`):
```java
@Getter
@Setter
public class PaymentInitiateResponse {
    private String pgType;                    // PG사 타입
    private String paymentMethod;             // 결제수단
    private String merchantId;                // 가맹점 ID
    private String merchantKey;               // 가맹점 키
    private String returnUrl;                 // 리턴 URL
    private Map<String, String> formData;     // PG사별 폼 데이터
}
```

**구현 핵심 로직**:
1. PG사 선택 (가중치 기반)
   - `PAY005` enum의 `referenceValue1`로 관리되는 가중치
   - 예: 이니시스 10%, 나이스 90% → Random(1-100)으로 선택

2. PG사별 전략 패턴 적용
   - Interface: `PaymentGatewayStrategy`
   - 구현체: `InicisPaymentStrategy`, `NicePaymentStrategy`

3. 서명 데이터 생성 (SHA-256 해싱)
   ```java
   MessageDigest md = MessageDigest.getInstance("SHA-256");
   md.update(text.getBytes(StandardCharsets.UTF_8));
   return String.format("%064x", new BigInteger(1, md.digest()));
   ```

---

#### API 3: 주문 생성
```
POST /api/order/order
```

**요청 DTO** (`OrderRequest.java`):
```java
@Getter
@Setter
public class OrderRequest {
    private String memberNo;           // 회원번호
    private String memberName;         // 회원명
    private String phone;              // 전화번호
    private String email;              // 이메일
    private List<BasketResponse> goodsList;  // 상품 목록
    private List<PayRequest> payList;  // 결제 목록 (복합결제 지원)
}

@Getter
@Setter
public class PayRequest {
    private String payWayCode;         // 결제방식코드 (PAY002)
    private Long amount;               // 금액
    private String payTypeCode;        // 결제유형코드 (PAY001)
    private PaymentConfirmRequest paymentConfirmRequest;  // PG 승인 요청 데이터
}
```

**구현 프로세스**:

1. **Entity 생성**
   - `order_base` 테이블: 주문 기본 정보
   - `order_detail` 테이블: 주문 상세 정보
   - `order_goods` 테이블: 주문 상품 정보

2. **검증 로직**
   ```java
   // 재고 검증
   if (orderQuantity > stockQuantity) {
       throw new ApiException(ApiError.INSUFFICIENT_STOCK);
   }

   // 가격 검증 (가격 조작 방지)
   Long dbPrice = goodsPriceHist.getSalePrice() + goodsItem.getItemPrice();
   if (!requestPrice.equals(dbPrice)) {
       throw new ApiException(ApiError.INVALID_PRICE);
   }

   // 회원 검증
   if (!"001".equals(member.getMemberStatusCode())) {
       throw new ApiException(ApiError.INVALID_MEMBER_STATUS);
   }

   // 장바구니 검증
   if (basket.getIsOrder()) {
       throw new ApiException(ApiError.ALREADY_ORDERED);
   }
   ```

3. **결제 승인** (전략 패턴)
   ```java
   // PAY002 코드의 displaySequence 순서대로 처리
   for (PayRequest payRequest : payList) {
       PaymentWayStrategy strategy = paymentWayFactory.getStrategy(payRequest.getPayWayCode());

       try {
           PaymentResult result = strategy.processPayment(payRequest);

           // pay_base insert
           // pay_interface_log insert (요청/응답 JSON 저장)

       } catch (PaymentException e) {
           // 이전 결제 망취소 처리
           rollbackPreviousPayments(successfulPayments);
           throw new ApiException(ApiError.PAYMENT_FAILED);
       }
   }
   ```

4. **망취소 처리**
   - 카드 결제: PG사 망취소 API 호출 (TODO 처리)
   - 포인트 결제: DB 롤백으로 자동 처리

5. **후처리**
   ```java
   // 장바구니 주문완료 여부 갱신
   basketTrxMapper.updateIsOrder(basketNos, true);
   ```

---

### 2.2 전략 패턴 구조

#### PG사 선택 전략
```java
public interface PaymentGatewayStrategy {
    PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request);
}

@Component
public class InicisPaymentStrategy implements PaymentGatewayStrategy {
    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        // 이니시스 전용 폼 데이터 생성
        // signature, verification, mKey 해싱
    }
}

@Component
public class NicePaymentStrategy implements PaymentGatewayStrategy {
    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        // 나이스 전용 폼 데이터 생성
        // SignData 해싱
    }
}
```

#### 결제수단 전략
```java
public interface PaymentWayStrategy {
    PaymentResult processPayment(PayRequest request);
    void cancelPayment(String payNo);  // 망취소용
}

@Component
public class CardPaymentStrategy implements PaymentWayStrategy {
    @Override
    public PaymentResult processPayment(PayRequest request) {
        // PG사별로 승인 API 호출
        // pay_base, pay_interface_log insert
        return paymentResult;
    }

    @Override
    public void cancelPayment(String payNo) {
        // TODO: PG사 망취소 API 호출
        // pay_base 상태 변경
        // pay_interface_log insert
    }
}

@Component
public class PointPaymentStrategy implements PaymentWayStrategy {
    @Override
    public PaymentResult processPayment(PayRequest request) {
        // 포인트 사용 API 호출 (/api/point/transaction)
        // pay_base insert
        return paymentResult;
    }

    @Override
    public void cancelPayment(String payNo) {
        // DB 롤백으로 자동 처리됨
        // 별도 망취소 불필요
    }
}
```

---

### 2.3 필수 Mapper 구현

#### OrderBaseTrxMapper.java
```java
public interface OrderBaseTrxMapper {
    // 주문번호 생성
    String generateOrderNo();

    // 클레임번호 생성
    String generateClaimNo();

    // 주문 기본 정보 등록
    int insertOrderBase(OrderBase orderBase);
}
```

#### OrderDetailTrxMapper.java
```java
public interface OrderDetailTrxMapper {
    // 주문 상세 정보 등록
    int insertOrderDetail(OrderDetail orderDetail);

    // 주문 상세 정보 목록 등록
    int insertOrderDetailList(List<OrderDetail> orderDetailList);
}
```

#### OrderGoodsTrxMapper.java
```java
public interface OrderGoodsTrxMapper {
    // 주문 상품 정보 등록
    int insertOrderGoods(OrderGoods orderGoods);

    // 주문 상품 정보 목록 등록
    int insertOrderGoodsList(List<OrderGoods> orderGoodsList);
}
```

#### PayBaseTrxMapper.java
```java
public interface PayBaseTrxMapper {
    // 결제번호 생성
    String generatePayNo();

    // 결제 정보 등록
    int insertPayBase(PayBase payBase);

    // 결제 정보 수정 (취소가능금액 등)
    int updatePayBase(PayBase payBase);
}
```

#### PayInterfaceLogTrxMapper.java
```java
public interface PayInterfaceLogTrxMapper {
    // 결제 인터페이스 번호 생성
    String generatePayInterfaceNo();

    // 결제 인터페이스 로그 등록
    int insertPayInterfaceLog(PayInterfaceLog log);
}
```

---

## 3. 추가 설명이 필요한 부분

### 3.1 PG 승인 요청 상세 스펙

#### 이니시스 승인 요청
```java
POST {authUrl}  // 인증 응답의 authUrl

Request Headers:
  Content-Type: application/json

Request Body:
{
  "mid": "가맹점ID",
  "authToken": "인증토큰",
  "timestamp": "TimeInMillis",
  "signature": "SHA256(authToken, timestamp)",
  "verification": "SHA256(authToken, signKey, timestamp)",
  "charset": "UTF-8",
  "format": "JSON",
  "price": "금액"
}

Response:
{
  "resultCode": "0000",  // 0000이면 성공
  "resultMsg": "성공",
  "tid": "거래번호",
  "mid": "가맹점ID",
  "MOID": "주문번호",
  "TotPrice": "결제금액",
  "applNum": "승인번호",
  "CARD_Num": "카드번호"
}
```

#### 나이스 승인 요청
```java
POST {authUrl}  // 인증 응답의 NextAppURL

Request Headers:
  Content-Type: application/x-www-form-urlencoded

Request Body (form-urlencoded):
TID={거래번호}&
AuthToken={인증토큰}&
MID={가맹점ID}&
Amt={금액}&
EdiDate={전문생성일시}&
SignData={서명데이터}&
CharSet=UTF-8&
EdiType=JSON

Response:
{
  "ResultCode": "3001",  // 3001이면 신용카드 성공
  "ResultMsg": "성공",
  "TID": "거래ID",
  "MID": "가맹점ID",
  "Moid": "주문번호",
  "Amt": "금액",
  "AuthCode": "승인번호",
  "CardNo": "카드번호"
}
```

---

### 3.2 복합결제 처리 시나리오

#### 시나리오: 카드 11,000원 + 포인트 5,000원

1. **요청 구조**
```json
{
  "payList": [
    {
      "payWayCode": "001",  // 카드 (displaySequence: 1)
      "amount": 11000,
      "paymentConfirmRequest": { /* 이니시스 or 나이스 인증 데이터 */ }
    },
    {
      "payWayCode": "002",  // 포인트 (displaySequence: 2)
      "amount": 5000
    }
  ]
}
```

2. **처리 순서** (displaySequence 기준)
   - 카드 결제 먼저 처리
   - 카드 성공 → 포인트 처리
   - 포인트 실패 → 카드 망취소

3. **DB 저장 결과**

**pay_base 테이블**:
```
| pay_no | pay_way_code | amount | pg_type_code | upper_pay_no |
|--------|-------------|--------|--------------|--------------|
| 000001 | 001         | 11000  | 001(이니시스) |              |
| 000002 | 002         | 5000   |              |              |
```

**pay_interface_log 테이블** (카드만):
```
| pay_interface_no | pay_no | pay_log_code | request_json | response_json |
|-----------------|--------|--------------|--------------|---------------|
| 000001          | 000001 | 001(결제)     | {...}        | {...}         |
| 000002          | 000001 | 002(승인)     | {...}        | {...}         |
```

---

### 3.3 가격 검증 로직 상세

```java
// 주문 시점의 가격 = goods_price_hist.sale_price + goods_item.item_price

// 1. goods_price_hist에서 현재 유효한 가격 조회
GoodsPriceHist priceHist = goodsPriceHistMapper.selectCurrentPrice(
    goodsNo,
    LocalDateTime.now()
);

// 2. goods_item에서 단품 가격 조회
GoodsItem goodsItem = goodsItemMapper.selectByKey(goodsNo, itemNo);

// 3. 합산 가격 계산
Long dbPrice = priceHist.getSalePrice() + goodsItem.getItemPrice();

// 4. 요청 가격과 비교
if (!orderGoods.getSalePrice().equals(dbPrice)) {
    throw new ApiException(ApiError.INVALID_PRICE,
        "가격이 변경되었습니다. 다시 주문해주세요.");
}
```

**이유**:
- 클라이언트에서 가격을 임의로 조작할 수 있으므로
- 주문 시점의 DB 가격과 반드시 일치해야 함

---

### 3.4 재고 검증 및 차감

```java
// 1. 현재 재고 조회
GoodsItem goodsItem = goodsItemMapper.selectByKey(goodsNo, itemNo);

// 2. 재고 검증
if (goodsItem.getStock() < orderQuantity) {
    throw new ApiException(ApiError.INSUFFICIENT_STOCK,
        "재고가 부족합니다. (재고: " + goodsItem.getStock() + ")");
}

// 3. 재고 차감 (낙관적 락 사용)
int updated = goodsItemTrxMapper.updateStockWithLock(
    goodsNo,
    itemNo,
    orderQuantity,
    goodsItem.getStock()  // WHERE stock = #{originalStock}
);

if (updated == 0) {
    // 다른 주문에서 재고를 먼저 차감함
    throw new ApiException(ApiError.STOCK_CONFLICT,
        "재고 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
}
```

**참고**:
- 요구사항에서 동시성 문제는 고려하지 않는다고 했으나
- 실제 구현 시에는 낙관적 락을 사용하는 것이 안전함

---

## 4. 테스트 시나리오

### 4.1 정상 흐름 테스트

#### 시나리오 1: 단일 상품, 카드 단독 결제 (이니시스)
```
1. 상품A 1개 선택 (10,000원)
2. 주문서 작성
3. 카드 결제 선택 (이니시스)
4. 결제 진행
5. 주문 완료

예상 결과:
- order_base: 1건
- order_detail: 1건 (order_sequence=1, order_process_sequence=1)
- order_goods: 1건
- pay_base: 1건 (pay_way_code=001, pg_type_code=001)
- pay_interface_log: 2건 (결제요청, 승인요청)
```

#### 시나리오 2: 여러 상품, 복합 결제 (나이스)
```
1. 상품A 1개 (10,000원) + 상품B 1개 (8,000원) 선택
2. 주문서 작성
3. 카드 11,000원 + 포인트 5,000원 + 배송비 2,000원 = 18,000원
4. 결제 진행 (나이스)
5. 주문 완료

예상 결과:
- order_base: 1건
- order_detail: 2건 (order_sequence=1,2)
- order_goods: 2건
- pay_base: 2건 (카드 1건, 포인트 1건)
- pay_interface_log: 2건 (카드만)
- point_history: 2건 (적립 1건, 사용 1건)
```

---

### 4.2 예외 흐름 테스트

#### 시나리오 3: 재고 부족
```
1. 재고 3개인 상품을 5개 주문 시도
2. 주문 API 호출
3. 재고 검증 실패

예상 결과:
- HTTP 400
- ApiError.INSUFFICIENT_STOCK
- DB 변경 없음
```

#### 시나리오 4: 가격 변경 (가격 조작 방지)
```
1. 주문서에서 본 가격: 10,000원
2. 주문 중 관리자가 가격을 15,000원으로 변경
3. 주문 API 호출 (10,000원으로 요청)
4. 가격 검증 실패

예상 결과:
- HTTP 400
- ApiError.INVALID_PRICE
- DB 변경 없음
- 사용자에게 "가격이 변경되었습니다" 메시지
```

#### 시나리오 5: 복합결제 중 포인트 부족
```
1. 카드 10,000원 + 포인트 5,000원 주문
2. 보유 포인트: 3,000원
3. 카드 결제 성공
4. 포인트 결제 실패
5. 카드 망취소 실행

예상 결과:
- HTTP 400
- ApiError.INSUFFICIENT_POINT
- pay_base: 카드 1건(취소됨), 포인트 0건
- pay_interface_log: 결제요청, 승인요청, 망취소요청 (3건)
- 주문 생성 안됨
```

---

## 5. 프론트엔드 연동 시 주의사항

### 5.1 PG 인증 데이터 전달

Frontend에서 `/api/order/order` 호출 시, PG 인증 응답을 그대로 전달:

```typescript
// PaymentConfirmRequest
{
  // 공통
  pgTypeCode: string;      // PAY005 코드
  authToken: string;
  orderNo: string;
  authUrl: string;
  netCancelUrl: string;

  // 나이스 전용
  transactionId?: string;  // Signature
  amount?: string;
  tradeNo?: string;        // TxTid
  mid?: string;

  // 이니시스 전용 (필요 시)
}
```

### 5.2 에러 처리

Backend에서 던지는 예외와 Frontend 메시지 매핑:

```typescript
const ERROR_MESSAGES = {
  'INSUFFICIENT_STOCK': '재고가 부족합니다. 다시 확인해주세요.',
  'INVALID_PRICE': '가격이 변경되었습니다. 장바구니에서 다시 확인해주세요.',
  'INVALID_MEMBER_STATUS': '주문 가능한 회원 상태가 아닙니다.',
  'ALREADY_ORDERED': '이미 주문된 상품입니다.',
  'PAYMENT_FAILED': '결제에 실패했습니다. 다시 시도해주세요.',
  'INSUFFICIENT_POINT': '포인트가 부족합니다.',
};
```

### 5.3 타임아웃 처리

```typescript
// 쿠키 만료 시간: 5분
// 주문 API 타임아웃: 30초 권장

const ORDER_TIMEOUT = 30000; // 30초

try {
  const response = await Promise.race([
    createOrder(orderRequest),
    timeout(ORDER_TIMEOUT)
  ]);
} catch (error) {
  if (error instanceof TimeoutError) {
    alert('주문 처리 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.');
  }
}
```

---

## 6. 운영 시 고려사항

### 6.1 시퀀스 관리

```sql
-- 주문번호 시퀀스는 CYCLE 옵션으로 일일 최대 999,999건
-- 초과 시 1부터 재시작하지만, 날짜가 달라서 중복 없음

-- 현재 값 조회
SELECT CURRVAL('SEQ_ORDER_NO');

-- 필요 시 수동 초기화 (비추천)
ALTER SEQUENCE SEQ_ORDER_NO RESTART WITH 1;
```

### 6.2 PG 장애 대응

```java
// PG 타임아웃 설정
RestTemplate restTemplate = new RestTemplateBuilder()
    .setConnectTimeout(Duration.ofSeconds(5))
    .setReadTimeout(Duration.ofSeconds(10))
    .build();

// 재시도 로직 (선택사항)
@Retryable(
    value = {PgTimeoutException.class},
    maxAttempts = 2,
    backoff = @Backoff(delay = 1000)
)
public PaymentResult callPgApproval(PaymentRequest request) {
    // PG 승인 API 호출
}
```

### 6.3 로깅

```java
// 주문 시작
log.info("Order process started. orderNo={}, memberNo={}", orderNo, memberNo);

// 검증 통과
log.debug("Validation passed. orderNo={}", orderNo);

// 결제 승인 요청
log.info("Payment approval requested. payWayCode={}, amount={}",
    payWayCode, amount);

// 결제 승인 성공
log.info("Payment approved. payNo={}, approveNo={}", payNo, approveNo);

// 주문 완료
log.info("Order completed. orderNo={}, finalAmount={}", orderNo, finalAmount);

// 에러 발생
log.error("Order failed. orderNo={}, error={}", orderNo, e.getMessage(), e);
```

### 6.4 모니터링 지표

```
- 주문 성공률: 성공 건수 / 전체 시도 건수
- 결제 승인률: 승인 건수 / 승인 요청 건수
- 평균 주문 처리 시간
- PG별 성공률 (이니시스 vs 나이스)
- 에러 유형별 발생 빈도
```

---

## 7. API 명세서 샘플

### 7.1 주문번호 생성 API

```yaml
GET /api/order/generateOrderNumber

Response 200:
{
  "timestamp": "2025-10-31T10:30:00.000",
  "code": "0000",
  "message": "성공",
  "payload": {
    "orderNumber": "20251031O000001"
  }
}
```

### 7.2 결제 초기화 API

```yaml
POST /api/payments/initiate

Request:
{
  "orderNumber": "20251031O000001",
  "paymentMethod": "CARD",
  "pgType": "INICIS",
  "amount": 18000,
  "productName": "상품A 외 1건",
  "buyerName": "홍길동",
  "buyerEmail": "test@example.com",
  "buyerTel": "010-1234-5678"
}

Response 200:
{
  "timestamp": "2025-10-31T10:30:00.000",
  "code": "0000",
  "message": "성공",
  "payload": {
    "pgType": "INICIS",
    "paymentMethod": "CARD",
    "merchantId": "INIpayTest",
    "merchantKey": "...",
    "returnUrl": "http://localhost:3000/order/return",
    "formData": {
      "mid": "INIpayTest",
      "orderNumber": "20251031O000001",
      "price": "18000",
      "goodsName": "상품A 외 1건",
      "buyerName": "홍길동",
      "buyerEmail": "test@example.com",
      "buyerTel": "010-1234-5678",
      "returnUrl": "http://localhost:3000/order/return",
      "signature": "...",
      "verification": "...",
      "mKey": "...",
      "timestamp": "...",
      "closeUrl": "http://localhost:3000/order/close"
    }
  }
}
```

### 7.3 주문 생성 API

```yaml
POST /api/order/order

Request:
{
  "memberNo": "000000000000001",
  "memberName": "홍길동",
  "phone": "010-1234-5678",
  "email": "test@example.com",
  "goodsList": [
    {
      "basketNo": "000000000000001",
      "goodsNo": "G00000000000001",
      "itemNo": "001",
      "quantity": 1,
      "salePrice": 10000,
      "supplyPrice": 5000,
      "goodsName": "상품A",
      "itemName": "단품A"
    }
  ],
  "payList": [
    {
      "payWayCode": "001",
      "amount": 10000,
      "payTypeCode": "001",
      "paymentConfirmRequest": {
        "pgTypeCode": "001",
        "authToken": "...",
        "orderNo": "20251031O000001",
        "authUrl": "https://pg.inicis.com/approve",
        "netCancelUrl": "https://pg.inicis.com/cancel"
      }
    }
  ]
}

Response 200:
{
  "timestamp": "2025-10-31T10:35:00.000",
  "code": "0000",
  "message": "성공",
  "payload": null
}

Response 400 (예: 재고 부족):
{
  "timestamp": "2025-10-31T10:35:00.000",
  "code": "1003",
  "message": "재고가 부족합니다.",
  "payload": null
}
```

---

## 8. 요약

### 구현 완료
✅ Frontend (FO) - 완전 구현
✅ 설계 문서 - 상세 작성

### 구현 필요
⏳ Backend (API) - 구현 가이드 제공

### 핵심 포인트
1. **전략 패턴**: PG사 선택, 결제수단 처리
2. **가중치 기반 PG 선택**: PAY005 enum의 referenceValue1 활용
3. **복합결제**: displaySequence 순서로 처리, 실패 시 망취소
4. **검증 로직**: 재고, 가격, 회원상태, 장바구니
5. **망취소**: 카드는 PG API, 포인트는 DB 롤백
6. **로그 저장**: pay_interface_log에 요청/응답 JSON 저장

이 문서를 기반으로 Backend 개발을 진행하시면 됩니다!
