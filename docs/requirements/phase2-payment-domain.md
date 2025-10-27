# Phase 2-4: Payment Domain Requirements

## Overview
Payment domain is the most complex, handling multi-PG integration using Factory, Adapter, and Strategy design patterns. Supports INICIS, NicePay, Toss payment gateways and point payments, with weighted PG selection capability.

## Prerequisites
- Phase 1 Data Layer completed
- Phase 2-3 RewardPoints domain completed
- All previous domains functional

## Architecture Patterns

### 1. Factory Pattern
- **PaymentProcessorFactory**: Creates payment processors based on payment method (CARD, POINT)
- **PaymentGatewayFactory**: Creates PG adapters based on PG company (INICIS, NICEPAY, TOSS)

### 2. Adapter Pattern
- **PaymentGatewayAdapter** (interface): Unified interface for all PG integrations
- **InicisAdapter**: INICIS-specific implementation
- **NicePayAdapter**: NicePay-specific implementation
- **TossAdapter**: Toss-specific implementation

### 3. Strategy Pattern
- **PaymentProcessor** (interface): Unified payment processing interface
- **CreditCardPaymentProcessor**: Card payment via PG
- **PointPaymentProcessor**: Point payment logic

## Backend Implementation

### Core Interfaces

#### PaymentGatewayAdapter.java
```java
package com.vibe.pay.backend.payment.gateway;

public interface PaymentGatewayAdapter {
    PaymentInitResponse initiate(PaymentInitiateRequest request);
    Payment confirm(PaymentConfirmRequest request);
    Payment cancel(PaymentCancelRequest request);
    Payment netCancel(PaymentNetCancelRequest request);
}
```

#### PaymentProcessor.java
```java
package com.vibe.pay.backend.payment.processor;

public interface PaymentProcessor {
    Payment processPayment(PaymentConfirmRequest request);
    void processRefund(Payment payment);
    void netCancel(PaymentNetCancelRequest request);
}
```

### Factory Classes

#### PaymentProcessorFactory.java
**Location:** `com.vibe.pay.backend.payment.factory.PaymentProcessorFactory`

**Methods:**
```java
@Component
public class PaymentProcessorFactory {
    @Autowired
    private CreditCardPaymentProcessor creditCardProcessor;
    @Autowired
    private PointPaymentProcessor pointProcessor;

    public PaymentProcessor getProcessor(String paymentMethod) {
        if ("CREDIT_CARD".equals(paymentMethod)) {
            return creditCardProcessor;
        } else if ("POINT".equals(paymentMethod)) {
            return pointProcessor;
        }
        throw new IllegalArgumentException("Unknown payment method: " + paymentMethod);
    }
}
```

#### PaymentGatewayFactory.java
**Location:** `com.vibe.pay.backend.payment.factory.PaymentGatewayFactory`

**Methods:**
```java
@Component
public class PaymentGatewayFactory {
    @Autowired
    private InicisAdapter inicisAdapter;
    @Autowired
    private NicePayAdapter nicePayAdapter;
    @Autowired
    private TossAdapter tossAdapter;

    public PaymentGatewayAdapter getAdapter(String pgCompany) {
        if ("INICIS".equals(pgCompany)) {
            return inicisAdapter;
        } else if ("NICEPAY".equals(pgCompany)) {
            return nicePayAdapter;
        } else if ("TOSS".equals(pgCompany)) {
            return tossAdapter;
        }
        throw new IllegalArgumentException("Unknown PG company: " + pgCompany);
    }
}
```

### PG Adapter Implementations

#### InicisAdapter.java
**Key Features:**
- Signature generation using HashUtils
- REST API integration via WebClient
- Request/response logging via PaymentInterfaceRequestLogService

**Methods:**
1. `initiate()`: Generate payment parameters with signature
2. `confirm()`: Approve payment via INICIS API
3. `cancel()`: Cancel approved payment
4. `netCancel()`: Cancel payment after order creation failure

**Configuration (application.yml):**
```yaml
inicis:
  mid: INIpayTest
  signKey: SU5JTElURV9UUklQTEVERVNfS0VZU1RS
  apiKey: ItEQKi3rY7uvDS8l
  returnUrl: http://localhost:3000/order/progress-popup
  closeUrl: http://localhost:3000/order/popup
  refundUrl: https://iniapi.inicis.com/v2/pg/refund
```

