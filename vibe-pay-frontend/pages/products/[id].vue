<template>
  <v-card>
    <v-card-title>{{ pageTitle }}</v-card-title>
    <v-card-text>
      <v-form @submit.prevent="saveProduct">
        <v-text-field v-model="product.name" label="Product Name" :rules="[rules.required]"></v-text-field>
        <v-text-field v-model.number="product.price" label="Price" type="number" prefix="$" :rules="[rules.required]"></v-text-field>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn type="submit" color="primary">Save</v-btn>
          <v-btn to="/products">Cancel</v-btn>
        </v-card-actions>
      </v-form>
    </v-card-text>
  </v-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const product = ref({
  name: '',
  price: 0
})

const isNewProduct = computed(() => route.params.id === 'new')
const pageTitle = computed(() => isNewProduct.value ? 'New Product' : 'Edit Product')

const rules = {
  required: value => !!value || 'Required.',
}

const fetchProductData = async (id) => {
  try {
    const response = await fetch(`/api/products/${id}`)
    if (!response.ok) {
      throw new Error('Failed to fetch product data')
    }
    product.value = await response.json()
  } catch (error) {
    console.error(error)
    router.push('/products') // Redirect if product not found
  }
}

const saveProduct = async () => {
  try {
    const url = isNewProduct.value ? '/api/products' : `/api/products/${route.params.id}`;
    const method = isNewProduct.value ? 'POST' : 'PUT';

    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(product.value),
    });

    if (!response.ok) {
      throw new Error('Failed to save product');
    }

    router.push('/products');
  } catch (error) {
    console.error(error);
  }
}

onMounted(() => {
  if (!isNewProduct.value) {
    fetchProductData(route.params.id)
  }
})
</script>
