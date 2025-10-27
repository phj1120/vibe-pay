# Test Scenarios

## Overview
Comprehensive test scenarios covering all VibePay functionality including payment flows, order management, point system, and error handling. Each scenario includes preconditions, steps, and expected results.

## Prerequisites

### Initial Data Loaded
- ✓ 3 test members (현준, 민지, 지훈)
- ✓ 10 products (various price ranges)
- ✓ Initial reward points configured
- ✓ PG test credentials in application.yml

### Services Running
- ✓ Backend: http://localhost:8080
- ✓ Frontend: http://localhost:3000
- ✓ PostgreSQL database

## Scenario 1: INICIS 카드 결제 성공

### Objective
Complete successful card payment through INICIS gateway

### Preconditions
- Member: 현준 (ID: 1)
- Product: 무선 마우스 (₩25,000)
- PG: INICIS

### Steps

#### Backend (API)
1. **Generate Order Number**
   ```bash
   GET /api/orders/generateOrderNumber
   ```
   **Expected:** `20250115O00000001` (17 chars)

2. **Initiate Payment**
   ```bash
   POST /api/payments/initiate
   {
     "memberId": 1,
     "orderId": "20250115O00000001",
     "amount": 25000,
     "paymentMethod": "CREDIT_CARD",
     "pgCompany": "INICIS",
     "goodName": "무선 마우스",
     "buyerName": "현준",
     "buyerTel": "010-1234-5678",
     "buyerEmail": "test@test.com"
   }
   ```
   **Expected:**
   - `200 OK`
   - Response includes: paymentId, mid, signature, selectedPgCompany=INICIS

3. **Confirm Payment** (after PG approval)
   ```bash
   POST /api/payments/confirm
   {
     "authToken": "test_token",
     "orderId": "20250115O00000001",
     "price": 25000,
     "mid": "INIpayTest",
     "memberId": 1,
     "paymentMethod": "CREDIT_CARD",
     "pgCompany": "INICIS",
     "txTid": "test_tid_12345"
   }
   ```
   **Expected:**
   - `200 OK`
   - Payment status: APPROVED
   - Payment record saved

4. **Create Order**
   ```bash
   POST /api/orders
   {
     "orderNumber": "20250115O00000001",
     "memberId": 1,
     "items": [{ "productId": 4, "quantity": 1 }],
     "paymentMethods": [{
       "paymentMethod": "CREDIT_CARD",
       "pgCompany": "INICIS",
       "amount": 25000,
       "authToken": "test_token",
       "txTid": "test_tid_12345"
     }]
   }
   ```
   **Expected:**
   - `200 OK`
   - Order created with status: ORDERED

#### Frontend (Browser)
1. Navigate to `/order`
2. Select member: 현준
3. Select product: 무선 마우스
4. Select PG: INICIS
5. Click "결제하기"
6. **Verify:** Payment popup opens (840×600)
7. Complete test payment in popup
8. **Verify:** progress-popup appears
9. **Verify:** Redirected to `/order/complete`
10. **Verify:** Order details displayed correctly

### Expected Results
- ✓ Payment approved via INICIS
- ✓ Payment record in database (status: APPROVED)
- ✓ Order created (status: ORDERED)
- ✓ OrderItem created
- ✓ PaymentInterfaceRequestLog entries created
- ✓ UI shows success page

## Scenario 2: NICEPAY 카드 결제 성공

### Objective
Complete successful card payment through NicePay gateway

### Preconditions
- Member: 민지 (ID: 2)
- Product: 키보드 (₩35,000)
- PG: NICEPAY

### Steps
Same as Scenario 1, but:
- Change `pgCompany: "NICEPAY"`
- Use member 민지
- **Verify:** Popup size is 570×830 (different from INICIS)

### Expected Results
- ✓ Payment approved via NICEPAY
- ✓ Payment record (status: APPROVED, pgCompany: NICEPAY)
- ✓ Order created successfully
- ✓ Different popup dimensions

## Scenario 3: 포인트 결제 성공

### Objective
Complete payment using only reward points

### Preconditions
- Member: 현준 (ID: 1, points: 50,000)
- Product: 텀블러 (₩5,000)
- Payment Method: POINT

### Steps

#### Backend
1. **Check Points**
   ```bash
   GET /api/rewardpoints/member/1
   ```
   **Expected:** `{ points: 50000 }`

