# Phase 2-1: Member Domain Requirements

## Overview
Member domain manages user information and serves as the foundation for orders, payments, and reward points. This is the simplest domain and should be implemented first to establish patterns for subsequent domains.

## Prerequisites
- Phase 1 Data Layer completed
- Member, MemberMapper, MemberRequest, MemberResponse classes available

## Backend Implementation

### MemberService.java

**Location:** `com.vibe.pay.backend.member.MemberService`

**Dependencies:**
- MemberMapper
- RewardPointsMapper (for automatic reward points creation)

**Methods:**

#### 1. createMember
```java
@Transactional
public Member createMember(MemberRequest request)
```
**Business Logic:**
- Validate member name is not empty
- Check if email already exists (if provided)
- Create Member entity
- Automatically create RewardPoints entity with 0 initial points
- Return created member

**Exceptions:**
- `IllegalArgumentException` if name is empty
- `DuplicateEmailException` if email already exists

#### 2. getMemberById
```java
public Optional<Member> getMemberById(Long memberId)
```
**Business Logic:**
- Query member by ID
- Return Optional.empty() if not found

#### 3. getAllMembers
```java
public List<Member> getAllMembers()
```
**Business Logic:**
- Return all members ordered by created_at DESC

#### 4. updateMember
```java
@Transactional
public Member updateMember(Long memberId, MemberRequest request)
```
**Business Logic:**
- Verify member exists
- Update member fields
- Return updated member

**Exceptions:**
- `MemberNotFoundException` if member doesn't exist

#### 5. deleteMember
```java
@Transactional
public void deleteMember(Long memberId)
```
**Business Logic:**
- Verify member exists
- Check if member has active orders (prevent deletion if exists)
- Delete member

**Exceptions:**
- `MemberNotFoundException` if member doesn't exist
- `MemberHasOrdersException` if member has orders

### MemberController.java

**Location:** `com.vibe.pay.backend.member.MemberController`

**Base Path:** `/api/members`

#### API Endpoints

##### GET /api/members
**Description:** Get all members

**Response:** `200 OK`
```json
[
  {
    "memberId": 1,
    "name": "현준",
    "shippingAddress": "서울시 강남구",
    "phoneNumber": "010-1234-5678",
    "email": "test@test.com",
    "createdAt": "2025-01-15T10:30:00"
  }
]
```

##### GET /api/members/{id}
**Description:** Get member by ID

**Path Variable:** `id` (Long)

**Response:** `200 OK`
```json
{
  "memberId": 1,
  "name": "현준",
  "shippingAddress": "서울시 강남구",
  "phoneNumber": "010-1234-5678",
  "email": "test@test.com",
  "createdAt": "2025-01-15T10:30:00"
}
```

**Error:** `404 NOT FOUND` if member not found

##### POST /api/members
**Description:** Create new member

**Request Body:**
```json
{
  "name": "현준",
  "shippingAddress": "서울시 강남구",
  "phoneNumber": "010-1234-5678",
  "email": "test@test.com"
}
```

**Response:** `201 CREATED`
```json
{
  "memberId": 1,
  "name": "현준",
  "shippingAddress": "서울시 강남구",
  "phoneNumber": "010-1234-5678",
  "email": "test@test.com",
  "createdAt": "2025-01-15T10:30:00"
}
```

**Validation:**
- name: required, not empty
- email: optional, valid email format
- phoneNumber: optional

##### PUT /api/members/{id}
**Description:** Update member information

**Path Variable:** `id` (Long)

**Request Body:**
```json
{
  "name": "홍길동",
  "shippingAddress": "서울시 서초구",
  "phoneNumber": "010-9876-5432",
  "email": "updated@test.com"
}
```

**Response:** `200 OK`
```json
{
  "memberId": 1,
  "name": "홍길동",
  "shippingAddress": "서울시 서초구",
  "phoneNumber": "010-9876-5432",
  "email": "updated@test.com",
  "createdAt": "2025-01-15T10:30:00"
}
```

