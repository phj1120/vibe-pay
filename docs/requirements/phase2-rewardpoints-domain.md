# Phase 2-3: RewardPoints Domain Requirements

## Overview
RewardPoints domain manages member points (mileage) including earning, using, refunding, and tracking transaction history. Every point change is logged in point_history for auditability.

## Prerequisites
- Phase 1 Data Layer completed
- Phase 2-1 Member domain completed
- RewardPoints, PointHistory, Mappers, DTOs available

## Backend Implementation

### RewardPointsService.java

**Location:** `com.vibe.pay.backend.rewardpoints.RewardPointsService`

**Dependencies:**
- RewardPointsMapper
- PointHistoryService

**Methods:**

#### 1. createRewardPoints
```java
@Transactional
public RewardPoints createRewardPoints(RewardPoints rewardPoints)
```
**Business Logic:**
- Set lastUpdated to current timestamp
- Insert reward points record
- Initial points default to 0
- Return created reward points

**Note:** Usually called automatically when creating a member

#### 2. getRewardPointsById
```java
public Optional<RewardPoints> getRewardPointsById(Long rewardPointsId)
```
**Business Logic:**
- Query reward points by ID
- Return Optional.empty() if not found

#### 3. getRewardPointsByMemberId
```java
public RewardPoints getRewardPointsByMemberId(Long memberId)
```
**Business Logic:**
- Query reward points by member ID
- Return null if not found

**Note:** Each member has exactly one RewardPoints record

#### 4. addPoints (Earn)
```java
@Transactional
public RewardPoints addPoints(Long memberId, Long pointsToAdd)
```
**Business Logic:**
- Get existing RewardPoints for member
- If not exists, create new with initial points
- Add points to balance
- Update lastUpdated timestamp
- Record transaction in PointHistory (EARN type)
- Return updated RewardPoints

**Exceptions:**
- `IllegalArgumentException` if pointsToAdd is negative

#### 5. usePoints
```java
@Transactional
public RewardPoints usePoints(Long memberId, Long pointsToUse)
```
**Business Logic:**
- Get existing RewardPoints for member
- Verify sufficient balance
- Deduct points from balance
- Update lastUpdated timestamp
- Record transaction in PointHistory (USE type)
- Return updated RewardPoints

**Exceptions:**
- `InsufficientPointsException` if balance < pointsToUse
- `MemberNotFoundException` if reward points not found

#### 6. refundPoints
```java
@Transactional
public RewardPoints refundPoints(Long memberId, Long pointsToRefund)
```
**Business Logic:**
- Get existing RewardPoints for member
- Add points back to balance
- Update lastUpdated timestamp
- Record transaction in PointHistory (REFUND type)
- Return updated RewardPoints

**Exceptions:**
- `MemberNotFoundException` if reward points not found

### PointHistoryService.java

**Location:** `com.vibe.pay.backend.pointhistory.PointHistoryService`

**Dependencies:**
- PointHistoryMapper

**Methods:**

#### 1. recordPointEarn
```java
@Transactional
public void recordPointEarn(Long memberId, Long pointAmount, String referenceType, String referenceId, String description)
```
**Business Logic:**
- Create PointHistory record
- Set transactionType = EARN
- Set balanceAfter (current balance + pointAmount)
- Set createdAt to current timestamp
- Insert record

#### 2. recordPointUse
```java
@Transactional
public void recordPointUse(Long memberId, Long pointAmount, String referenceType, String referenceId, String description)
```
**Business Logic:**
- Create PointHistory record
- Set transactionType = USE
- Set pointAmount as negative value
- Set balanceAfter (current balance - pointAmount)
- Set createdAt to current timestamp
- Insert record

#### 3. recordPointRefund
```java
@Transactional
public void recordPointRefund(Long memberId, Long pointAmount, String referenceType, String referenceId, String description)
```
**Business Logic:**
- Create PointHistory record
- Set transactionType = REFUND
- Set balanceAfter (current balance + pointAmount)
- Set createdAt to current timestamp
- Insert record

#### 4. getHistoryByMemberId
```java
public List<PointHistory> getHistoryByMemberId(Long memberId)
```
**Business Logic:**
- Query all point history for member
- Order by createdAt DESC (newest first)
- Return list

#### 5. getStatisticsByMemberId
```java
public PointStatistics getStatisticsByMemberId(Long memberId)
```
**Business Logic:**
- Calculate total earned points (sum of EARN transactions)
- Calculate total used points (sum of USE transactions)
- Calculate total refunded points (sum of REFUND transactions)
- Return statistics object

