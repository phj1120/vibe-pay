# Phase 2-2: Product Domain Requirements

## Overview
Product domain manages product catalog and inventory. Products are referenced in orders and display their information throughout the payment flow.

## Prerequisites
- Phase 1 Data Layer completed
- Product, ProductMapper, ProductRequest, ProductResponse classes available

## Backend Implementation

### ProductService.java

**Location:** `com.vibe.pay.backend.product.ProductService`

**Dependencies:**
- ProductMapper

**Methods:**

#### 1. createProduct
```java
@Transactional
public Product createProduct(ProductRequest request)
```
**Business Logic:**
- Validate product name is not empty
- Validate price is positive
- Create Product entity
- Return created product

**Exceptions:**
- `IllegalArgumentException` if name is empty or price is negative/zero

#### 2. getProductById
```java
public Optional<Product> getProductById(Long productId)
```
**Business Logic:**
- Query product by ID
- Return Optional.empty() if not found

#### 3. getAllProducts
```java
public List<Product> getAllProducts()
```
**Business Logic:**
- Return all products ordered by product_id ASC

#### 4. updateProduct
```java
@Transactional
public Product updateProduct(Long productId, ProductRequest request)
```
**Business Logic:**
- Verify product exists
- Validate new price is positive
- Update product fields
- Return updated product

**Exceptions:**
- `ProductNotFoundException` if product doesn't exist
- `IllegalArgumentException` if price is invalid

#### 5. deleteProduct
```java
@Transactional
public void deleteProduct(Long productId)
```
**Business Logic:**
- Verify product exists
- Check if product is referenced in any orders
- Delete product

**Exceptions:**
- `ProductNotFoundException` if product doesn't exist
- `ProductInUseException` if product is referenced in orders

### ProductController.java

**Location:** `com.vibe.pay.backend.product.ProductController`

**Base Path:** `/api/products`

#### API Endpoints

##### GET /api/products
**Description:** Get all products

**Response:** `200 OK`
```json
[
  {
    "productId": 1,
    "name": "상품1",
    "price": 10000.0
  },
  {
    "productId": 2,
    "name": "상품2",
    "price": 25000.0
  }
]
```

##### GET /api/products/{id}
**Description:** Get product by ID

**Path Variable:** `id` (Long)

**Response:** `200 OK`
```json
{
  "productId": 1,
  "name": "상품1",
  "price": 10000.0
}
```

**Error:** `404 NOT FOUND` if product not found

##### POST /api/products
**Description:** Create new product

**Request Body:**
```json
{
  "name": "신규 상품",
  "price": 15000.0
}
```

**Response:** `201 CREATED`
```json
{
  "productId": 3,
  "name": "신규 상품",
  "price": 15000.0
}
```

**Validation:**
- name: required, not empty
- price: required, must be > 0

##### PUT /api/products/{id}
**Description:** Update product information

**Path Variable:** `id` (Long)

**Request Body:**
```json
{
  "name": "수정된 상품",
  "price": 20000.0
}
```

**Response:** `200 OK`
```json
{
  "productId": 1,
  "name": "수정된 상품",
  "price": 20000.0
}
```

**Error:** `404 NOT FOUND` if product not found

##### DELETE /api/products/{id}
**Description:** Delete product

**Path Variable:** `id` (Long)

**Response:** `204 NO CONTENT`

**Error:**
- `404 NOT FOUND` if product not found
- `409 CONFLICT` if product is in use

## Frontend Implementation

### pages/products/index.vue

**Route:** `/products`

**Description:** Product catalog page with CRUD operations

#### UI Components

##### 1. Page Header
- Title: "상품 관리"
- Subtitle: "상품 목록 및 정보 관리"
- Back button to navigate to home

##### 2. Create Product Button
- Floating action button (FAB) or header button
- Icon: `mdi-plus`
- Opens create product dialog

##### 3. Product Grid (Vuetify Card Grid)
**Card Contents:**
- Product image (placeholder or default image)
- Product name
- Price (formatted with thousand separator: ₩10,000)
- Edit button
- Delete button