2. **Initiate Payment**
   ```bash
   POST /api/payments/initiate
   {
     "memberId": 1,
     "orderId": "20250115O00000002",
     "amount": 5000,
     "paymentMethod": "POINT",
     "pgCompany": null
   }
   ```

3. **Confirm Payment**
   ```bash
   POST /api/payments/confirm
   {
     "orderId": "20250115O00000002",
     "memberId": 1,
     "paymentMethod": "POINT",
     "price": 5000
   }
   ```
   **Expected:**
   - Payment approved
   - Points deducted: 50,000 → 45,000

4. **Create Order**
   ```bash
   POST /api/orders
   {
     "orderNumber": "20250115O00000002",
     "memberId": 1,
     "items": [{ "productId": 1, "quantity": 1 }],
     "paymentMethods": [{
       "paymentMethod": "POINT",
       "amount": 5000
     }]
   }
   ```

5. **Verify Point History**
   ```bash
   GET /api/pointhistory/member/1
   ```
   **Expected:**
   - New USE record with pointAmount: -5000
   - balanceAfter: 45000
   - referenceType: PAYMENT

#### Frontend
1. Navigate to `/order`
2. Select member: 현준
3. Select product: 텀블러 (₩5,000)
4. Enter used points: 5,000
5. Click "결제하기"
6. **No popup** (point payment bypasses PG)
7. Order created directly
8. **Verify:** Complete page shows point payment
9. Navigate to `/members/1`
10. **Verify:** Points decreased to 45,000
11. **Verify:** Point history shows USE transaction

### Expected Results
- ✓ Payment processed without PG
- ✓ Points deducted correctly
- ✓ PointHistory record created
- ✓ Order created
- ✓ No PG popup shown

## Scenario 4: 혼합 결제 (포인트 + 카드)

### Objective
Complete payment using both points and card

### Preconditions
- Member: 현준 (ID: 1, points: 45,000 after Scenario 3)
- Products: 모니터 (₩180,000) + 마우스패드 (₩3,000)
- Total: ₩183,000
- Payment: 45,000 points + 138,000 card (INICIS)

### Steps
1. Navigate to `/order`
2. Select member: 현준
3. Add products:
   - 모니터 × 1
   - 마우스패드 × 1
4. Enter used points: 45,000
5. Select PG: INICIS
6. Click "결제하기"
7. Complete card payment for ₩138,000
8. **Verify:** Order created with 2 payment records
9. **Verify:** Points balance = 0
10. **Verify:** Order total = ₩183,000

### Expected Results
- ✓ 2 Payment records created (POINT + CREDIT_CARD)
- ✓ Points: 45,000 → 0
- ✓ Card payment: ₩138,000 approved
- ✓ Order with 2 items created
- ✓ Order detail shows both payments

## Scenario 5: 가중치 기반 PG 선택

### Objective
Test weighted PG selection randomness

### Preconditions
- Configuration (application.yml):
  ```yaml
  payment:
    weight:
      inicis: 50
      nicepay: 50
  ```

### Steps
1. Repeat 10 times:
   - Select pgCompany: "WEIGHTED"
   - Call POST /api/payments/initiate
   - Record selectedPgCompany from response
2. Count INICIS vs NICEPAY selections
3. **Expected distribution:** ~50% each (±20% variance acceptable)

### Expected Results
- ✓ selectedPgCompany returned in response
- ✓ Random selection follows weight ratio
- ✓ Both INICIS and NICEPAY selected
- ✓ Popup size adjusts based on actual selected PG

## Scenario 6: 망취소 (Net Cancel) - 주문 생성 실패

### Objective
Test automatic payment cancellation when order creation fails

### Preconditions
- Member: 민지 (ID: 2)
- Product: 무선 마우스 (₩25,000)
- PG: INICIS
- Special: Set `netCancel: true` to trigger failure

### Steps

#### Backend
1. **Initiate & Confirm Payment** (same as Scenario 1)
   **Result:** Payment APPROVED

2. **Create Order with Net Cancel Flag**
   ```bash
   POST /api/orders
   {
     "orderNumber": "20250115O00000003",
     "memberId": 2,
     "items": [{ "productId": 4, "quantity": 1 }],
     "paymentMethods": [...],
     "netCancel": true  // ← Triggers failure
   }
   ```
   **Expected:**
   - `500 INTERNAL_SERVER_ERROR`
   - Order creation fails

