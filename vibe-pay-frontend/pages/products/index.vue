<template>
  <div class="page-container">
    <!-- 헤더 -->
    <div class="page-header">
      <v-btn icon @click="goBack" class="back-btn">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <h1 class="page-title">상품 정보</h1>
    </div>

    <!-- 컨텐츠 -->
    <v-card class="content-card">
      <v-card-title class="d-flex align-center">
        상품 목록
        <v-spacer></v-spacer>
        <v-btn color="primary" to="/products/new">새 상품 추가</v-btn>
      </v-card-title>
      <v-data-table
        :headers="headers"
        :items="products"
        :loading="loading"
        class="elevation-1"
        @click:row="goToProductDetail"
      >
        <template v-slot:loading>
          <v-skeleton-loader type="table-row@5"></v-skeleton-loader>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(true)
const products = ref([])

const headers = [
  { title: 'ID', key: 'productId', align: 'start' },
  { title: 'Name', key: 'name' },
  { title: 'Price', key: 'price' },
]

const fetchProducts = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/products')
    if (!response.ok) {
      throw new Error('Failed to fetch products')
    }
    products.value = await response.json()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const goToProductDetail = (event, { item }) => {
  router.push(`/products/${item.productId}`)
}

const goBack = () => {
  router.go(-1)
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background-color: #fafafa;
}

.page-header {
  background: white;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-btn {
  margin-right: 12px;
}

.page-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.content-card {
  margin: 20px;
  border-radius: 16px !important;
}

.v-data-table :deep(tbody tr) {
  cursor: pointer;
}
</style>