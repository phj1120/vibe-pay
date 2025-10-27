# Phase 2-5: Order Domain Requirements

## Overview
Order domain manages the complete order lifecycle including creation after payment approval, cancellation with payment refunds, and automatic net cancel (망취소) when order creation fails. Uses Command pattern for complex order operations.

## Prerequisites
- Phase 1 Data Layer completed
- Phase 2-4 Payment domain completed
- All previous domains functional

## Architecture Pattern: Command Pattern

### Purpose
- Encapsulate order operations as objects
- Enable rollback capabilities
- Support complex multi-step transactions

### Components
1. **OrderCommand** (interface): Command contract
2. **CreateOrderCommand**: Create order after payment approval
3. **CancelOrderCommand**: Cancel order and refund payments
4. **OrderCommandInvoker**: Execute and rollback commands

## Backend Implementation

### Command Pattern Classes

#### OrderCommand.java (Interface)
```java
package com.vibe.pay.backend.order.command;

public interface OrderCommand {
    List<Order> execute();
    void rollback();
}
```

#### CreateOrderCommand.java
**Location:** `com.vibe.pay.backend.order.command.CreateOrderCommand`

**Purpose:** Create order with multiple items

**Dependencies:**
- OrderMapper
- OrderItemMapper
- ProductService

**Logic:**
1. Validate payment approved
2. Create order records (one per product with ord_seq)
3. Create order_item records
4. Return created orders

**Rollback:**
- Delete created orders
- Delete created order items
- Refund points (if point payment used)

#### CancelOrderCommand.java
**Location:** `com.vibe.pay.backend.order.command.CancelOrderCommand`

**Purpose:** Cancel existing order

**Dependencies:**
- OrderMapper
- OrderItemMapper
- PaymentService
- RewardPointsService

**Logic:**
1. Find original orders (ord_proc_seq = 1)
2. Generate claim_id
3. Create cancel orders (ord_proc_seq = 2) with negative amounts
4. Create cancel order_items with negative quantities
5. Refund payments via PaymentProcessorFactory
6. Return cancel orders

**Rollback:** N/A (cancellation itself is not reversible)

#### OrderCommandInvoker.java
**Location:** `com.vibe.pay.backend.order.command.OrderCommandInvoker`

**Purpose:** Execute commands with error handling

**Methods:**
```java
@Component
public class OrderCommandInvoker {
    public List<Order> invoke(OrderCommand command) {
        try {
            return command.execute();
        } catch (Exception e) {
            command.rollback();
            throw e;
        }
    }
}
```

### OrderService.java

**Location:** `com.vibe.pay.backend.order.OrderService`

**Dependencies:**
- OrderMapper
- OrderItemMapper
- ProductService
- PaymentService
- PaymentProcessorFactory
- PaymentGatewayFactory

**Methods:**

#### 1. generateOrderNumber
```java
public String generateOrderNumber()
```
**Logic:**
- Get current date (YYYYMMDD)
- Get next sequence from order_id_seq
- Format as `YYYYMMDDOXXXXXXXX` (17 chars)
- Return order ID

#### 2. generateClaimNumber
```java
public String generateClaimNumber()
```
**Logic:**
- Get current date (YYYYMMDD)
- Get next sequence from claim_id_seq
- Format as `YYYYMMDDCXXXXXXXX` (17 chars)
- Return claim ID

#### 3. createOrder
```java
@Transactional
public List<Order> createOrder(OrderRequest orderRequest)
```
**Logic:**
1. Validate payments approved
2. Execute payment confirmation for each payment method
3. Create orders for each product (ord_seq = 1, 2, 3...)
4. Create order items
5. **On failure:** Automatic net cancel for all payments
6. Return created orders

**Net Cancel Logic:**
```java
try {
    // Create orders
} catch (Exception e) {
    log.error("Order creation failed, initiating net cancel");

    // Net cancel all payments
    for (PaymentMethodRequest pm : paymentMethods) {
        PaymentProcessor processor = factory.getProcessor(pm.getPaymentMethod());
        processor.netCancel(buildNetCancelRequest(pm));
    }

    throw new OrderException.creationFailed(e.getMessage());
}
```

#### 4. cancelOrder
```java
@Transactional
public Order cancelOrder(String orderId)
```
**Logic:**
1. Find original orders (ord_proc_seq = 1)
2. Verify not already cancelled
3. Generate claim ID
4. Create cancel orders (ord_proc_seq = 2, negative amounts)
5. Create cancel order items (negative quantities)
6. Refund all payments
7. Refund points if used
8. Return first cancel order

**Order Table Structure:**
```
order_id  | ord_seq | ord_proc_seq | total_amount | status
----------|---------|--------------|--------------|----------
20250115O1|    1    |      1       |    10000     | ORDERED
20250115O1|    2    |      1       |    20000     | ORDERED
20250115O1|    1    |      2       |   -10000     | CANCELLED
20250115O1|    2    |      2       |   -20000     | CANCELLED
```

#### 5. getOrderById
```java
public List<Order> getOrderById(String orderId)
```

