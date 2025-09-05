# 데이터 검증 및 비즈니스 로직 개선

## 🎯 개선 요구사항

1. **PG사 결제 요청/승인 로그** → 인터페이스 로그 테이블에 정확히 기록
2. **Payment 테이블** → 성공한 건에 대해서만 저장
3. **PG사 선택** → INICIS로 고정 (NICEPAY는 추후 추가)
4. **적립금 사용** → payment_method에 mileage 추가

## 🔄 변경사항

### 1. PaymentService.initiatePayment() 개선

#### Before
```java
// PG 선택(샘플)
Random random = new Random();
String pgCompany = random.nextBoolean() ? "INICIS" : "NICEPAY";

// 결제 레코드 생성
Payment payment = new Payment(...);
paymentMapper.insert(payment);
```

#### After
```java
// PG 선택 - 현재는 INICIS만 지원 (추후 NICEPAY 추가 예정)
String pgCompany = "INICIS";

// 결제 방법 결정 (적립금 사용 여부에 따라)
String paymentMethod = request.getPaymentMethod();
if (request.getUsedMileage() != null && request.getUsedMileage() > 0) {
    paymentMethod = paymentMethod + "+MILEAGE";
}

// 인터페이스 로그 먼저 기록 (결제 요청 시작)
PaymentInterfaceRequestLog initiateLog = new PaymentInterfaceRequestLog(
    null, "INITIATE_PAYMENT", 
    "Request: memberId=" + request.getMemberId() + ", usedMileage=" + request.getUsedMileage(),
    "Starting payment initiation process"
);

// Payment 테이블에는 저장하지 않고 임시ID만 생성
Long tempPaymentId = System.currentTimeMillis();
```

### 2. PaymentService.confirmPayment() 개선

#### Before
```java
// 기존 Payment 레코드 찾기
Payment payment = paymentMapper.findById(paymentId);

// 상태 업데이트
payment.setStatus("SUCCESS" or "FAILED");
paymentMapper.update(payment);
```

#### After
```java
// 승인 요청 로그 기록
PaymentInterfaceRequestLog approvalRequestLog = new PaymentInterfaceRequestLog(
    tempPaymentId, "INICIS_APPROVAL_REQUEST", 
    "Request: authToken=" + request.getAuthToken(),
    "Starting Inicis approval process"
);

// 이니시스 승인 처리
boolean approvalSuccess = processInicisApproval(request);

// 승인 응답 로그 기록
PaymentInterfaceRequestLog approvalResponseLog = new PaymentInterfaceRequestLog(
    tempPaymentId, "INICIS_APPROVAL_RESPONSE",
    "Response: success=" + approvalSuccess,
    approvalSuccess ? "Inicis approval successful" : "Inicis approval failed"
);

// 성공한 경우에만 Payment 테이블에 저장
if (approvalSuccess) {
    Payment payment = new Payment(
        null, Double.valueOf(request.getPrice()),
        paymentMethod, "INICIS", "SUCCESS", transactionId
    );
    paymentMapper.insert(payment);
} else {
    throw new RuntimeException("Payment approval failed");
}
```

### 3. PaymentInitiateRequest 확장

#### 새로운 필드 추가
```java
public class PaymentInitiateRequest {
    // 기존 필드들...
    private Double usedMileage;   // 사용한 적립금
    
    public Double getUsedMileage() { return usedMileage; }
    public void setUsedMileage(Double usedMileage) { this.usedMileage = usedMileage; }
}
```

### 4. 프론트엔드 연동 개선

#### 적립금 정보 전달
```javascript
const initiatePayload = {
  memberId: selectedMember.value.id,
  amount: total.value,
  paymentMethod: 'CREDIT_CARD',
  usedMileage: usedPoints.value, // 적립금 사용량 추가
  goodName: goodName || '주문결제',
  buyerName: selectedMember.value.name || '구매자',
  // ...
};
```

## 📊 데이터 흐름 개선

### 인터페이스 로그 테이블 기록 패턴

| 단계 | 액션 | 로그 타입 | 설명 |
|------|------|-----------|------|
| 1 | 결제 시작 | `INITIATE_PAYMENT` | 결제 요청 시작 |
| 2 | 파라미터 생성 | `INICIS_PARAMETERS_GENERATED` | 이니시스 파라미터 생성 완료 |
| 3 | 승인 요청 | `INICIS_APPROVAL_REQUEST` | 이니시스 승인 API 호출 시작 |
| 4 | 승인 응답 | `INICIS_APPROVAL_RESPONSE` | 이니시스 승인 API 응답 |

### Payment 테이블 저장 조건

```
✅ 저장되는 경우:
- 이니시스 승인 성공 (resultCode: "0000")
- 승인 API 호출 성공
- 트랜잭션 ID 생성 성공

❌ 저장되지 않는 경우:
- 이니시스 승인 실패
- 승인 API 호출 실패
- 네트워크 오류 등
```

## 🎨 적립금 사용 로직

### payment_method 값 결정

```java
String paymentMethod = request.getPaymentMethod(); // "CREDIT_CARD"

if (request.getUsedMileage() != null && request.getUsedMileage() > 0) {
    paymentMethod = paymentMethod + "+MILEAGE"; // "CREDIT_CARD+MILEAGE"
}
```

### 예시 데이터

| 결제 방법 | 적립금 사용 | payment_method 값 |
|-----------|-------------|-------------------|
| 신용카드 | 0원 | `CREDIT_CARD` |
| 신용카드 | 5,000원 | `CREDIT_CARD+MILEAGE` |
| 계좌이체 | 2,000원 | `BANK_TRANSFER+MILEAGE` |

## 🔮 확장성 고려사항

### NICEPAY 추가 시 구조

```java
// PG 선택 로직 (추후 확장)
private String selectPgCompany(PaymentInitiateRequest request) {
    // 현재는 INICIS만 지원
    return "INICIS";
    
    // 추후 NICEPAY 추가 시:
    // if (request.getPreferredPg() != null) {
    //     return request.getPreferredPg();
    // }
    // return "INICIS"; // 기본값
}
```

### 인터페이스 로그 확장

```java
// PG사별 로그 타입 구분
String logType = pgCompany + "_APPROVAL_REQUEST"; // "INICIS_APPROVAL_REQUEST" or "NICEPAY_APPROVAL_REQUEST"
```

## 📈 개선 효과

### 1. 데이터 정합성 향상
- ✅ 실패한 결제는 Payment 테이블에 저장되지 않음
- ✅ 모든 PG 통신 내역이 인터페이스 로그에 기록됨

### 2. 비즈니스 로직 명확화
- ✅ PG사 선택 로직 단순화 (INICIS 고정)
- ✅ 적립금 사용 여부가 payment_method에 명확히 표시

### 3. 확장성 확보
- ✅ NICEPAY 추가 시 최소한의 코드 변경으로 대응 가능
- ✅ 새로운 결제 방법 추가 시 확장 용이

### 4. 운영 효율성 향상
- ✅ 결제 실패 원인 추적 용이 (상세한 인터페이스 로그)
- ✅ 데이터 일관성 보장으로 정산 오류 방지
