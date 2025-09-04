<template>
  <v-card>
    <v-card-title>{{ pageTitle }}</v-card-title>
    <v-card-text>
      <v-form @submit.prevent="saveMember">
        <v-text-field v-model="member.name" label="Name" :rules="[rules.required]"></v-text-field>
        <v-text-field v-model="member.address" label="Address"></v-text-field>
        <v-text-field v-model="member.phoneNumber" label="Phone Number"></v-text-field>

        <v-divider class="my-4"></v-divider>

        <div v-if="!isNewMember">
          <h3 class="text-h6 mb-2">Points</h3>
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field v-model.number="pointsToAdd" label="Add Points" type="number"></v-text-field>
            </v-col>
            <v-col cols="12" md="6">
              <v-btn @click="addPoints" color="secondary">Add Points</v-btn>
            </v-col>
          </v-row>
        </div>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn type="submit" color="primary">Save</v-btn>
          <v-btn to="/members">Cancel</v-btn>
        </v-card-actions>
      </v-form>
    </v-card-text>
  </v-card>

    <v-card class="mt-4" v-if="!isNewMember">
    <v-card-title>Order History</v-card-title>
    <v-data-table
      :headers="orderHeaders"
      :items="orders"
      :loading="ordersLoading"
    >
      <template v-slot:item.actions="{ item }">
        <v-btn 
          v-if="item.status === 'PAID'"
          color="error"
          size="small"
          @click="cancelOrder(item.orderId)"
        >
          Cancel
        </v-btn>
      </template>
    </v-data-table>
  </v-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const member = ref({ name: '', address: '', phoneNumber: '' })
const pointsToAdd = ref(0)
const orders = ref([])
const ordersLoading = ref(false)

const orderHeaders = [
  { title: 'Order ID', key: 'orderId' },
  { title: 'Date', key: 'orderDate' },
  { title: 'Total', key: 'totalAmount' },
  { title: 'Status', key: 'status' },
  { title: 'Actions', key: 'actions', sortable: false },
]

const isNewMember = computed(() => route.params.id === 'new')
const pageTitle = computed(() => isNewMember.value ? 'New Member' : 'Edit Member')

const rules = {
  required: value => !!value || 'Required.',
}

const fetchMemberData = async (id) => {
  try {
    const response = await fetch(`/api/members/${id}`)
    if (!response.ok) throw new Error('Failed to fetch member data')
    member.value = await response.json()
  } catch (error) {
    console.error(error)
    router.push('/members')
  }
}

const fetchOrderHistory = async (memberId) => {
  ordersLoading.value = true
  try {
    const response = await fetch(`/api/orders/member/${memberId}`)
    if (!response.ok) throw new Error('Failed to fetch orders')
    const data = await response.json()
    orders.value = data.map(o => ({...o, orderDate: new Date(o.orderDate).toLocaleString()}))
  } catch (error) {
    console.error(error)
  } finally {
    ordersLoading.value = false
  }
}

const saveMember = async () => {
  try {
    const url = isNewMember.value ? '/api/members' : `/api/members/${route.params.id}`;
    const method = isNewMember.value ? 'POST' : 'PUT';
    const response = await fetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(member.value),
    });
    if (!response.ok) throw new Error('Failed to save member');
    router.push('/members');
  } catch (error) {
    console.error(error);
  }
}

const addPoints = async () => {
  if (pointsToAdd.value > 0) {
    try {
      const response = await fetch(`/api/members/${route.params.id}/points`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amount: pointsToAdd.value, reason: 'Manual grant' })
      });
      if (!response.ok) throw new Error('Failed to add points');
      alert('Successfully added points!');
      pointsToAdd.value = 0;
      // Optionally, refresh member data to show updated total points
    } catch (error) {
      console.error(error);
      alert('Error adding points.');
    }
  }
}

const cancelOrder = async (orderId) => {
  if (!confirm('Are you sure you want to cancel this order?')) return;
  try {
    const response = await fetch(`/api/orders/${orderId}/cancel`, { method: 'POST' });
    if (!response.ok) throw new Error('Failed to cancel order');
    await fetchOrderHistory(route.params.id);
  } catch (error) {
    console.error(error);
    alert('Error cancelling order.');
  }
}

onMounted(() => {
  if (!isNewMember.value) {
    fetchMemberData(route.params.id)
    fetchOrderHistory(route.params.id)
  }
})
</script>