**PointStatistics DTO:**
```java
@Getter
@Setter
public class PointStatistics {
    private Long totalEarned;
    private Long totalUsed;
    private Long totalRefunded;
    private Long currentBalance;
}
```

### RewardPointsController.java

**Location:** `com.vibe.pay.backend.rewardpoints.RewardPointsController`

**Base Path:** `/api/rewardpoints` or `/api/members/{memberId}/points`

#### API Endpoints

##### GET /api/rewardpoints/member/{memberId}
**Description:** Get reward points for member

**Path Variable:** `memberId` (Long)

**Response:** `200 OK`
```json
{
  "rewardPointsId": 1,
  "memberId": 1,
  "points": 10000,
  "lastUpdated": "2025-01-15T10:30:00"
}
```

**Error:** `404 NOT FOUND` if member not found

##### POST /api/rewardpoints/member/{memberId}/earn
**Description:** Add points to member balance

**Path Variable:** `memberId` (Long)

**Request Body:**
```json
{
  "pointAmount": 5000,
  "description": "수동 적립"
}
```

**Response:** `200 OK`
```json
{
  "rewardPointsId": 1,
  "memberId": 1,
  "points": 15000,
  "lastUpdated": "2025-01-15T10:35:00"
}
```

**Validation:**
- pointAmount: required, must be > 0

##### POST /api/rewardpoints/member/{memberId}/use
**Description:** Use points from member balance

**Path Variable:** `memberId` (Long)

**Request Body:**
```json
{
  "pointAmount": 3000,
  "description": "상품 구매"
}
```

**Response:** `200 OK`
```json
{
  "rewardPointsId": 1,
  "memberId": 1,
  "points": 12000,
  "lastUpdated": "2025-01-15T10:40:00"
}
```

**Error:** `409 CONFLICT` if insufficient balance

##### GET /api/pointhistory/member/{memberId}
**Description:** Get point transaction history for member

**Path Variable:** `memberId` (Long)

**Response:** `200 OK`
```json
[
  {
    "pointHistoryId": 3,
    "memberId": 1,
    "pointAmount": -3000,
    "balanceAfter": 12000,
    "transactionType": "USE",
    "referenceType": "PAYMENT",
    "referenceId": "20250115P00000001",
    "description": "상품 구매",
    "createdAt": "2025-01-15T10:40:00"
  },
  {
    "pointHistoryId": 2,
    "memberId": 1,
    "pointAmount": 5000,
    "balanceAfter": 15000,
    "transactionType": "EARN",
    "referenceType": "MANUAL_CHARGE",
    "referenceId": "1",
    "description": "수동 적립",
    "createdAt": "2025-01-15T10:35:00"
  }
]
```

##### GET /api/pointhistory/member/{memberId}/statistics
**Description:** Get point statistics for member

**Path Variable:** `memberId` (Long)

**Response:** `200 OK`
```json
{
  "totalEarned": 50000,
  "totalUsed": 30000,
  "totalRefunded": 5000,
  "currentBalance": 25000
}
```

## Frontend Implementation

### Enhancement to pages/members/[id].vue

Add reward points section to existing member detail page.

#### UI Components to Add

##### 1. Points Balance Card
**Display:**
- Current points balance (large, highlighted)
- Last updated timestamp
- Icon: `mdi-star`

##### 2. Points Actions
**Buttons:**
- "적립" (Earn Points) button
- "사용" (Use Points) button - for testing
- Links to point history

##### 3. Points History Section
**Table Columns:**
- Date/Time
- Transaction Type (badge: EARN=success, USE=error, REFUND=warning)
- Amount (positive/negative with sign)
- Balance After
- Description
- Reference ID (if available)

**Features:**
- Pagination (10 items per page)
- Filter by transaction type
- Date range filter (optional)

##### 4. Earn Points Dialog
**Form Fields:**
- Point Amount (number input, required, > 0)
- Description (text input, optional)

**Actions:**
- Earn button
- Cancel button

##### 5. Use Points Dialog (for testing)
**Form Fields:**
- Point Amount (number input, required, > 0)
- Description (text input, optional)

**Actions:**
- Use button
- Cancel button

**Validation:**
- Amount must not exceed current balance

#### Data Flow Enhancement

