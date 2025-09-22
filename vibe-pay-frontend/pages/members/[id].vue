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
      <v-card-title
        class="text-h6 d-flex align-center cursor-pointer"
        @click="toggleMileageExpansion"
      >
        <v-icon class="mr-2" color="secondary">mdi-wallet</v-icon>
        마일리지 관리
        <v-spacer></v-spacer>
        <v-icon>{{ mileageExpanded ? 'mdi-chevron-up' : 'mdi-chevron-down' }}</v-icon>
      </v-card-title>
      <v-expand-transition>
        <v-card-text v-show="mileageExpanded">
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
      </v-expand-transition>
    </v-card>

    <!-- 포인트 내역 (기존 회원만) -->
    <v-card class="content-card" v-if="!isNewMember">
      <v-card-title class="text-h6 d-flex align-center">
        <v-icon class="mr-2" color="secondary">mdi-history</v-icon>
        포인트 내역
      </v-card-title>
      <v-card-text>
        <!-- 포인트 필터 -->
        <div class="mb-4">
          <v-chip-group v-model="selectedPointFilter" mandatory>
            <v-chip
              v-for="filter in pointFilters"
              :key="filter.value"
              :value="filter.value"
              variant="outlined"
              color="primary"
            >
              {{ filter.label }}
            </v-chip>
          </v-chip-group>
        </div>

        <!-- 포인트 내역 리스트 -->
        <v-timeline side="end" density="compact" v-if="filteredPointHistory.length > 0">
          <v-timeline-item
            v-for="history in filteredPointHistory"
            :key="history.pointHistoryId"
            :dot-color="getPointHistoryColor(history.transactionType)"
            size="small"
          >
            <template v-slot:icon>
              <v-icon size="16">{{ getPointHistoryIcon(history.transactionType) }}</v-icon>
            </template>
            <v-card variant="outlined" class="point-history-card">
              <v-card-text class="pa-3">
                <div class="d-flex justify-space-between align-center mb-2">
                  <span class="text-subtitle2">{{ history.description }}</span>
                  <span
                    class="text-h6 font-weight-bold"
                    :class="getPointAmountClass(history.transactionType)"
                  >
                    {{ formatPointAmount(history.pointAmount, history.transactionType) }}
                  </span>
                </div>
                <div class="d-flex justify-space-between text-body-2 text-medium-emphasis">
                  <span>{{ formatDateTime(history.createdAt) }}</span>
                  <span>잔액: {{ formatNumber(history.balanceAfter) }}P</span>
                </div>
                <div v-if="history.referenceId" class="text-caption text-medium-emphasis mt-1">
                  참조: {{ history.referenceId }}
                </div>
              </v-card-text>
            </v-card>
          </v-timeline-item>
        </v-timeline>
        <v-alert v-else type="info" variant="tonal">
          포인트 내역이 없습니다.
        </v-alert>

        <!-- 더 보기 버튼 -->
        <div v-if="pointHistoryHasMore && filteredPointHistory.length > 0" class="text-center mt-4">
          <v-btn
            @click="loadMorePointHistory"
            variant="outlined"
            :loading="pointHistoryLoading"
          >
            더 보기
          </v-btn>
        </div>
      </v-card-text>
    </v-card>

    <!-- 주문 내역 (기존 회원만) -->
    <v-card class="content-card" v-if="!isNewMember">
      <v-card-title class="text-h6 d-flex align-center">
        <v-icon class="mr-2" color="info">mdi-shopping</v-icon>
        주문 내역
      </v-card-title>
      <v-card-text>
        <v-expansion-panels v-if="orders.length > 0" multiple>
          <v-expansion-panel
            v-for="order in orders"
            :key="order.orderId"
            class="order-expansion-panel"
          >
            <v-expansion-panel-title>
              <div class="d-flex justify-space-between align-center w-100 mr-4">
                <div>
                  <div class="text-subtitle1 font-weight-medium">
                    주문번호: {{ order.orderId }}
                  </div>
                  <div class="text-body-2 text-medium-emphasis">
                    {{ formatDateTime(order.orderDate) }}
                  </div>
                </div>
                <div class="text-right">
                  <div class="text-h6 font-weight-bold text-primary">
                    {{ formatNumber(order.finalPaymentAmount) }}원
                  </div>
                  <v-chip
                    :color="getOrderStatusColor(order.status)"
                    size="small"
                    variant="flat"
                  >
                    {{ getOrderStatusText(order.status) }}
                  </v-chip>
                </div>
              </div>
            </v-expansion-panel-title>
            <v-expansion-panel-text>
              <v-row>
                <!-- 주문 상품 정보 -->
                <v-col cols="12">
                  <v-card variant="outlined" class="pa-3 mb-3">
                    <div class="text-subtitle2 mb-3">주문 상품</div>
                    <div v-if="order.orderItems && order.orderItems.length > 0">
                      <div
                        v-for="item in order.orderItems"
                        :key="item.orderItemId"
                        class="product-item"
                      >
                        <div class="d-flex justify-space-between align-center">
                          <div>
                            <div class="text-subtitle2">{{ item.productName }}</div>
                            <div class="text-body-2 text-medium-emphasis">
                              {{ formatNumber(item.priceAtOrder) }}원 × {{ item.quantity }}개
                            </div>
                          </div>
                          <div class="text-h6 font-weight-bold">
                            {{ formatNumber(item.totalPrice) }}원
                          </div>
                        </div>
                        <v-divider v-if="order.orderItems.indexOf(item) < order.orderItems.length - 1" class="my-2"></v-divider>
                      </div>
                    </div>
                    <div v-else class="text-body-2 text-medium-emphasis">
                      상품 정보를 불러올 수 없습니다.
                    </div>
                  </v-card>
                </v-col>

                <!-- 결제 상세 -->
                <v-col cols="12" md="6">
                  <v-card variant="outlined" class="pa-3">
                    <div class="text-subtitle2 mb-2">결제 상세</div>
                    <div class="payment-detail-item">
                      <span>총 주문금액</span>
                      <span class="font-weight-medium">{{ formatNumber(order.totalAmount) }}원</span>
                    </div>

                    <!-- 포인트 결제 정보 -->
                    <div v-if="order.pointPayments && order.pointPayments.length > 0">
                      <div
                        v-for="pointPayment in order.pointPayments"
                        :key="pointPayment.paymentId"
                        class="payment-detail-item"
                      >
                        <span>포인트 결제</span>
                        <span class="font-weight-medium text-orange">-{{ formatNumber(pointPayment.amount) }}P</span>
                      </div>
                    </div>

                    <!-- 카드 결제 정보 -->
                    <div v-if="order.cardPayments && order.cardPayments.length > 0">
                      <div
                        v-for="cardPayment in order.cardPayments"
                        :key="cardPayment.paymentId"
                        class="payment-detail-item"
                      >
                        <span>{{ getPaymentMethodText(cardPayment.paymentMethod) }}</span>
                        <span class="font-weight-medium">{{ formatNumber(cardPayment.amount) }}원</span>
                      </div>
                    </div>

                    <v-divider class="my-2"></v-divider>
                    <div class="payment-detail-item">
                      <span class="text-subtitle2">최종 결제금액</span>
                      <span class="text-h6 font-weight-bold text-primary">{{ formatNumber(order.finalPaymentAmount) }}원</span>
                    </div>
                  </v-card>
                </v-col>

                <!-- 주문 정보 -->
                <v-col cols="12" md="6">
                  <v-card variant="outlined" class="pa-3">
                    <div class="text-subtitle2 mb-2">주문 정보</div>
                    <div class="order-info-item">
                      <span>주문상태</span>
                      <v-chip
                        :color="getOrderStatusColor(order.status)"
                        size="small"
                        variant="flat"
                      >
                        {{ getOrderStatusText(order.status) }}
                      </v-chip>
                    </div>
                    <div v-if="order.payments && order.payments.length > 0" class="order-info-item">
                      <span>결제방법</span>
                      <div>
                        <v-chip
                          v-for="payment in order.payments"
                          :key="payment.paymentId"
                          size="small"
                          variant="outlined"
                          class="mr-1 mb-1"
                        >
                          {{ getPaymentMethodText(payment.paymentMethod) }}
                        </v-chip>
                      </div>
                    </div>
                    <div v-if="order.cardPayments && order.cardPayments.length > 0" class="order-info-item">
                      <span>거래번호</span>
                      <div>
                        <div
                          v-for="cardPayment in order.cardPayments"
                          :key="cardPayment.paymentId"
                          class="text-caption mb-1"
                        >
                          {{ cardPayment.transactionId }}
                        </div>
                      </div>
                    </div>
                  </v-card>
                </v-col>
              </v-row>
              <v-card-actions class="px-0">
                <v-spacer></v-spacer>
                <v-btn
                  v-if="order.status === 'PAID'"
                  color="error"
                  variant="outlined"
                  @click="cancelOrder(order.orderId)"
                >
                  주문 취소
                </v-btn>
              </v-card-actions>
            </v-expansion-panel-text>
          </v-expansion-panel>
        </v-expansion-panels>
        <v-alert v-else type="info" variant="tonal">
          주문 내역이 없습니다.
        </v-alert>

        <!-- 더 보기 버튼 -->
        <div v-if="orderHistoryHasMore && orders.length > 0" class="text-center mt-4">
          <v-btn
            @click="loadMoreOrderHistory"
            variant="outlined"
            :loading="ordersLoading"
          >
            더 보기
          </v-btn>
        </div>
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
const pointHistory = ref([])
const pointStats = ref(null)
const selectedPointFilter = ref(0)
const mileageExpanded = ref(false)
const pointHistoryPage = ref(0)
const orderHistoryPage = ref(0)
const pointHistoryLoading = ref(false)
const pointHistoryHasMore = ref(true)
const orderHistoryHasMore = ref(true)