**Layout:**
- Grid layout (3 columns on desktop, 2 on tablet, 1 on mobile)
- Card hover effect
- Responsive design

##### 4. Create/Edit Product Dialog
**Form Fields:**
- Name (required)
- Price (required, number input)

**Actions:**
- Save button
- Cancel button

**Validation:**
- Name must not be empty
- Price must be greater than 0

##### 5. Delete Confirmation Dialog
- Warning message
- Confirm/Cancel buttons
- Show error if product is in use

#### Data Flow

```javascript
<script setup>
import { ref, onMounted } from 'vue'

const products = ref([])
const loading = ref(false)
const dialog = ref(false)
const deleteDialog = ref(false)
const editedProduct = ref({
  name: '',
  price: 0
})
const productToDelete = ref(null)
const isEditMode = ref(false)

const fetchProducts = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/products')
    products.value = await response.json()
  } catch (error) {
    console.error(error)
    alert('상품 목록 조회 실패')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEditMode.value = false
  editedProduct.value = {
    name: '',
    price: 0
  }
  dialog.value = true
}

const openEditDialog = (product) => {
  isEditMode.value = true
  editedProduct.value = { ...product }
  dialog.value = true
}

const saveProduct = async () => {
  if (!editedProduct.value.name || editedProduct.value.price <= 0) {
    alert('상품명과 가격을 올바르게 입력해주세요.')
    return
  }

  const url = isEditMode.value
    ? `/api/products/${editedProduct.value.productId}`
    : '/api/products'

  const method = isEditMode.value ? 'PUT' : 'POST'

  try {
    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editedProduct.value)
    })

    if (response.ok) {
      dialog.value = false
      fetchProducts()
    } else {
      alert('저장 실패')
    }
  } catch (error) {
    console.error(error)
    alert('저장 중 오류 발생')
  }
}

const openDeleteDialog = (product) => {
  productToDelete.value = product
  deleteDialog.value = true
}

const deleteProduct = async () => {
  try {
    const response = await fetch(`/api/products/${productToDelete.value.productId}`, {
      method: 'DELETE'
    })

    if (response.ok) {
      deleteDialog.value = false
      fetchProducts()
    } else if (response.status === 409) {
      alert('주문 내역이 있는 상품은 삭제할 수 없습니다.')
    } else {
      alert('삭제 실패')
    }
  } catch (error) {
    console.error(error)
    alert('삭제 중 오류 발생')
  }
}

const formatPrice = (price) => {
  return `₩${price.toLocaleString()}`
}

onMounted(() => {
  fetchProducts()
})
</script>
```

#### Styling
- Card-based grid layout
- Success color for product cards: #00C896
- Hover effect: box-shadow and slight elevation
- Price highlighted in primary color
- Default product image placeholder
- Clean and modern UI with Vuetify components

#### Template Structure
```vue
<template>
  <div class="products-page">
    <!-- Page Header -->
    <div class="page-header">
      <v-btn icon @click="$router.go(-1)">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <h1 class="page-title">상품 관리</h1>
    </div>

    <!-- Products Grid -->
    <div class="products-container">
      <div class="products-grid">
        <v-card
          v-for="product in products"
          :key="product.productId"
          class="product-card"
          elevation="2"
          hover
        >
          <div class="product-image">
            <v-icon size="64" color="grey-lighten-2">mdi-package-variant</v-icon>
          </div>
          <v-card-title>{{ product.name }}</v-card-title>
          <v-card-subtitle class="product-price">
            {{ formatPrice(product.price) }}
          </v-card-subtitle>
          <v-card-actions>
            <v-btn
              icon
              size="small"
              @click="openEditDialog(product)"
            >
              <v-icon>mdi-pencil</v-icon>
            </v-btn>
            <v-btn
              icon
              size="small"
              color="error"
              @click="openDeleteDialog(product)"
            >
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </v-card-actions>
        </v-card>
      </div>
    </div>

    <!-- FAB -->
    <v-btn
      class="fab"
      color="success"
      icon
      size="large"
      @click="openCreateDialog"
    >
      <v-icon>mdi-plus</v-icon>
    </v-btn>

    <!-- Create/Edit Dialog -->
    <!-- Delete Dialog -->
  </div>
</template>
```