```javascript
<script setup>
// ... existing member detail code ...

const rewardPoints = ref(null)
const pointHistory = ref([])
const pointStatistics = ref(null)
const earnDialog = ref(false)
const useDialog = ref(false)
const pointAmount = ref(0)
const pointDescription = ref('')

const fetchRewardPoints = async () => {
  try {
    const response = await fetch(`/api/rewardpoints/member/${route.params.id}`)
    if (response.ok) {
      rewardPoints.value = await response.json()
    }
  } catch (error) {
    console.error(error)
  }
}

const fetchPointHistory = async () => {
  try {
    const response = await fetch(`/api/pointhistory/member/${route.params.id}`)
    if (response.ok) {
      pointHistory.value = await response.json()
    }
  } catch (error) {
    console.error(error)
  }
}

const fetchPointStatistics = async () => {
  try {
    const response = await fetch(`/api/pointhistory/member/${route.params.id}/statistics`)
    if (response.ok) {
      pointStatistics.value = await response.json()
    }
  } catch (error) {
    console.error(error)
  }
}

const openEarnDialog = () => {
  pointAmount.value = 0
  pointDescription.value = ''
  earnDialog.value = true
}

const earnPoints = async () => {
  if (pointAmount.value <= 0) {
    alert('적립할 포인트를 입력해주세요.')
    return
  }

  try {
    const response = await fetch(`/api/rewardpoints/member/${route.params.id}/earn`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        pointAmount: pointAmount.value,
        description: pointDescription.value || '수동 적립'
      })
    })

    if (response.ok) {
      earnDialog.value = false
      fetchRewardPoints()
      fetchPointHistory()
      fetchPointStatistics()
      alert('포인트가 적립되었습니다.')
    } else {
      alert('포인트 적립 실패')
    }
  } catch (error) {
    console.error(error)
    alert('포인트 적립 중 오류 발생')
  }
}

const openUseDialog = () => {
  pointAmount.value = 0
  pointDescription.value = ''
  useDialog.value = true
}

const usePoints = async () => {
  if (pointAmount.value <= 0) {
    alert('사용할 포인트를 입력해주세요.')
    return
  }

  if (pointAmount.value > rewardPoints.value.points) {
    alert('보유 포인트가 부족합니다.')
    return
  }

  try {
    const response = await fetch(`/api/rewardpoints/member/${route.params.id}/use`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        pointAmount: pointAmount.value,
        description: pointDescription.value || '수동 사용'
      })
    })

    if (response.ok) {
      useDialog.value = false
      fetchRewardPoints()
      fetchPointHistory()
      fetchPointStatistics()
      alert('포인트가 사용되었습니다.')
    } else if (response.status === 409) {
      alert('포인트가 부족합니다.')
    } else {
      alert('포인트 사용 실패')
    }
  } catch (error) {
    console.error(error)
    alert('포인트 사용 중 오류 발생')
  }
}

const getTransactionTypeColor = (type) => {
  switch (type) {
    case 'EARN': return 'success'
    case 'USE': return 'error'
    case 'REFUND': return 'warning'
    default: return 'grey'
  }
}

const formatAmount = (amount) => {
  const sign = amount >= 0 ? '+' : ''
  return `${sign}${amount.toLocaleString()}P`
}

onMounted(() => {
  // ... existing fetch calls ...
  fetchRewardPoints()
  fetchPointHistory()
  fetchPointStatistics()
})
</script>
```

#### Template Addition

```vue
<template>
  <!-- ... existing member detail content ... -->

  <!-- Reward Points Section -->
  <div class="section-card">
    <div class="section-header">
      <v-icon color="warning" class="mr-3">mdi-star</v-icon>
      <h3>포인트 정보</h3>
    </div>

    <!-- Points Balance -->
    <div class="points-balance">
      <div class="balance-amount">{{ rewardPoints?.points?.toLocaleString() || 0 }}P</div>
      <div class="balance-label">보유 포인트</div>
      <div class="balance-updated">최종 업데이트: {{ rewardPoints?.lastUpdated }}</div>
    </div>

    <!-- Points Actions -->
    <div class="points-actions">
      <v-btn color="success" @click="openEarnDialog">
        <v-icon left>mdi-plus</v-icon>
        적립
      </v-btn>
      <v-btn color="error" @click="openUseDialog">
        <v-icon left>mdi-minus</v-icon>
        사용
      </v-btn>
    </div>

    <!-- Points Statistics -->
    <div class="points-stats" v-if="pointStatistics">
      <div class="stat-item">
        <span class="stat-label">총 적립</span>
        <span class="stat-value success">+{{ pointStatistics.totalEarned?.toLocaleString() }}P</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">총 사용</span>
        <span class="stat-value error">-{{ pointStatistics.totalUsed?.toLocaleString() }}P</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">총 환불</span>
        <span class="stat-value warning">+{{ pointStatistics.totalRefunded?.toLocaleString() }}P</span>
      </div>
    </div>
  </div>

  <!-- Point History Section -->
  <div class="section-card">
    <div class="section-header">
      <v-icon color="info" class="mr-3">mdi-history</v-icon>
      <h3>포인트 내역</h3>
    </div>

    <v-data-table
      :items="pointHistory"
      :headers="pointHistoryHeaders"
      :items-per-page="10"
      class="elevation-0"
    >
      <template v-slot:item.transactionType="{ item }">
        <v-chip
          :color="getTransactionTypeColor(item.transactionType)"
          size="small"
          label
        >
          {{ item.transactionType }}
        </v-chip>
      </template>

      <template v-slot:item.pointAmount="{ item }">
        <span :class="item.pointAmount >= 0 ? 'text-success' : 'text-error'">
          {{ formatAmount(item.pointAmount) }}
        </span>
      </template>

      <template v-slot:item.balanceAfter="{ item }">
        {{ item.balanceAfter.toLocaleString() }}P
      </template>
    </v-data-table>
  </div>

  <!-- Earn Points Dialog -->
  <!-- Use Points Dialog -->
</template>
```