#### 6. getOrderDetailsByMemberId
```java
public List<OrderDetailDto> getOrderDetailsWithPaymentsByMemberId(Long memberId)
```
**Returns:** Orders with products and payment information

#### 7. getOrderDetailsWithPaging
```java
public List<OrderDetailDto> getOrderDetailsWithPaymentsByMemberIdWithPaging(Long memberId, int page, int size)
```

### OrderController.java

**Location:** `com.vibe.pay.backend.order.OrderController`

**Base Path:** `/api/orders`

#### API Endpoints

##### GET /api/orders/generateOrderNumber
**Description:** Generate new order ID

**Response:** `200 OK`
```
20250115O00000001
```

##### POST /api/orders
**Description:** Create order after payment approval

**Request Body:**
```json
{
  "orderNumber": "20250115O00000001",
  "memberId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ],
  "paymentMethods": [
    {
      "paymentMethod": "POINT",
      "amount": 5000,
      "authToken": "...",
      "pgCompany": "INICIS"
    },
    {
      "paymentMethod": "CREDIT_CARD",
      "pgCompany": "INICIS",
      "amount": 25000,
      "authToken": "token_from_pg",
      "txTid": "inicis_tid_12345",
      "authUrl": "...",
      "netCancelUrl": "...",
      "mid": "INIpayTest"
    }
  ],
  "netCancel": false
}
```

**Response:** `200 OK`
```json
[
  {
    "orderId": "20250115O00000001",
    "ordSeq": 1,
    "ordProcSeq": 1,
    "memberId": 1,
    "orderDate": "2025-01-15T10:30:00",
    "totalAmount": 20000.0,
    "status": "ORDERED"
  },
  {
    "orderId": "20250115O00000001",
    "ordSeq": 2,
    "ordProcSeq": 1,
    "memberId": 1,
    "orderDate": "2025-01-15T10:30:00",
    "totalAmount": 10000.0,
    "status": "ORDERED"
  }
]
```

**Error:** `500 INTERNAL_SERVER_ERROR` if order creation fails (net cancel triggered)

##### GET /api/orders/{orderId}
**Description:** Get order details

**Path Variable:** `orderId` (String)

**Response:** `200 OK`
```json
{
  "orderId": "20250115O00000001",
  "ordSeq": 1,
  "ordProcSeq": 1,
  "memberId": 1,
  "orderDate": "2025-01-15T10:30:00",
  "totalAmount": 30000.0,
  "status": "ORDERED",
  "orderProcesses": [
    { /* original order */ },
    { /* cancel order if cancelled */ }
  ],
  "orderItems": [
    {
      "orderItemId": 1,
      "orderId": "20250115O00000001",
      "productId": 1,
      "productName": "상품1",
      "quantity": 2,
      "priceAtOrder": 10000.0
    }
  ],
  "payments": [
    {
      "paymentId": "20250115P00000001",
      "paymentMethod": "POINT",
      "amount": 5000,
      "status": "APPROVED"
    },
    {
      "paymentId": "20250115P00000002",
      "paymentMethod": "CREDIT_CARD",
      "amount": 25000,
      "pgCompany": "INICIS",
      "status": "APPROVED"
    }
  ]
}
```

##### DELETE /api/orders/{orderId}
**Description:** Cancel order

**Path Variable:** `orderId` (String)

**Response:** `200 OK`
```json
{
  "orderId": "20250115O00000001",
  "ordSeq": 1,
  "ordProcSeq": 2,
  "claimId": "20250115C00000001",
  "memberId": 1,
  "orderDate": "2025-01-15T11:00:00",
  "totalAmount": -20000.0,
  "status": "CANCELLED"
}
```

**Error:** `409 CONFLICT` if already cancelled

##### GET /api/orders/member/{memberId}
**Description:** Get all orders for member

**Path Variable:** `memberId` (Long)

**Query Params:**
- `page` (optional, default 0)
- `size` (optional, default 10)

**Response:** `200 OK`
```json
[
  {
    "orderId": "20250115O00000001",
    "orderDate": "2025-01-15T10:30:00",
    "totalAmount": 30000.0,
    "status": "ORDERED",
    "orderItems": [...],
    "payments": [...]
  }
]
```

## Frontend Implementation

### pages/order/progress-popup.vue

**Route:** `/order/progress-popup`

**Description:** Payment progress and order creation

**Flow:**
1. Receives payment result via URL params
2. Shows loading spinner
3. Calls POST /api/orders to create order
4. On success: redirect parent to /order/complete
5. On failure: redirect parent to /order/failed
6. Close popup

**Key Features:**
- Loading state display
- Automatic order creation
- PostMessage to parent window
- Auto-close after redirect

**Template:**
```vue
<template>
  <div class="progress-popup">
    <v-progress-circular
      indeterminate
      color="primary"
      size="64"
    ></v-progress-circular>
    <h2>주문 생성 중...</h2>
    <p>잠시만 기다려주세요.</p>
  </div>
</template>
```

