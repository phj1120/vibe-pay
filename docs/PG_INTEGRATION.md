# PG사 연동 명세서

## 지원 PG사

- **나이스페이**: https://developers.nicepay.co.kr/manual-auth.php
- **이니시스**: https://manual.inicis.com/pay/stdpay_pc.html

## PG사 선택 로직

### 비율 기반 선택
```java
// 예시: 70% 이니시스, 30% 나이스페이
public String selectPgCompany() {
    Random random = new Random();
    int ratio = random.nextInt(100);
    
    if (ratio < 70) {
        return "INICIS";
    } else {
        return "NICEPAY";
    }
}
```

## 이니시스 연동

### 1. 설정 정보
```yaml
# application.yml
inicis:
  mid: INIpayTest
  signKey: SU5JTElURV9UUklQTEVERVNfS0VZU1RS
  version: 1.0
  currency: WON
  gopaymethod: Card
  acceptmethod: below1000
  returnUrl: http://localhost:8080/api/payments/return
  closeUrl: http://localhost:3000/order/close
```

### 2. 결제 파라미터 생성
```java
// InicisPaymentParameters.java
public class InicisPaymentParameters {
    private String mid;           // 상점 ID
    private String oid;           // 주문번호
    private String price;         // 결제금액
    private String timestamp;     // 타임스탬프
    private String signature;     // 서명
    private String verification;  // 검증값
    private String mKey;          // 상점 키
    private String version;       // 버전
    private String currency;      // 통화
    private String moId;          // 모바일 ID
    private String goodName;      // 상품명
    private String buyerName;     // 구매자명
    private String buyerTel;      // 구매자 전화번호
    private String buyerEmail;    // 구매자 이메일
    private String returnUrl;     // 리턴 URL
    private String closeUrl;      // 닫기 URL
    private String gopaymethod;   // 결제수단
    private String acceptmethod;  // 승인방법
}
```

### 3. 서명 생성
```java
public String generateSignature(String mid, String oid, String price, String timestamp, String signKey) {
    String data = mid + oid + price + timestamp;
    return DigestUtils.sha256Hex(data + signKey);
}
```

### 4. 결제 프로세스

#### 4.1 결제 시작
```java
@PostMapping("/initiate")
public ResponseEntity<InicisPaymentParameters> initiatePayment(@RequestBody PaymentInitiateRequest request) {
    // 1. 주문번호 생성
    String oid = "ORDER_" + System.currentTimeMillis();
    
    // 2. 타임스탬프 생성
    String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
    
    // 3. 서명 생성
    String signature = generateSignature(mid, oid, request.getAmount(), timestamp, signKey);
    
    // 4. 파라미터 생성
    InicisPaymentParameters params = new InicisPaymentParameters();
    // ... 파라미터 설정
    
    return ResponseEntity.ok(params);
}
```

#### 4.2 결제 승인
```java
@PostMapping("/confirm")
public ResponseEntity<Payment> confirmPayment(@RequestBody PaymentConfirmRequest request) {
    // 1. PG사 승인 요청
    // 2. 승인 결과 처리
    // 3. 결제 정보 저장
    // 4. 로그 기록
    
    return ResponseEntity.ok(payment);
}
```

#### 4.3 리턴 처리
```java
@PostMapping(value = "/return", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
public ResponseEntity<String> stdPayReturn(@RequestParam MultiValueMap<String, String> form) {
    // 1. 리턴 파라미터 검증
    // 2. 결제 상태 확인
    // 3. 결과 처리
    
    return ResponseEntity.ok("Payment return received.");
}
```

### 5. 프론트엔드 연동

#### 5.1 SDK 로드
```typescript
// plugins/inicis.client.ts
export default defineNuxtPlugin(() => {
  if (typeof window === 'undefined') return;
  
  const src = 'https://stdpay.inicis.com/stdjs/INIStdPay.js';
  if (!document.querySelector(`script[src="${src}"]`)) {
    const script = document.createElement('script');
    script.src = src;
    script.async = true;
    document.head.appendChild(script);
  }
});
```

#### 5.2 결제 호출
```typescript
// pages/order/index.vue
const proceedToPayment = async () => {
  // 1. 결제 파라미터 생성
  const response = await fetch('/api/payments/initiate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(paymentRequest)
  });
  
  const inicisParams = await response.json();
  
  // 2. SDK 로드 대기
  await loadInicisSDK();
  
  // 3. 결제창 호출
  window.INIStdPay.pay('inicisForm');
};
```

