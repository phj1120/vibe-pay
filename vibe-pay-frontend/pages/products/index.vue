<template>
  <v-card>
    <v-card-title class="d-flex align-center">
      Products
      <v-spacer></v-spacer>
      <v-btn color="primary" to="/products/new">New Product</v-btn>
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

// Mock API call
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

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.v-data-table :deep(tbody tr) {
  cursor: pointer;
}
</style>