**Error:** `404 NOT FOUND` if member not found

##### DELETE /api/members/{id}
**Description:** Delete member

**Path Variable:** `id` (Long)

**Response:** `204 NO CONTENT`

**Error:**
- `404 NOT FOUND` if member not found
- `409 CONFLICT` if member has active orders

## Frontend Implementation

### pages/members/index.vue

**Route:** `/members`

**Description:** Member list page with CRUD operations

#### UI Components

##### 1. Page Header
- Title: "회원 관리"
- Subtitle: "회원 목록 및 정보 관리"
- Back button to navigate to home

##### 2. Create Member Button
- Floating action button (FAB) or header button
- Icon: `mdi-account-plus`
- Opens create member dialog

##### 3. Member Table (Vuetify DataTable)
**Columns:**
- Member ID
- Name
- Email
- Phone Number
- Created At
- Actions (View, Edit, Delete)

**Features:**
- Sortable columns
- Search/filter by name or email
- Pagination (10 items per page)
- Responsive design

##### 4. Create/Edit Member Dialog
**Form Fields:**
- Name (required)
- Email (optional, email validation)
- Phone Number (optional)
- Shipping Address (optional)

**Actions:**
- Save button
- Cancel button

**Validation:**
- Name must not be empty
- Email must be valid format (if provided)

##### 5. Delete Confirmation Dialog
- Warning message
- Confirm/Cancel buttons
- Show error if member has orders

#### Data Flow

```javascript
<script setup>
import { ref, onMounted } from 'vue'

const members = ref([])
const loading = ref(false)
const dialog = ref(false)
const deleteDialog = ref(false)
const editedMember = ref({
  name: '',
  email: '',
  phoneNumber: '',
  shippingAddress: ''
})
const memberToDelete = ref(null)
const isEditMode = ref(false)

const fetchMembers = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/members')
    members.value = await response.json()
  } catch (error) {
    console.error(error)
    alert('회원 목록 조회 실패')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEditMode.value = false
  editedMember.value = {
    name: '',
    email: '',
    phoneNumber: '',
    shippingAddress: ''
  }
  dialog.value = true
}

const openEditDialog = (member) => {
  isEditMode.value = true
  editedMember.value = { ...member }
  dialog.value = true
}

const saveMember = async () => {
  const url = isEditMode.value
    ? `/api/members/${editedMember.value.memberId}`
    : '/api/members'

  const method = isEditMode.value ? 'PUT' : 'POST'

  try {
    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editedMember.value)
    })

    if (response.ok) {
      dialog.value = false
      fetchMembers()
    } else {
      alert('저장 실패')
    }
  } catch (error) {
    console.error(error)
    alert('저장 중 오류 발생')
  }
}

const openDeleteDialog = (member) => {
  memberToDelete.value = member
  deleteDialog.value = true
}

const deleteMember = async () => {
  try {
    const response = await fetch(`/api/members/${memberToDelete.value.memberId}`, {
      method: 'DELETE'
    })

    if (response.ok) {
      deleteDialog.value = false
      fetchMembers()
    } else if (response.status === 409) {
      alert('주문 내역이 있는 회원은 삭제할 수 없습니다.')
    } else {
      alert('삭제 실패')
    }
  } catch (error) {
    console.error(error)
    alert('삭제 중 오류 발생')
  }
}

onMounted(() => {
  fetchMembers()
})
</script>
```

#### Styling
- Card-based layout
- Primary color: #0064FF
- Responsive design for mobile
- Clean and modern UI with Vuetify components

### pages/members/[id].vue

**Route:** `/members/:id`

**Description:** Member detail page

#### UI Components

##### 1. Page Header
- Back button
- Title: Member name
- Edit button

##### 2. Member Information Card
**Display Fields:**
- Member ID (read-only)
- Name
- Email
- Phone Number
- Shipping Address
- Created At (read-only)