## 나이스페이 연동

### 1. 설정 정보
```yaml
# application.yml
nicepay:
  clientId: "your_client_id"
  clientSecret: "your_client_secret"
  baseUrl: "https://sandbox-api.nicepay.co.kr"
```

### 2. 결제 파라미터
```java
public class NicepayPaymentParameters {
    private String orderId;       // 주문번호
    private String amount;        // 결제금액
    private String goodsName;     // 상품명
    private String buyerName;     // 구매자명
    private String buyerTel;      // 구매자 전화번호
    private String buyerEmail;    // 구매자 이메일
    private String returnUrl;     // 리턴 URL
    private String cancelUrl;     // 취소 URL
}
```

### 3. 결제 프로세스

#### 3.1 결제 요청
```java
@PostMapping("/nicepay/initiate")
public ResponseEntity<NicepayPaymentParameters> initiateNicepayPayment(@RequestBody PaymentInitiateRequest request) {
    // 1. 나이스페이 API 호출
    // 2. 결제 URL 생성
    // 3. 파라미터 반환
    
    return ResponseEntity.ok(nicepayParams);
}
```

#### 3.2 승인 처리
```java
@PostMapping("/nicepay/confirm")
public ResponseEntity<Payment> confirmNicepayPayment(@RequestBody NicepayConfirmRequest request) {
    // 1. 나이스페이 승인 API 호출
    // 2. 승인 결과 처리
    // 3. 결제 정보 저장
    
    return ResponseEntity.ok(payment);
}
```

## 결제 로그 관리

### 1. 로그 테이블
```sql
CREATE TABLE payment_interface_request_log (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT,
    request_type VARCHAR(50) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    timestamp TIMESTAMP NOT NULL
);
```

### 2. 로그 기록
```java
public void logPaymentRequest(Long paymentId, String requestType, Object request, Object response) {
    PaymentInterfaceRequestLog log = new PaymentInterfaceRequestLog();
    log.setPaymentId(paymentId);
    log.setRequestType(requestType);
    log.setRequestPayload(JsonUtils.toJson(request));
    log.setResponsePayload(JsonUtils.toJson(response));
    log.setTimestamp(LocalDateTime.now());
    
    paymentLogService.save(log);
}
```

## 에러 처리

### 1. PG사 에러 코드
```java
public enum PgErrorCode {
    SUCCESS("0000", "성공"),
    INVALID_PARAMETER("1001", "잘못된 파라미터"),
    PAYMENT_FAILED("2001", "결제 실패"),
    NETWORK_ERROR("3001", "네트워크 오류");
    
    private final String code;
    private final String message;
}
```

### 2. 에러 처리 로직
```java
public PaymentResult processPayment(PaymentRequest request) {
    try {
        // 결제 처리
        return processPaymentInternal(request);
    } catch (PgException e) {
        log.error("PG 결제 오류: {}", e.getMessage());
        return PaymentResult.failure(e.getCode(), e.getMessage());
    } catch (Exception e) {
        log.error("결제 처리 중 오류 발생", e);
        return PaymentResult.failure("SYSTEM_ERROR", "시스템 오류가 발생했습니다.");
    }
}
```

## 보안 고려사항

### 1. 서명 검증
- 모든 PG사 요청에 대한 서명 검증
- 타임스탬프 기반 리플레이 공격 방지

### 2. 데이터 암호화
- 민감한 정보 암호화 저장
- HTTPS 통신 강제

### 3. 로그 보안
- 개인정보 마스킹 처리
- 로그 접근 권한 관리

## 테스트

### 1. 테스트 카드 정보
```yaml
# 이니시스 테스트 카드
test_cards:
  success: "4111111111111111"
  failure: "4111111111111112"
  expired: "4111111111111113"
```

### 2. 테스트 시나리오
- 정상 결제
- 결제 실패
- 네트워크 오류
- 타임아웃
- 취소/환불

## 모니터링

### 1. 결제 성공률 모니터링
- PG사별 성공률 추적
- 실패 원인 분석

### 2. 응답 시간 모니터링
- API 응답 시간 측정
- 성능 최적화 지점 파악

### 3. 알림 설정
- 결제 실패율 임계값 초과 시 알림
- 시스템 오류 발생 시 알림