3. **Verify Payment Cancelled**
   ```bash
   GET /api/payments/20250115P00000003
   ```
   **Expected:**
   - Payment status: CANCELLED (not APPROVED)

4. **Verify Net Cancel Logged**
   ```bash
   GET /api/paymentlog
   ```
   **Expected:**
   - Log entry with requestType: "netCancel"

#### Frontend
1. Complete payment flow with netCancel flag
2. **Verify:** Redirected to `/order/failed`
3. **Verify:** Error message displayed
4. Navigate to payment list
5. **Verify:** Payment shows CANCELLED status

### Expected Results
- ✓ Payment approved initially
- ✓ Order creation fails (intentional)
- ✓ Automatic net cancel triggered
- ✓ Payment status changed to CANCELLED
- ✓ PaymentInterfaceRequestLog shows net cancel
- ✓ Points refunded (if point payment)
- ✓ User redirected to failed page

## Scenario 7: 주문 취소

### Objective
Cancel existing order and refund payments

### Preconditions
- Existing order from Scenario 1: `20250115O00000001`
- Order status: ORDERED
- Payment approved

### Steps

#### Backend
1. **Get Order Details**
   ```bash
   GET /api/orders/20250115O00000001
   ```
   **Expected:** Order with ord_proc_seq = 1

2. **Cancel Order**
   ```bash
   DELETE /api/orders/20250115O00000001
   ```
   **Expected:**
   - `200 OK`
   - Cancel order created

3. **Verify Cancel Records**
   ```bash
   GET /api/orders/20250115O00000001
   ```
   **Expected:**
   - 2 sets of records (ord_proc_seq 1 and 2)
   - ord_proc_seq=2 has negative amounts
   - claim_id generated

4. **Verify Payment Refunded**
   ```bash
   GET /api/payments?orderId=20250115O00000001
   ```
   **Expected:**
   - Original payment: pay_type=PAYMENT
   - Refund payment: pay_type=REFUND

5. **Verify Points Refunded** (if used)
   ```bash
   GET /api/rewardpoints/member/1
   GET /api/pointhistory/member/1
   ```
   **Expected:**
   - Points restored
   - REFUND transaction in history

#### Frontend
1. Navigate to member detail page
2. Find completed order
3. Click "취소" button
4. Confirm cancellation
5. **Verify:** Cancel records appear
6. **Verify:** Negative amounts shown
7. **Verify:** Points refunded (if applicable)

### Expected Results
- ✓ Cancel orders created (ord_proc_seq=2)
- ✓ Negative amounts and quantities
- ✓ claim_id generated
- ✓ Payments refunded
- ✓ Points refunded (if used)
- ✓ Cannot cancel same order twice

## Scenario 8: 회원 CRUD

### Objective
Test complete member lifecycle

### Steps
1. **Create Member**
   ```bash
   POST /api/members
   {
     "name": "테스트회원",
     "email": "new@test.com",
     "phoneNumber": "010-9999-9999"
   }
   ```
   **Expected:**
   - Member created
   - RewardPoints automatically created with 0 points

2. **Verify RewardPoints Created**
   ```bash
   GET /api/rewardpoints/member/{newMemberId}
   ```
   **Expected:** `{ points: 0 }`

3. **Update Member**
   ```bash
   PUT /api/members/{id}
   {
     "name": "수정된이름",
     "email": "updated@test.com"
   }
   ```

4. **Delete Member (Success)**
   ```bash
   DELETE /api/members/{id}
   ```
   **Expected:** `204 NO CONTENT`

5. **Delete Member with Orders (Fail)**
   ```bash
   DELETE /api/members/1  # 현준 has orders
   ```
   **Expected:** `409 CONFLICT`

### Expected Results
- ✓ Member CRUD operations work
- ✓ RewardPoints auto-created
- ✓ Email uniqueness enforced
- ✓ Cannot delete member with orders

## Scenario 9: 상품 CRUD

### Objective
Test complete product lifecycle

### Steps
1. **Create Product**
   ```bash
   POST /api/products
   {
     "name": "신규상품",
     "price": 15000
   }
   ```

