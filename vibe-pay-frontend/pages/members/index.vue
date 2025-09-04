<template>
  <v-card>
    <v-card-title class="d-flex align-center">
      Members
      <v-spacer></v-spacer>
      <v-btn color="primary" to="/members/new">New Member</v-btn>
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
        <v-btn small color="primary" @click.stop="goToOrderForm(item.id)">Order</v-btn>
      </template>
    </v-data-table>
  </v-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(true)
const members = ref([])

const headers = [
  { title: 'ID', key: 'id', align: 'start' },
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
  router.push(`/members/${item.id}`)
}

const goToOrderForm = (memberId) => {
  router.push(`/order?memberId=${memberId}`)
}

onMounted(() => {
  fetchMembers()
})
</script>

<style scoped>
.v-data-table :deep(tbody tr) {
  cursor: pointer;
}
</style>