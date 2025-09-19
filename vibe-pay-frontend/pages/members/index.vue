<template>
  <div class="page-container">
    <!-- 헤더 -->
    <div class="page-header">
      <v-btn icon @click="goBack" class="back-btn">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <h1 class="page-title">회원 정보</h1>
    </div>

    <!-- 컨텐츠 -->
    <v-card class="content-card">
      <v-card-title class="d-flex align-center">
        회원 목록
        <v-spacer></v-spacer>
        <v-btn color="primary" to="/members/new">새 회원 추가</v-btn>
      </v-card-title>
    <v-data-table
      :headers="headers"
      :items="members"
      :loading="loading"
      class="elevation-1"
      @click:row="goToMemberDetail"
    >
      <template v-slot:loading>
        <v-skeleton-loader type="table-row@5"></v-skeleton-loader>
      </template>
      <template v-slot:item.actions="{ item }">
        <v-btn small color="primary" @click.stop="goToOrderForm(item.memberId)">Order</v-btn>
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
const members = ref([])

const headers = [
  { title: 'ID', key: 'memberId', align: 'start' },
  { title: 'Name', key: 'name' },
  { title: 'Address', key: 'shippingAddress' },
  { title: 'Phone Number', key: 'phoneNumber' },
  { title: 'Registered At', key: 'createdAt' },
  { title: 'Actions', key: 'actions', sortable: false },
]

const fetchMembers = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/members')
    if (!response.ok) {
      throw new Error('Failed to fetch members')
    }
    const data = await response.json()
    members.value = data.map(m => ({...m, createdAt: new Date(m.createdAt).toLocaleString()}))
  } catch (error) {
    console.error(error)
    // You could add user-facing error handling here
  } finally {
    loading.value = false
  }
}

const goToMemberDetail = (event, { item }) => {
  router.push(`/members/${item.memberId}`)
}

const goToOrderForm = (memberId) => {
  router.push(`/order?memberId=${memberId}`)
}

const goBack = () => {
  router.go(-1)
}

onMounted(() => {
  fetchMembers()
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