## Testing Criteria

### Integration Tests

#### API Testing (Postman/cURL)
1. **Get Reward Points**
   ```bash
   GET /api/rewardpoints/member/1
   ```

2. **Earn Points**
   ```bash
   POST /api/rewardpoints/member/1/earn
   {
     "pointAmount": 5000,
     "description": "수동 적립"
   }
   ```

3. **Use Points**
   ```bash
   POST /api/rewardpoints/member/1/use
   {
     "pointAmount": 3000,
     "description": "테스트 사용"
   }
   ```

4. **Get Point History**
   ```bash
   GET /api/pointhistory/member/1
   ```

5. **Get Point Statistics**
   ```bash
   GET /api/pointhistory/member/1/statistics
   ```

#### Frontend Testing (Browser)
1. Navigate to member detail page
2. Verify current points balance displayed
3. Click "적립" button
4. Enter amount and submit
5. Verify balance increased
6. Verify new entry in point history
7. Click "사용" button
8. Enter amount less than balance
9. Verify balance decreased
10. Verify statistics updated

## Success Criteria

- [ ] RewardPointsService implements all methods
- [ ] PointHistoryService logs all transactions
- [ ] Cannot use more points than balance
- [ ] All point changes are recorded in history
- [ ] Point statistics calculate correctly
- [ ] Member detail page shows points section
- [ ] Can earn/use points via UI
- [ ] Point history displays correctly
- [ ] Transaction types color-coded
- [ ] All error cases handled

## Dependencies

### Backend
- Phase 1 Data Layer
- Phase 2-1 Member domain
- Spring Boot Web
- Spring Transaction Management

### Frontend
- Phase 2-1 Member pages
- Vuetify 3 components

## Implementation Notes

### Backend
- Every point change MUST be logged in point_history
- Use transactions to ensure atomicity
- Validate balance before deducting points
- referenceType examples: PAYMENT, CANCEL, MANUAL, MANUAL_CHARGE
- referenceId stores related entity ID (paymentId, orderId, etc.)

### Frontend
- Display points with "P" suffix
- Use color coding: green (earn), red (use), orange (refund)
- Show positive amounts with "+" prefix
- Format numbers with thousand separator
- Real-time balance updates after transactions

## File Locations

### Backend
```
vibe-pay-backend/src/main/java/com/vibe/pay/backend/
├── rewardpoints/
│   ├── RewardPoints.java (from Phase 1)
│   ├── RewardPointsMapper.java (from Phase 1)
│   ├── RewardPointsRequest.java (from Phase 1)
│   ├── RewardPointsResponse.java (from Phase 1)
│   ├── RewardPointsService.java (NEW)
│   └── RewardPointsController.java (NEW)
└── pointhistory/
    ├── PointHistory.java (from Phase 1)
    ├── PointHistoryMapper.java (from Phase 1)
    ├── PointStatistics.java (NEW)
    ├── PointHistoryService.java (NEW)
    └── PointHistoryController.java (NEW)
```

### Frontend
```
vibe-pay-frontend/pages/members/
└── [id].vue (ENHANCED with points section)
```

## Notes
- Points are stored as Long (no decimals)
- All point transactions are immutable (no updates/deletes)
- Point history provides full audit trail
- RewardPoints automatically created with Member
- Balance calculations can be verified from point_history
- Transaction descriptions are for user reference only