// 빠른 충전 금액들
const quickAmounts = [1000, 5000, 10000, 30000, 50000, 100000]

// 포인트 필터 옵션
const pointFilters = [
  { label: '전체', value: 'all' },
  { label: '적립', value: 'EARN' },
  { label: '사용', value: 'USE' },
  { label: '환불', value: 'REFUND' }
]

const orderHeaders = [
  { title: '주문번호', key: 'orderId' },
  { title: '주문일시', key: 'orderDate' },
  { title: '총금액', key: 'totalAmount' },
  { title: '상태', key: 'status' },
  { title: '액션', key: 'actions', sortable: false },
]

const isNewMember = computed(() => route.params.id === 'new')
const pageTitle = computed(() => isNewMember.value ? '새 회원 등록' : '회원 정보 수정')

// 필터된 포인트 내역
const filteredPointHistory = computed(() => {
  const filterValue = pointFilters[selectedPointFilter.value]?.value
  if (!filterValue || filterValue === 'all') {
    return pointHistory.value
  }
  return pointHistory.value.filter(item => item.transactionType === filterValue)
})

const rules = {
  required: value => !!value || '필수 입력 항목입니다.',
  minAmount: value => value > 0 || '0보다 큰 금액을 입력해주세요.',
}

// 숫자 포맷팅
const formatNumber = (value) => {
  return new Intl.NumberFormat('ko-KR').format(value)
}