**Script:**
```javascript
<script setup>
import { onMounted } from 'vue'

onMounted(async () => {
  // Get order data from cookie
  const orderCookie = useCookie('pendingOrder')
  const orderData = JSON.parse(orderCookie.value)

  // Get payment result from URL
  const route = useRoute()
  const paymentResult = {
    authToken: route.query.authToken,
    txTid: route.query.tid,
    // ... other payment params
  }

  // Create order
  try {
    const response = await fetch('/api/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ...orderData,
        paymentMethods: buildPaymentMethods(orderData, paymentResult)
      })
    })

    if (response.ok) {
      const orders = await response.json()
      const orderId = orders[0].orderId

      // Redirect parent to complete page
      window.opener.postMessage({
        type: 'ORDER_COMPLETE',
        orderId: orderId
      }, '*')

      window.opener.location.href = `/order/complete?orderId=${orderId}`
      window.close()
    } else {
      // Redirect to failure page
      window.opener.location.href = '/order/failed'
      window.close()
    }
  } catch (error) {
    console.error(error)
    window.opener.location.href = '/order/failed'
    window.close()
  }
})
</script>
```

### pages/order/complete.vue

**Route:** `/order/complete`

**Description:** Order completion page

**Query Params:** `orderId`

**UI Components:**
1. Success icon
2. Order number
3. Order summary (products, amounts)
4. Payment information
5. "확인" button → home page

**Data Flow:**
```javascript
<script setup>
import { ref, onMounted } from 'vue'

const route = useRoute()
const orderDetail = ref(null)

const fetchOrderDetail = async () => {
  const response = await fetch(`/api/orders/${route.query.orderId}`)
  orderDetail.value = await response.json()
}

onMounted(() => {
  fetchOrderDetail()
})
</script>
```

### pages/order/failed.vue

**Route:** `/order/failed`

**Description:** Order failure page

**Query Params:** `error` (optional)

**UI Components:**
1. Error icon
2. Error message
3. "다시 시도" button → /order
4. "홈으로" button → /

## Testing Criteria

### Integration Tests

#### API Testing

1. **Generate Order Number**
   ```bash
   GET /api/orders/generateOrderNumber
   # Verify format: YYYYMMDDOXXXXXXXX
   ```

2. **Create Order (Success)**
   ```bash
   POST /api/orders
   { orderNumber: "...", items: [...], paymentMethods: [...] }
   # Verify orders created
   # Verify order_items created
   # Verify payments confirmed
   ```

3. **Create Order (Failure - Net Cancel)**
   ```bash
   POST /api/orders
   { ..., netCancel: true }
   # Verify order creation fails
   # Verify payments cancelled (net cancel)
   # Verify points refunded
   ```

4. **Get Order Details**
   ```bash
   GET /api/orders/20250115O00000001
   # Verify includes orderItems and payments
   ```

5. **Cancel Order**
   ```bash
   DELETE /api/orders/20250115O00000001
   # Verify cancel orders created (ord_proc_seq=2)
   # Verify payments refunded
   # Verify points refunded
   ```

### Frontend Testing

1. Complete full payment flow from /order
2. Verify progress-popup appears
3. Verify redirect to complete page
4. Verify order details displayed
5. Test net cancel scenario (set netCancel=true)
6. Verify redirect to failed page
7. Test order cancellation from member page

## Success Criteria

- [ ] Command pattern implemented correctly
- [ ] Order creation after payment works
- [ ] Net cancel triggers on order creation failure
- [ ] Net cancel refunds all payments
- [ ] Net cancel refunds points
- [ ] Order cancellation creates negative records
- [ ] Cancelled orders trigger payment refunds
- [ ] progress-popup creates order automatically
- [ ] complete page shows order details
- [ ] failed page handles errors gracefully
- [ ] All error scenarios handled

## File Structure

```
vibe-pay-backend/src/main/java/com/vibe/pay/backend/order/
├── Order.java (Phase 1)
├── OrderItem.java (Phase 1)
├── OrderMapper.java (Phase 1)
├── OrderItemMapper.java (Phase 1)
├── OrderRequest.java (Phase 1)
├── OrderItemRequest.java (Phase 1)
├── PaymentMethodRequest.java (Phase 1)
├── OrderDetailDto.java (Phase 1)
├── OrderItemDto.java (Phase 1)
├── OrderService.java (NEW)
├── OrderController.java (NEW)
└── command/
    ├── OrderCommand.java (NEW - interface)
    ├── CreateOrderCommand.java (NEW)
    ├── CancelOrderCommand.java (NEW)
    └── OrderCommandInvoker.java (NEW)

vibe-pay-frontend/pages/order/
├── index.vue (from Phase 2-4)
├── popup.vue (from Phase 2-4)
├── progress-popup.vue (NEW)
├── complete.vue (NEW)
└── failed.vue (NEW)
```

## Notes

- Orders use composite primary key (order_id, ord_seq, ord_proc_seq)
- ord_seq: Item sequence within order (1, 2, 3...)
- ord_proc_seq: Processing sequence (1=original, 2=cancel)
- Cancelled orders have negative amounts and quantities
- Net cancel is automatic when order creation fails
- All payment refunds go through PaymentProcessor
- Command pattern enables complex rollback logic
- progress-popup is intermediary between payment and completion
- PostMessage used for popup-parent communication