#### NicePayAdapter.java
**Key Features:**
- Merchant key-based authentication
- Different API structure than INICIS
- Form-based payment submission

**Configuration (application.yml):**
```yaml
nicepay:
  mid: nicepay00m
  merchantKey: EYzu8jGGMfqaDEp76gSckuvnaHHu+bC4opsSN6lHv3b2lurNYkVXrZ7Z1AoqQnXI3eLuaUFyoRNC6FkrzVjceg==
  returnUrl: http://localhost:3000/order/progress-popup
  cancelUrl: http://localhost:3000/order/popup
```

#### TossAdapter.java
**Key Features:**
- Client key & secret key authentication
- Modern REST API
- Simplified integration

**Configuration (application.yml):**
```yaml
toss:
  clientKey: test_clientkey
  secretKey: test_secretkey
  successUrl: http://localhost:3000/order/complete
  failUrl: http://localhost:3000/order/popup
```

### Payment Processor Implementations

#### CreditCardPaymentProcessor.java
**Dependencies:**
- PaymentMapper
- PaymentGatewayFactory
- PaymentInterfaceRequestLogService

**Logic:**
1. Generate payment ID using sequence
2. Get PG adapter from factory
3. Call PG confirm API
4. Save payment record
5. Return approved payment

#### PointPaymentProcessor.java
**Dependencies:**
- PaymentMapper
- RewardPointsService
- PointHistoryService

**Logic:**
1. Generate payment ID
2. Check point balance
3. Use points via RewardPointsService
4. Save payment record (pgCompany = null)
5. Return approved payment

### Utility Classes

#### PgWeightSelector.java
**Location:** `com.vibe.pay.backend.payment.PgWeightSelector`

**Purpose:** Probability-based PG selection

**Configuration (application.yml):**
```yaml
payment:
  weight:
    inicis: 50
    nicepay: 50
```

**Method:**
```java
public String selectPgCompanyByWeight() {
    int total = inicisWeight + nicepayWeight;
    int random = ThreadLocalRandom.current().nextInt(total);

    if (random < inicisWeight) {
        return "INICIS";
    } else {
        return "NICEPAY";
    }
}
```

#### HashUtils.java
**Location:** `com.vibe.pay.backend.util.HashUtils`

**Purpose:** Generate HMAC-SHA256 signatures for PG APIs

**Method:**
```java
public static String generateSignature(String data, String key)
```

#### WebClientUtil.java
**Location:** `com.vibe.pay.backend.util.WebClientUtil`

**Purpose:** HTTP client for PG API calls

**Methods:**
- `post(String url, Object body)`: POST request
- `get(String url)`: GET request

### PaymentService.java

**Location:** `com.vibe.pay.backend.payment.PaymentService`

**Dependencies:**
- PaymentMapper
- PaymentProcessorFactory
- PaymentGatewayFactory
- PgWeightSelector

**Methods:**

#### 1. initiatePayment
```java
@Transactional
public PaymentInitResponse initiatePayment(PaymentInitiateRequest request)
```
**Logic:**
1. Determine PG company (direct selection or weighted)
2. Get PG adapter from factory
3. Call adapter.initiate()
4. Return init response with selectedPgCompany

#### 2. confirmPayment
```java
@Transactional
public Payment confirmPayment(PaymentConfirmRequest request)
```
**Logic:**
1. Get processor from factory based on payment method
2. Call processor.processPayment()
3. Return approved payment

#### 3. findByOrderId
```java
public List<Payment> findByOrderId(String orderId)
```

#### 4. getPaymentById
```java
public Optional<Payment> getPaymentById(String paymentId)
```

### PaymentInterfaceRequestLogService.java

**Location:** `com.vibe.pay.backend.paymentlog.PaymentInterfaceRequestLogService`

**Purpose:** Log all PG API requests/responses for debugging and audit

**Method:**
```java
@Transactional
public void logRequest(String paymentId, String requestType, String requestPayload, String responsePayload)
```

### PaymentController.java

**Location:** `com.vibe.pay.backend.payment.PaymentController`

**Base Path:** `/api/payments`

