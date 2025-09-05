<template>
  <div class="page-container">
    <!-- 헤더 -->
    <div class="page-header">
      <v-btn icon @click="goBack" class="back-btn">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <h1 class="page-title">{{ pageTitle }}</h1>
    </div>

    <!-- 회원 정보 -->
    <v-card class="content-card">
      <v-card-title class="text-h6">회원 정보</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="saveMember">
          <v-text-field v-model="member.name" label="이름" :rules="[rules.required]" variant="outlined"></v-text-field>
          <v-text-field v-model="member.address" label="주소" variant="outlined"></v-text-field>
          <v-text-field v-model="member.phoneNumber" label="전화번호" variant="outlined"></v-text-field>
          <v-text-field v-model="member.email" label="이메일" type="email" variant="outlined"></v-text-field>

          <v-card-actions class="px-0">
            <v-spacer></v-spacer>
            <v-btn type="submit" color="primary" variant="elevated" size="large">저장</v-btn>
            <v-btn @click="goBack" variant="outlined" size="large">취소</v-btn>
          </v-card-actions>
        </v-form>
      </v-card-text>
    </v-card>

    <!-- 마일리지 관리 (기존 회원만) -->
    <v-card class="content-card" v-if="!isNewMember">
      <v-card-title class="text-h6 d-flex align-center">
        <v-icon class="mr-2" color="secondary">mdi-wallet</v-icon>
        마일리지 관리
      </v-card-title>
      <v-card-text>
        <!-- 현재 마일리지 표시 -->
        <v-alert 
          v-if="currentPoints !== null" 
          type="info" 
          variant="tonal" 
          class="mb-4"
        >
          <div class="d-flex align-center">
            <v-icon class="mr-2">mdi-coins</v-icon>
            <span class="text-h6">현재 마일리지: {{ formatNumber(currentPoints) }}원</span>
          </div>
        </v-alert>

        <!-- 마일리지 충전 -->
        <v-row>
          <v-col cols="12" md="8">
            <v-text-field 
              v-model.number="pointsToAdd" 
              label="충전할 마일리지" 
              type="number"
              variant="outlined"
              suffix="원"
              :rules="[rules.required, rules.minAmount]"
            ></v-text-field>
          </v-col>
          <v-col cols="12" md="4" class="d-flex align-center">
            <v-btn 
              @click="addPoints" 
              color="secondary" 
              variant="elevated"
              size="large"
              :disabled="!pointsToAdd || pointsToAdd <= 0"
              :loading="addingPoints"
              block
            >
              <v-icon class="mr-2">mdi-plus</v-icon>
              충전하기
            </v-btn>
          </v-col>
        </v-row>

        <!-- 빠른 충전 버튼들 -->
        <div class="mt-4">
          <v-chip-group>
            <v-chip 
              v-for="amount in quickAmounts"
              :key="amount"
              @click="pointsToAdd = amount"
              variant="outlined"
              color="primary"
            >
              {{ formatNumber(amount) }}원
            </v-chip>
          </v-chip-group>
        </div>
      </v-card-text>
    </v-card>

    <!-- 주문 내역 (기존 회원만) -->
    <v-card class="content-card" v-if="!isNewMember">
      <v-card-title class="text-h6 d-flex align-center">
        <v-icon class="mr-2" color="info">mdi-history</v-icon>
        주문 내역
      </v-card-title>
      <v-card-text>
        <v-data-table
          :headers="orderHeaders"
          :items="orders"
          :loading="ordersLoading"
          no-data-text="주문 내역이 없습니다."
        >
          <template v-slot:item.actions="{ item }">
            <v-btn 
              v-if="item.status === 'PAID'"
              color="error"
              size="small"
              variant="outlined"
              @click="cancelOrder(item.orderId)"
            >
              취소
            </v-btn>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const member = ref({ name: '', address: '', phoneNumber: '', email: '' })
const pointsToAdd = ref(0)
const currentPoints = ref(null)
const addingPoints = ref(false)
const orders = ref([])
const ordersLoading = ref(false)

// 빠른 충전 금액들
const quickAmounts = [1000, 5000, 10000, 30000, 50000, 100000]

const orderHeaders = [
  { title: '주문번호', key: 'orderId' },
  { title: '주문일시', key: 'orderDate' },
  { title: '총금액', key: 'totalAmount' },
  { title: '상태', key: 'status' },
  { title: '액션', key: 'actions', sortable: false },
]

const isNewMember = computed(() => route.params.id === 'new')
const pageTitle = computed(() => isNewMember.value ? '새 회원 등록' : '회원 정보 수정')

const rules = {
  required: value => !!value || '필수 입력 항목입니다.',
  minAmount: value => value > 0 || '0보다 큰 금액을 입력해주세요.',
}

// 숫자 포맷팅
const formatNumber = (value) => {
  return new Intl.NumberFormat('ko-KR').format(value)
}

// 뒤로가기
const goBack = () => {
  router.go(-1)
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
    
    const message = isNewMember.value ? '새 회원이 성공적으로 등록되었습니다.' : '회원 정보가 성공적으로 수정되었습니다.';
    alert(message);
    router.push('/members');
  } catch (error) {
    console.error(error);
    alert('회원 정보 저장에 실패했습니다. 다시 시도해주세요.');
  }
}

// 현재 마일리지 조회
const fetchCurrentPoints = async (memberId) => {
  try {
    const response = await fetch(`/api/rewardpoints/member/${memberId}`)
    if (response.ok) {
      const rewardPoints = await response.json()
      currentPoints.value = rewardPoints.points || 0
    } else {
      // 마일리지 레코드가 없는 경우 0으로 설정
      currentPoints.value = 0
    }
  } catch (error) {
    console.error('마일리지 조회 실패:', error)
    currentPoints.value = 0
  }
}

// 마일리지 충전
const addPoints = async () => {
  if (!pointsToAdd.value || pointsToAdd.value <= 0) {
    alert('충전할 마일리지를 입력해주세요.')
    return
  }

  addingPoints.value = true
  try {
    const response = await fetch('/api/rewardpoints/add', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        memberId: parseInt(route.params.id), 
        points: pointsToAdd.value 
      })
    })
    
    if (!response.ok) throw new Error('Failed to add points')
    
    const updatedRewardPoints = await response.json()
    currentPoints.value = updatedRewardPoints.points
    
    alert(`${formatNumber(pointsToAdd.value)}원이 성공적으로 충전되었습니다!`)
    pointsToAdd.value = 0
  } catch (error) {
    console.error('마일리지 충전 실패:', error)
    alert('마일리지 충전에 실패했습니다. 다시 시도해주세요.')
  } finally {
    addingPoints.value = false
  }
}

const cancelOrder = async (orderId) => {
  if (!confirm('정말로 이 주문을 취소하시겠습니까?')) return;
  try {
    const response = await fetch(`/api/orders/${orderId}/cancel`, { method: 'POST' });
    if (!response.ok) throw new Error('Failed to cancel order');
    await fetchOrderHistory(route.params.id);
    alert('주문이 성공적으로 취소되었습니다.');
  } catch (error) {
    console.error(error);
    alert('주문 취소에 실패했습니다. 다시 시도해주세요.');
  }
}

onMounted(() => {
  if (!isNewMember.value) {
    fetchMemberData(route.params.id)
    fetchOrderHistory(route.params.id)
    fetchCurrentPoints(route.params.id)
  }
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

.v-chip--variant-outlined {
  cursor: pointer;
  transition: all 0.2s ease;
}

.v-chip--variant-outlined:hover {
  background-color: rgba(0, 100, 255, 0.1);
  transform: translateY(-1px);
}
</style>