##### 3. Reward Points Section (Placeholder for Task 2-3)
- Display current points
- Link to point history
- Note: Full implementation in Task 2-3

##### 4. Order History Section (Placeholder for Task 2-5)
- Link to view orders
- Note: Full implementation in Task 2-5

#### Data Flow

```javascript
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const member = ref(null)
const loading = ref(false)

const fetchMember = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/members/${route.params.id}`)
    if (response.ok) {
      member.value = await response.json()
    } else if (response.status === 404) {
      alert('회원을 찾을 수 없습니다.')
      router.push('/members')
    }
  } catch (error) {
    console.error(error)
    alert('회원 정보 조회 실패')
  } finally {
    loading.value = false
  }
}

const goToEdit = () => {
  router.push(`/members/${route.params.id}/edit`)
}

onMounted(() => {
  fetchMember()
})
</script>
```

## Testing Criteria

### Unit Tests (Optional)
- MemberService CRUD operations
- Email validation logic
- Member creation with reward points

### Integration Tests

#### API Testing (Postman/cURL)
1. **Create Member**
   ```bash
   POST /api/members
   {
     "name": "테스트",
     "email": "test@test.com",
     "phoneNumber": "010-1234-5678",
     "shippingAddress": "서울시 강남구"
   }
   ```

2. **Get All Members**
   ```bash
   GET /api/members
   ```

3. **Get Member by ID**
   ```bash
   GET /api/members/1
   ```

4. **Update Member**
   ```bash
   PUT /api/members/1
   {
     "name": "수정된이름",
     "email": "updated@test.com"
   }
   ```

5. **Delete Member**
   ```bash
   DELETE /api/members/1
   ```

#### Frontend Testing (Browser)
1. Navigate to `/members`
2. Click "Create Member" button
3. Fill form and submit
4. Verify member appears in table
5. Click edit icon
6. Update member information
7. Verify changes saved
8. Click delete icon
9. Confirm deletion
10. Verify member removed from table

## Success Criteria

- [ ] MemberService implements all 5 methods
- [ ] MemberController provides all 5 endpoints
- [ ] Creating member automatically creates RewardPoints entry
- [ ] Email validation works correctly
- [ ] Cannot delete member with active orders
- [ ] pages/members/index.vue displays member list
- [ ] Can create, edit, delete members via UI
- [ ] pages/members/[id].vue shows member details
- [ ] Responsive design works on mobile
- [ ] All error cases handled properly

## Dependencies

### Backend
- Phase 1 Data Layer (Member, MemberMapper)
- Spring Boot Web
- Spring Transaction Management
- Lombok

### Frontend
- Nuxt.js 3
- Vuetify 3
- Vue Router

## Implementation Notes

### Backend
- Use `@Transactional` for create and update operations
- Implement proper exception handling
- Log all operations for debugging
- Return appropriate HTTP status codes

### Frontend
- Use Vuetify data table components
- Implement loading states
- Show error messages clearly
- Use confirmation dialogs for destructive actions
- Cache member list to reduce API calls
- Debounce search input

## API Contract

### Error Response Format
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Member not found with id 1",
  "path": "/api/members/1"
}
```

### Success Response Format
All successful responses return the entity or list of entities directly without wrapping.

## File Locations

### Backend
```
vibe-pay-backend/src/main/java/com/vibe/pay/backend/member/
├── Member.java (from Phase 1)
├── MemberMapper.java (from Phase 1)
├── MemberRequest.java (from Phase 1)
├── MemberResponse.java (from Phase 1)
├── MemberService.java (NEW)
└── MemberController.java (NEW)
```

### Frontend
```
vibe-pay-frontend/pages/members/
├── index.vue (NEW)
└── [id].vue (NEW)
```

## Notes
- This is the simplest domain - establish patterns here
- Reward points are created automatically on member creation
- Member cannot be deleted if they have orders
- All fields except name are optional
- Created timestamp is auto-generated
- Use consistent error handling across all endpoints