#### API Endpoints

##### POST /api/payments/initiate
**Description:** Initialize payment (generate PG parameters)

**Request Body:**
```json
{
  "memberId": 1,
  "orderId": "20250115O00000001",
  "amount": 50000,
  "paymentMethod": "CREDIT_CARD",
  "pgCompany": "INICIS",
  "usedMileage": 5000,
  "goodName": "상품1, 상품2",
  "buyerName": "현준",
  "buyerTel": "010-1234-5678",
  "buyerEmail": "test@test.com"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "paymentId": "20250115P00000001",
  "selectedPgCompany": "INICIS",
  "mid": "INIpayTest",
  "timestamp": "20250115103000",
  "oid": "20250115O00000001",
  "price": "50000",
  "goodName": "상품1, 상품2",
  "signature": "generated_signature_here",
  "returnUrl": "http://localhost:3000/order/progress-popup",
  "closeUrl": "http://localhost:3000/order/popup"
}
```

##### POST /api/payments/confirm
**Description:** Confirm payment approval

**Request Body:**
```json
{
  "authToken": "token_from_pg",
  "authUrl": "pg_auth_url",
  "nextAppUrl": "next_app_url",
  "orderId": "20250115O00000001",
  "price": 50000,
  "mid": "INIpayTest",
  "netCancelUrl": "pg_cancel_url",
  "memberId": 1,
  "paymentMethod": "CREDIT_CARD",
  "pgCompany": "INICIS",
  "txTid": "transaction_id"
}
```

**Response:** `200 OK`
```json
{
  "paymentId": "20250115P00000001",
  "memberId": 1,
  "orderId": "20250115O00000001",
  "amount": 50000,
  "paymentMethod": "CREDIT_CARD",
  "payType": "PAYMENT",
  "pgCompany": "INICIS",
  "status": "APPROVED",
  "orderStatus": "ORDER",
  "transactionId": "inicis_tid_12345",
  "paymentDate": "2025-01-15T10:30:00"
}
```

##### POST /api/payments/cancel
**Description:** Cancel approved payment

##### GET /api/payments
**Description:** Get all payments

##### GET /api/payments/{id}
**Description:** Get payment by ID

## Frontend Implementation

### pages/order/index.vue

**Route:** `/order`

**Description:** Order form with payment initiation

**Key Features:**
- Member selection dropdown
- Product selection with quantity
- Point usage input
- PG selection (INICIS, NICEPAY, WEIGHTED)
- Net cancel test checkbox
- Total amount calculation

**Flow:**
1. User fills order form
2. Click "결제하기" button
3. Generate order number via API
4. Call /api/payments/initiate
5. Open payment popup with PG parameters
6. Popup handles PG payment
7. After success, create order

### pages/order/popup.vue

**Route:** `/order/popup`

**Description:** PG payment popup window

**Features:**
- Receives payment parameters via PostMessage
- Loads PG SDK (INICIS or NicePay)
- Submits payment form to PG
- Receives payment result
- Sends result back to parent window
- Opens progress-popup on success

**Window Sizes:**
- INICIS: 840×600
- NICEPAY: 570×830

**PostMessage Protocol:**
```javascript
// Parent → Popup
{
  type: 'PAYMENT_PARAMS',
  data: {
    paymentId, mid, oid, price, goodName, signature, ...
  }
}

// Popup → Parent
{
  type: 'PAYMENT_RESULT',
  data: {
    success: true,
    authToken, tid, orderId, ...
  }
}
```

### plugins/inicis.client.ts

**Purpose:** Load INICIS SDK

**Function:**
```typescript
export function initInicisSDK() {
  const script = document.createElement('script')
  script.src = 'https://stgstdpay.inicis.com/stdjs/INIStdPay.js'
  script.charset = 'UTF-8'
  document.head.appendChild(script)
}

export function payWithInicis(params) {
  INIStdPay.pay(params)
}
```

### plugins/nicepay.client.ts

**Purpose:** Load NicePay SDK and handle form submission

**Function:**
```typescript
export function initNicepaySDK() {
  const script = document.createElement('script')
  script.src = 'https://web.nicepay.co.kr/v3/webstd/js/nicepay-3.0.js'
  document.head.appendChild(script)
}

export function payWithNicepay(params) {
  const form = createPaymentForm(params)
  document.body.appendChild(form)
  form.submit()
}
```