### pages/products/[id].vue

**Route:** `/products/:id`

**Description:** Product detail page

#### UI Components

##### 1. Page Header
- Back button
- Title: Product name
- Edit button

##### 2. Product Information Card
**Display Fields:**
- Product image (large placeholder)
- Product ID (read-only)
- Name
- Price (formatted)

##### 3. Order History Section (Placeholder)
- List of orders containing this product
- Note: Full implementation in Task 2-5

#### Data Flow

```javascript
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const loading = ref(false)

const fetchProduct = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/products/${route.params.id}`)
    if (response.ok) {
      product.value = await response.json()
    } else if (response.status === 404) {
      alert('상품을 찾을 수 없습니다.')
      router.push('/products')
    }
  } catch (error) {
    console.error(error)
    alert('상품 정보 조회 실패')
  } finally {
    loading.value = false
  }
}

const formatPrice = (price) => {
  return `₩${price.toLocaleString()}`
}

onMounted(() => {
  fetchProduct()
})
</script>
```

## Testing Criteria

### Integration Tests

#### API Testing (Postman/cURL)
1. **Create Product**
   ```bash
   POST /api/products
   {
     "name": "테스트 상품",
     "price": 15000.0
   }
   ```

2. **Get All Products**
   ```bash
   GET /api/products
   ```

3. **Get Product by ID**
   ```bash
   GET /api/products/1
   ```

4. **Update Product**
   ```bash
   PUT /api/products/1
   {
     "name": "수정된 상품",
     "price": 20000.0
   }
   ```

5. **Delete Product**
   ```bash
   DELETE /api/products/1
   ```

#### Frontend Testing (Browser)
1. Navigate to `/products`
2. Verify products displayed in grid
3. Click FAB to create product
4. Fill form and submit
5. Verify product appears in grid
6. Click edit icon on product card
7. Update product information
8. Verify changes saved
9. Click delete icon
10. Confirm deletion
11. Verify product removed from grid

## Success Criteria

- [ ] ProductService implements all 5 methods
- [ ] ProductController provides all 5 endpoints
- [ ] Price validation prevents negative values
- [ ] Cannot delete product referenced in orders
- [ ] pages/products/index.vue displays product grid
- [ ] Can create, edit, delete products via UI
- [ ] pages/products/[id].vue shows product details
- [ ] Price formatting works correctly (₩10,000)
- [ ] Responsive grid layout works on all devices
- [ ] All error cases handled properly

## Dependencies

### Backend
- Phase 1 Data Layer (Product, ProductMapper)
- Spring Boot Web
- Spring Transaction Management
- Lombok

### Frontend
- Nuxt.js 3
- Vuetify 3
- Vue Router

## Implementation Notes

### Backend
- Use `@Transactional` for create, update, delete operations
- Validate price is always positive
- Return appropriate HTTP status codes
- Log all operations

### Frontend
- Use Vuetify card components for product display
- Implement grid layout with responsive breakpoints
- Show price with thousand separator
- Use default product image placeholder
- Implement loading states
- Cache product list to reduce API calls

## File Locations

### Backend
```
vibe-pay-backend/src/main/java/com/vibe/pay/backend/product/
├── Product.java (from Phase 1)
├── ProductMapper.java (from Phase 1)
├── ProductRequest.java (from Phase 1)
├── ProductResponse.java (from Phase 1)
├── ProductService.java (NEW)
└── ProductController.java (NEW)
```

### Frontend
```
vibe-pay-frontend/pages/products/
├── index.vue (NEW)
└── [id].vue (NEW)
```

## Notes
- All prices use Double type for precision
- Price formatting: ₩ symbol with thousand separator
- Product images are placeholders (icon-based)
- Grid layout adjusts to screen size
- Products cannot be deleted if referenced in orders
- Simple domain similar to Member domain