2. **Update Product**
   ```bash
   PUT /api/products/{id}
   {
     "name": "수정된상품",
     "price": 18000
   }
   ```

3. **Delete Product (Success)**
   ```bash
   DELETE /api/products/{id}
   ```
   **Expected:** `204 NO CONTENT`

4. **Delete Product in Orders (Fail)**
   ```bash
   DELETE /api/products/4  # 무선 마우스 in orders
   ```
   **Expected:** `409 CONFLICT`

### Expected Results
- ✓ Product CRUD operations work
- ✓ Price validation (must be > 0)
- ✓ Cannot delete product in orders

## Scenario 10: 포인트 적립/사용/환불

### Objective
Test point system operations

### Steps
1. **Earn Points (Manual)**
   ```bash
   POST /api/rewardpoints/member/2/earn
   {
     "pointAmount": 10000,
     "description": "이벤트 적립"
   }
   ```
   **Expected:**
   - Points increased
   - EARN transaction logged

2. **Use Points**
   ```bash
   POST /api/rewardpoints/member/2/use
   {
     "pointAmount": 5000,
     "description": "수동 사용"
   }
   ```
   **Expected:**
   - Points decreased
   - USE transaction logged

3. **Try Use More Than Balance**
   ```bash
   POST /api/rewardpoints/member/2/use
   {
     "pointAmount": 999999
   }
   ```
   **Expected:** `409 CONFLICT` - Insufficient points

4. **Get Point History**
   ```bash
   GET /api/pointhistory/member/2
   ```
   **Expected:**
   - All transactions listed
   - Correct balanceAfter values

5. **Get Point Statistics**
   ```bash
   GET /api/pointhistory/member/2/statistics
   ```
   **Expected:**
   - totalEarned, totalUsed, totalRefunded
   - currentBalance matches actual balance

### Expected Results
- ✓ Point operations work correctly
- ✓ All transactions logged
- ✓ Cannot use more than balance
- ✓ Statistics calculate correctly

## Summary Checklist

### Payment Flows
- [ ] INICIS card payment
- [ ] NICEPAY card payment
- [ ] Point-only payment
- [ ] Mixed payment (point + card)
- [ ] Weighted PG selection

### Order Management
- [ ] Order creation after payment
- [ ] Multi-item orders
- [ ] Order cancellation
- [ ] Net cancel on failure
- [ ] Order detail retrieval

### Point System
- [ ] Point earning
- [ ] Point usage
- [ ] Point refund
- [ ] Point history tracking
- [ ] Insufficient balance handling

### CRUD Operations
- [ ] Member creation with auto reward points
- [ ] Member update/delete
- [ ] Product creation/update/delete
- [ ] Constraint enforcement (FK checks)

### Error Handling
- [ ] Insufficient points
- [ ] Net cancel scenario
- [ ] Invalid PG selection
- [ ] Duplicate email
- [ ] Delete constraints

### Frontend
- [ ] Payment popup communication
- [ ] Popup size adjustment per PG
- [ ] Progress popup flow
- [ ] Success/failure pages
- [ ] Point display and usage

## Test Execution Notes

### API Testing
- Use Postman or similar tool
- Save test collection for reusability
- Verify response codes and body

### Browser Testing
- Test in Chrome/Firefox
- Check popup blocker settings
- Verify PostMessage communication
- Test responsive design

### Database Verification
- Check actual DB records after each test
- Verify FK relationships
- Confirm transaction logging

### Performance
- Payment flow should complete < 5 seconds
- Order creation < 1 second
- Point operations < 500ms

## Troubleshooting

### Common Issues

**Popup Blocked**
- Allow popups for localhost:3000
- Check browser console for errors

**Payment Fails**
- Verify PG test credentials in application.yml
- Check PaymentInterfaceRequestLog

**Net Cancel Not Working**
- Verify netCancel flag passed correctly
- Check OrderService exception handling

**Points Not Deducted**
- Check RewardPointsService called
- Verify PointHistory transaction

**Order Creation Fails**
- Verify payment approved first
- Check product IDs exist
- Verify member ID exists

## Regression Testing

After any code changes, run:
1. Scenario 1 (INICIS)
2. Scenario 3 (Points)
3. Scenario 6 (Net Cancel)
4. Scenario 7 (Order Cancel)

These 4 scenarios cover critical paths.