## Testing Criteria

### API Testing

1. **Initiate INICIS Payment**
   ```bash
   POST /api/payments/initiate
   { "pgCompany": "INICIS", ... }
   ```

2. **Initiate NICEPAY Payment**
   ```bash
   POST /api/payments/initiate
   { "pgCompany": "NICEPAY", ... }
   ```

3. **Initiate Weighted Payment**
   ```bash
   POST /api/payments/initiate
   { "pgCompany": "WEIGHTED", ... }
   # Verify selectedPgCompany in response
   ```

4. **Confirm Card Payment**
   ```bash
   POST /api/payments/confirm
   { "paymentMethod": "CREDIT_CARD", ... }
   ```

5. **Confirm Point Payment**
   ```bash
   POST /api/payments/confirm
   { "paymentMethod": "POINT", ... }
   ```

### Frontend Testing

1. Navigate to /order
2. Select member and products
3. Select INICIS → Verify popup size 840×600
4. Complete test payment
5. Verify payment success
6. Repeat with NICEPAY → Verify popup size 570×830
7. Test weighted selection → Verify random PG selection
8. Test point payment
9. Verify all payment logs created

## Success Criteria

- [ ] Factory pattern correctly routes to processors/adapters
- [ ] INICIS integration works with test credentials
- [ ] NICEPAY integration works with test credentials
- [ ] Weighted PG selection follows configured ratios
- [ ] Point payment deducts from balance
- [ ] All PG requests logged in payment_interface_request_log
- [ ] Payment popup communicates via PostMessage
- [ ] Different popup sizes for different PGs
- [ ] Payment initiate/confirm APIs functional
- [ ] Error handling for all failure scenarios

## File Structure

```
vibe-pay-backend/src/main/java/com/vibe/pay/backend/
├── payment/
│   ├── Payment.java (Phase 1)
│   ├── PaymentMapper.java (Phase 1)
│   ├── PaymentService.java (NEW)
│   ├── PaymentController.java (NEW)
│   ├── dto/
│   │   ├── PaymentInitiateRequest.java (Phase 1)
│   │   ├── PaymentInitResponse.java (Phase 1)
│   │   ├── PaymentConfirmRequest.java (Phase 1)
│   │   ├── PaymentNetCancelRequest.java (Phase 1)
│   │   └── ... (PG-specific DTOs)
│   ├── factory/
│   │   ├── PaymentProcessorFactory.java (NEW)
│   │   └── PaymentGatewayFactory.java (NEW)
│   ├── gateway/
│   │   ├── PaymentGatewayAdapter.java (NEW - interface)
│   │   ├── InicisAdapter.java (NEW)
│   │   ├── NicePayAdapter.java (NEW)
│   │   └── TossAdapter.java (NEW)
│   ├── processor/
│   │   ├── PaymentProcessor.java (NEW - interface)
│   │   ├── CreditCardPaymentProcessor.java (NEW)
│   │   └── PointPaymentProcessor.java (NEW)
│   └── PgWeightSelector.java (NEW)
├── paymentlog/
│   ├── PaymentInterfaceRequestLog.java (Phase 1)
│   ├── PaymentInterfaceRequestLogMapper.java (Phase 1)
│   ├── PaymentInterfaceRequestLogService.java (NEW)
│   └── PaymentInterfaceRequestLogController.java (NEW)
└── util/
    ├── HashUtils.java (NEW)
    └── WebClientUtil.java (NEW)

vibe-pay-frontend/
├── pages/order/
│   ├── index.vue (NEW - order form)
│   └── popup.vue (NEW - payment popup)
└── plugins/
    ├── inicis.client.ts (NEW)
    └── nicepay.client.ts (NEW)
```

## Notes

- Most complex domain with ~20 files
- Design patterns are critical for maintainability
- Each PG has different API structure
- Test credentials provided in application.yml
- Popup-based payment required for SSR compatibility
- PostMessage enables parent-child communication
- Payment logs enable debugging and compliance
- Weighted selection uses simple random distribution
- Point payment bypasses PG entirely
- Net cancel handled by processors