// 날짜/시간 포맷팅
const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 포인트 내역 아이콘
const getPointHistoryIcon = (transactionType) => {
  switch (transactionType) {
    case 'EARN': return 'mdi-plus-circle'
    case 'USE': return 'mdi-minus-circle'
    case 'REFUND': return 'mdi-restore'
    default: return 'mdi-circle'
  }
}

// 포인트 내역 색상
const getPointHistoryColor = (transactionType) => {
  switch (transactionType) {
    case 'EARN': return 'success'
    case 'USE': return 'error'
    case 'REFUND': return 'info'
    default: return 'grey'
  }
}

// 포인트 금액 클래스
const getPointAmountClass = (transactionType) => {
  switch (transactionType) {
    case 'EARN': return 'text-success'
    case 'USE': return 'text-error'
    case 'REFUND': return 'text-info'
    default: return ''
  }
}

// 포인트 금액 포맷팅 (부호 포함)
const formatPointAmount = (amount, transactionType) => {
  const formattedAmount = formatNumber(Math.abs(amount))
  switch (transactionType) {
    case 'EARN': return `+${formattedAmount}P`
    case 'USE': return `-${formattedAmount}P`
    case 'REFUND': return `+${formattedAmount}P`
    default: return `${formattedAmount}P`
  }
}

// 주문 상태 색상
const getOrderStatusColor = (status) => {
  switch (status) {
    case 'COMPLETED':
    case 'PAID': return 'success'
    case 'PENDING': return 'warning'
    case 'FAIL': return 'error'
    default: return 'grey'
  }
}

// 주문 상태 텍스트
const getOrderStatusText = (status) => {
  switch (status) {
    case 'COMPLETED': return '완료'
    case 'PAID': return '결제완료'
    case 'PENDING': return '진행중'
    case 'FAIL': return '실패'
    default: return status
  }
}

// 결제수단 텍스트
const getPaymentMethodText = (paymentMethod) => {
  switch (paymentMethod) {
    case 'CREDIT_CARD': return '신용카드'
    case 'DEBIT_CARD': return '체크카드'
    case 'POINT': return '포인트'
    case 'BANK_TRANSFER': return '계좌이체'
    default: return paymentMethod
  }
}

// 뒤로가기
const goBack = () => {
  router.go(-1)
}

const fetchMemberData = async (memberId) => {
  try {
    const response = await fetch(`/api/members/${memberId}`)
    if (!response.ok) throw new Error('Failed to fetch member data')
    member.value = await response.json()
  } catch (error) {
    console.error(error)
    router.push('/members')
  }
}

const fetchOrderHistory = async (memberId, page = 0, append = false) => {
  ordersLoading.value = true
  try {
    const response = await fetch(`/api/members/${memberId}/order-history?page=${page}&size=10`)
    if (!response.ok) throw new Error('Failed to fetch order details')
    const data = await response.json()
    const processedData = data.map(o => ({
      ...o,
      // orderDate는 그대로 두고 템플릿에서 formatDateTime으로 포맷
      // 결제 정보 분리
      cardPayments: o.payments?.filter(p => p.paymentMethod !== 'POINT') || [],
      pointPayments: o.payments?.filter(p => p.paymentMethod === 'POINT') || []
    }))

    if (append) {
      orders.value = [...orders.value, ...processedData]
    } else {
      orders.value = processedData
    }
    orderHistoryHasMore.value = data.length === 10
    console.log('주문 상세 정보 조회 성공:', processedData)
  } catch (error) {
    console.error('주문 상세 정보 조회 실패:', error)
  } finally {
    ordersLoading.value = false
  }
}

// 주문 내역 더 불러오기
const loadMoreOrderHistory = async () => {
  if (!orderHistoryHasMore.value || ordersLoading.value) return
  orderHistoryPage.value += 1
  await fetchOrderHistory(route.params.id, orderHistoryPage.value, true)
}

// 마일리지 관리 펼침/접힘 토글
const toggleMileageExpansion = () => {
  mileageExpanded.value = !mileageExpanded.value
}

// 포인트 내역 조회 (페이징)
const fetchPointHistory = async (memberId, page = 0, append = false) => {
  pointHistoryLoading.value = true
  try {
    const response = await fetch(`/api/members/${memberId}/point-history?page=${page}&size=10`)
    if (response.ok) {
      const data = await response.json()
      if (append) {
        pointHistory.value = [...pointHistory.value, ...data]
      } else {
        pointHistory.value = data
      }
      pointHistoryHasMore.value = data.length === 10
      console.log('포인트 내역 조회 성공:', data)
    } else {
      console.error('포인트 내역 조회 실패 - 응답 상태:', response.status)
    }
  } catch (error) {
    console.error('포인트 내역 조회 실패:', error)
  } finally {
    pointHistoryLoading.value = false
  }
}

// 포인트 내역 더 불러오기
const loadMorePointHistory = async () => {
  if (!pointHistoryHasMore.value || pointHistoryLoading.value) return
  pointHistoryPage.value += 1
  await fetchPointHistory(route.params.id, pointHistoryPage.value, true)
}

// 포인트 통계 조회
const fetchPointStats = async (memberId) => {
  try {
    const response = await fetch(`/api/point-history/member/${memberId}/statistics`)
    if (response.ok) {
      pointStats.value = await response.json()
      console.log('포인트 통계 조회 성공:', pointStats.value)
    } else {
      console.error('포인트 통계 조회 실패 - 응답 상태:', response.status)
    }
  } catch (error) {
    console.error('포인트 통계 조회 실패:', error)
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

    // 포인트 내역 새로고침
    pointHistoryPage.value = 0
    await fetchPointHistory(route.params.id, 0, false)
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

    // 주문 내역과 포인트 내역, 현재 포인트 새로고침
    orderHistoryPage.value = 0
    pointHistoryPage.value = 0
    await Promise.all([
      fetchOrderHistory(route.params.id, 0, false),
      fetchPointHistory(route.params.id, 0, false),
      fetchCurrentPoints(route.params.id)
    ]);

    alert('주문이 성공적으로 취소되었습니다.');
  } catch (error) {
    console.error(error);
    alert('주문 취소에 실패했습니다. 다시 시도해주세요.');
  }
}

onMounted(() => {
  if (!isNewMember.value) {
    fetchMemberData(route.params.id)
    fetchOrderHistory(route.params.id, 0, false)
    fetchCurrentPoints(route.params.id)
    fetchPointHistory(route.params.id, 0, false)
    fetchPointStats(route.params.id)
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

/* 포인트 내역 카드 */
.point-history-card {
  margin-bottom: 8px;
}

/* 결제 상세 항목 */
.payment-detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

/* 주문 정보 항목 */
.order-info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

/* 주문 확장 패널 */
.order-expansion-panel {
  margin-bottom: 8px;
}

/* 커서 포인터 */
.cursor-pointer {
  cursor: pointer;
}
</style>
