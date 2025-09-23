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
      <v-card-title
        class="text-h6 d-flex align-center cursor-pointer"
        @click="toggleMemberInfoExpansion"
      >
        <v-icon class="mr-2" color="primary">mdi-account</v-icon>
        회원 정보
        <v-spacer></v-spacer>
        <v-icon>{{ memberInfoExpanded ? 'mdi-chevron-up' : 'mdi-chevron-down' }}</v-icon>
      </v-card-title>
      <v-expand-transition>
        <v-card-text v-show="memberInfoExpanded">
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
      </v-expand-transition>
    </v-card>

    <!-- 포인트 내역 (기존 회원만) -->
    <v-card class="content-card" v-if="!isNewMember">
      <v-card-title
        class="text-h6 d-flex align-center cursor-pointer"
        @click="togglePointHistoryExpansion"
      >
        <v-icon class="mr-2" color="secondary">mdi-history</v-icon>
        포인트 내역
        <v-spacer></v-spacer>
        <v-btn
          color="secondary"
          variant="elevated"
          size="small"
          @click.stop="showMileageModal = true"
        >
          <v-icon class="mr-1">mdi-plus</v-icon>
          마일리지 충전
        </v-btn>
        <v-icon>{{ pointHistoryExpanded ? 'mdi-chevron-up' : 'mdi-chevron-down' }}</v-icon>
      </v-card-title>
      <v-expand-transition>
        <v-card-text v-show="pointHistoryExpanded">
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
      </v-expand-transition>
    </v-card>

    <!-- 주문 내역 (기존 회원만) -->
    <v-card class="content-card" v-if="!isNewMember">
      <v-card-title class="text-h6 d-flex align-center">
        <v-icon class="mr-2" color="info">mdi-shopping</v-icon>
        주문 내역
      </v-card-title>
      <v-card-text>
        <div v-if="getGroupedOrders().length > 0" class="order-groups">
          <!-- 주문번호별 그룹 -->
          <div v-for="orderGroup in getGroupedOrders()" :key="orderGroup.orderId" class="order-group">
            <!-- 주문번호 헤더 -->
            <div class="group-header cursor-pointer" @click="toggleOrderExpansion(orderGroup.orderId)">
              <div class="group-info">
                <h3 class="group-title">{{ orderGroup.orderId }}</h3>
                <div class="group-status" :class="orderGroup.finalStatus === 'CANCELLED' ? 'cancelled-status' : 'ordered-status'">
                  {{ orderGroup.finalStatus === 'CANCELLED' ? '취소' : '주문' }}
                </div>
              </div>
              <v-icon>{{ expandedOrders.has(orderGroup.orderId) ? 'mdi-chevron-up' : 'mdi-chevron-down' }}</v-icon>
            </div>

            <!-- 주문/취소 상세 영역 -->
            <v-expand-transition>
              <div v-show="expandedOrders.has(orderGroup.orderId)" class="order-details">

                <!-- 주문 영역 -->
                <div class="order-record-card">
                  <div class="record-header order-header">
                    <div class="record-title">
                      <h4>주문번호: {{ orderGroup.orderId }}</h4>
                      <div class="record-amount positive">
                        {{ formatCurrency(orderGroup.orderAmount) }}
                      </div>
                    </div>
                    <div class="record-date">{{ formatDateTime(orderGroup.orderDate) }}</div>
                    <div class="record-status order-status">주문</div>
                  </div>

                  <!-- 상품 정보 -->
                  <div class="record-section">
                    <h5 class="section-title">상품 정보</h5>
                    <div class="item-list">
                      <div v-for="item in orderGroup.orderItems" :key="item.orderItemId" class="item-row">
                        <div class="item-name">{{ item.productName }}</div>
                        <div class="item-details">₩{{ formatNumber(item.priceAtOrder) }} x {{ item.quantity }}개</div>
                      </div>
                    </div>
                  </div>

                  <!-- 결제 정보 -->
                  <div class="record-section">
                    <h5 class="section-title">결제 정보</h5>
                    <div class="payment-summary">
                      <div class="summary-row">
                        <span>총 주문금액</span>
                        <span>{{ formatCurrency(orderGroup.orderAmount) }}</span>
                      </div>
                      <div v-for="payment in orderGroup.orderPayments" :key="payment.paymentId" class="summary-row">
                        <span>{{ getPaymentMethodText(payment.paymentMethod) }} 결제</span>
                        <span>{{ formatCurrency(payment.amount) }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 취소 영역 (있는 경우에만) -->
                <div v-if="orderGroup.cancelPayments.length > 0" class="order-record-card cancel-record">
                  <div class="record-header cancel-header">
                    <div class="record-title">
                      <h4>클레임번호: {{ orderGroup.claimId }}</h4>
                      <div class="record-amount negative">
                        {{ formatCurrency(orderGroup.cancelAmount, true) }}
                      </div>
                    </div>
                    <div class="record-date">{{ formatDateTime(orderGroup.cancelDate) }}</div>
                    <div class="record-status cancel-status">취소</div>
                  </div>

                  <!-- 상품 정보 -->
                  <div class="record-section">
                    <h5 class="section-title">상품 정보</h5>
                    <div class="item-list">
                      <div v-for="item in orderGroup.orderItems" :key="'cancel-' + item.orderItemId" class="item-row">
                        <div class="item-name">{{ item.productName }}</div>
                        <div class="item-details">₩{{ formatNumber(item.priceAtOrder) }} x {{ item.quantity }}개</div>
                      </div>
                    </div>
                  </div>

                  <!-- 결제 정보 -->
                  <div class="record-section">
                    <h5 class="section-title">결제 정보</h5>
                    <div class="payment-summary">
                      <div class="summary-row">
                        <span>총 주문금액</span>
                        <span class="negative">{{ formatCurrency(orderGroup.cancelAmount, true) }}</span>
                      </div>
                      <div v-for="payment in orderGroup.cancelPayments" :key="payment.paymentId" class="summary-row">
                        <span>{{ getPaymentMethodText(payment.paymentMethod) }} 결제</span>
                        <span class="negative">{{ formatCurrency(payment.amount, true) }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 주문 취소 버튼 -->
                <div v-if="orderGroup.finalStatus === 'ORDERED'" class="group-actions">
                  <v-btn
                    color="error"
                    variant="outlined"
                    @click="openCancelDialog(orderGroup.originalOrder)"
                  >
                    주문 취소
                  </v-btn>
                </div>
              </div>
            </v-expand-transition>
          </div>
        </div>
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

    <!-- 마일리지 충전 모달 -->
    <v-dialog v-model="showMileageModal" max-width="500" persistent>
      <v-card class="mileage-modal">
        <v-card-title class="dialog-title">
          <v-icon color="secondary" class="mr-2">mdi-wallet</v-icon>
          마일리지 충전
        </v-card-title>
        <v-card-text class="dialog-content">
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
          <v-text-field
            v-model.number="pointsToAdd"
            label="충전할 마일리지"
            type="number"
            variant="outlined"
            suffix="원"
            :rules="[rules.required, rules.minAmount]"
            class="mb-4"
          ></v-text-field>

          <!-- 빠른 충전 버튼들 -->
          <div class="mb-4">
            <div class="text-subtitle2 mb-2">빠른 충전</div>
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
        <v-card-actions class="dialog-actions">
          <v-btn
            variant="outlined"
            @click="closeMileageModal"
            :disabled="addingPoints"
          >
            취소
          </v-btn>
          <v-btn
            color="secondary"
            @click="addPointsFromModal"
            :disabled="!pointsToAdd || pointsToAdd <= 0"
            :loading="addingPoints"
          >
            충전하기
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 주문 취소 확인 다이얼로그 -->
    <v-dialog v-model="showCancelDialog" max-width="400" persistent>
      <v-card>
        <v-card-title class="text-h6">주문 취소 확인</v-card-title>
        <v-card-text>
          정말로 이 주문을 취소하시겠습니까?
          <br><br>
          <strong>주문번호:</strong> {{ orderToCancel?.orderId }}
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            variant="outlined"
            @click="showCancelDialog = false"
            :disabled="cancelProcessing"
          >
            취소
          </v-btn>
          <v-btn
            color="error"
            @click="confirmCancelOrder"
            :loading="cancelProcessing"
          >
            확인
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 주문 취소 성공 다이얼로그 -->
    <v-dialog v-model="showCancelSuccessDialog" max-width="400" persistent>
      <v-card>
        <v-card-title class="text-h6 text-center">알림</v-card-title>
        <v-card-text class="text-center">
          <v-icon color="success" size="48" class="mb-4">mdi-check-circle</v-icon>
          <br>
          주문 취소가 완료되었습니다.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            variant="elevated"
            @click="confirmCancelSuccess"
          >
            확인
          </v-btn>
          <v-spacer></v-spacer>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
const memberInfoExpanded = ref(false)
const pointHistoryExpanded = ref(false)
const showMileageModal = ref(false)
const pointHistoryPage = ref(0)
const orderHistoryPage = ref(0)
const pointHistoryLoading = ref(false)
const pointHistoryHasMore = ref(true)
const orderHistoryHasMore = ref(true)
const orderToCancel = ref(null)
const showCancelDialog = ref(false)
const showCancelSuccessDialog = ref(false)
const cancelProcessing = ref(false)
const expandedOrders = ref(new Set())

// 빠른 충전 금액들
const quickAmounts = [1000, 5000, 10000, 30000, 50000, 100000]

// 포인트 필터 옵션
const pointFilters = [
  { label: '전체', value: 'all' },
  { label: '충전', value: 'charge' },
  { label: '사용', value: 'USE' }
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
  if (filterValue === 'charge') {
    // 충전 탭: EARN, REFUND 타입 표시
    return pointHistory.value.filter(item =>
      item.transactionType === 'EARN' || item.transactionType === 'REFUND'
    )
  }
  if (filterValue === 'USE') {
    // 사용 탭: USE 타입만 표시
    return pointHistory.value.filter(item => item.transactionType === 'USE')
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

// 주문 상태 색상 (취소 여부 고려)
const getOrderStatusColor = (order) => {
  // 취소된 주문인지 확인 (ord_proc_seq가 2 이상인 항목이 있으면 취소됨)
  const isCancelled = order.orderProcesses &&
    order.orderProcesses.some(process => process.ordProcSeq > 1)

  if (isCancelled) {
    return 'error'
  }

  // 원본 상태값 기반 처리
  switch (order.status) {
    case 'COMPLETED':
    case 'PAID': return 'success'
    case 'PENDING': return 'warning'
    case 'FAIL': return 'error'
    default: return 'grey'
  }
}

// 주문 상태 텍스트 (취소 여부 고려)
const getOrderStatusText = (order) => {
  // 취소된 주문인지 확인 (ord_proc_seq가 2 이상인 항목이 있으면 취소됨)
  const isCancelled = order.orderProcesses &&
    order.orderProcesses.some(process => process.ordProcSeq > 1)

  if (isCancelled) {
    return '취소완료'
  }

  // 원본 상태값 기반 처리
  switch (order.status) {
    case 'COMPLETED': return '완료'
    case 'PAID': return '결제완료'
    case 'PENDING': return '진행중'
    case 'FAIL': return '실패'
    default: return order.status
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

// 주문 결제 정보 필터링 (order_status = 'ORDER')
const getOrderPayments = (order) => {
  if (!order || !order.payments) return []
  return order.payments.filter(payment => payment.orderStatus === 'ORDER')
}

// 취소 환불 정보 필터링 (order_status = 'CANCELED')
const getCancelPayments = (order) => {
  if (!order || !order.payments) return []
  return order.payments.filter(payment => payment.orderStatus === 'CANCELED')
}

// 주문을 주문번호별로 그루핑
const getGroupedOrders = () => {
  const orderMap = new Map()

  // 주문ID별로 그룹핑
  orders.value.forEach(order => {
    const orderId = order.orderId

    if (!orderMap.has(orderId)) {
      // 새로운 주문 그룹 생성
      const orderPayments = getOrderPayments(order)
      const cancelPayments = getCancelPayments(order)

      if (orderPayments.length > 0) {
        const finalStatus = cancelPayments.length > 0 ? 'CANCELLED' : 'ORDERED'
        const orderAmount = orderPayments.reduce((sum, payment) => sum + payment.amount, 0)
        const cancelAmount = cancelPayments.reduce((sum, payment) => sum + payment.amount, 0)

        // 클레임 ID 찾기 - Order 테이블에서 claim_id 조회
        const claimId = order.orderProcesses?.find(process => process.claimId)?.claimId || 'N/A'
        const cancelDate = cancelPayments[0]?.paymentDate || order.orderDate

        // 같은 주문ID를 가진 모든 주문의 상품들을 수집
        const allOrderItems = orders.value
          .filter(o => o.orderId === orderId)
          .flatMap(o => o.orderItems || [])

        orderMap.set(orderId, {
          orderId: orderId,
          finalStatus: finalStatus,
          orderPayments: orderPayments,
          cancelPayments: cancelPayments,
          orderAmount: orderAmount,
          cancelAmount: cancelAmount,
          orderDate: order.orderDate,
          cancelDate: cancelDate,
          claimId: claimId,
          orderItems: allOrderItems,
          canCancel: order.status === 'ORDERED' && cancelPayments.length === 0,
          originalOrder: order
        })
      }
    }
  })

  // Map을 배열로 변환하고 날짜순 정렬
  return Array.from(orderMap.values()).sort((a, b) => new Date(b.orderDate) - new Date(a.orderDate))
}

// 주문을 주문 기록과 취소 기록으로 확장
const getExpandedOrders = () => {
  const expandedOrders = []

  orders.value.forEach(order => {
    // 주문 기록 추가
    const orderPayments = getOrderPayments(order)
    if (orderPayments.length > 0) {
      const orderAmount = orderPayments.reduce((sum, payment) => sum + payment.amount, 0)
      expandedOrders.push({
        id: order.orderId,
        type: 'order',
        amount: orderAmount,
        date: order.orderDate,
        items: order.orderItems || [],
        payments: orderPayments,
        canCancel: order.status === 'ORDERED' && !getCancelPayments(order).length,
        originalOrder: order
      })
    }

    // 취소 기록 추가 (취소가 있는 경우)
    const cancelPayments = getCancelPayments(order)
    if (cancelPayments.length > 0) {
      const cancelAmount = cancelPayments.reduce((sum, payment) => sum + payment.amount, 0)
      const claimId = cancelPayments[0]?.claimId || 'N/A'
      const cancelDate = cancelPayments[0]?.paymentDate || order.orderDate

      expandedOrders.push({
        id: claimId,
        type: 'cancel',
        amount: cancelAmount,
        date: cancelDate,
        items: order.orderItems || [],
        payments: cancelPayments,
        canCancel: false,
        originalOrder: order
      })
    }
  })

  // 날짜순으로 정렬 (최신순)
  return expandedOrders.sort((a, b) => new Date(b.date) - new Date(a.date))
}

// 금액 포맷팅 (마이너스 옵션 포함)
const formatCurrency = (amount, isNegative = false) => {
  if (!amount && amount !== 0) return '0원'
  const formatted = formatNumber(Math.abs(amount)) + '원'
  return isNegative ? '-' + formatted : formatted
}

// 주문 총 결제 금액 계산
const calculateOrderTotal = (order) => {
  if (!order || !order.payments) {
    return 0
  }

  // 결제 테이블에서 PAYMENT 타입인 것들의 금액 합계
  const paymentTotal = order.payments
    .filter(payment => payment.payType === 'PAYMENT')
    .reduce((sum, payment) => sum + (payment.amount || 0), 0)

  return paymentTotal
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

// 회원 정보 펼침/접힘 토글
const toggleMemberInfoExpansion = () => {
  memberInfoExpanded.value = !memberInfoExpanded.value
}

// 마일리지 관리 펼침/접힘 토글
const toggleMileageExpansion = () => {
  mileageExpanded.value = !mileageExpanded.value
}

// 포인트 내역 펼침/접힘 토글
const togglePointHistoryExpansion = () => {
  pointHistoryExpanded.value = !pointHistoryExpanded.value
}

// 주문 그룹 펼침/접힘 토글
const toggleOrderExpansion = (orderId) => {
  if (expandedOrders.value.has(orderId)) {
    expandedOrders.value.delete(orderId)
  } else {
    expandedOrders.value.add(orderId)
  }
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

// 마일리지 모달 닫기
const closeMileageModal = () => {
  showMileageModal.value = false
  pointsToAdd.value = 0
}

// 모달에서 마일리지 충전
const addPointsFromModal = async () => {
  await addPoints()
  if (!addingPoints.value) { // 충전 성공 시에만 모달 닫기
    closeMileageModal()
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

// 주문 취소 다이얼로그 열기
const openCancelDialog = (order) => {
  orderToCancel.value = order
  showCancelDialog.value = true
}

// 주문 취소 확인
const confirmCancelOrder = async () => {
  if (!orderToCancel.value) return

  cancelProcessing.value = true
  try {
    const response = await fetch(`/api/orders/${orderToCancel.value.orderId}/cancel`, {
      method: 'POST'
    })

    if (!response.ok) {
      throw new Error('Failed to cancel order')
    }

    // 취소 확인 모달 닫기
    showCancelDialog.value = false
    orderToCancel.value = null

    // 성공 모달 표시
    showCancelSuccessDialog.value = true

  } catch (error) {
    console.error('주문 취소 실패:', error)
    alert('주문 취소에 실패했습니다. 다시 시도해주세요.')
  } finally {
    cancelProcessing.value = false
  }
}

// 주문 취소 성공 모달 확인
const confirmCancelSuccess = () => {
  showCancelSuccessDialog.value = false
  // 페이지 새로고침 (전체 데이터 다시 로드)
  window.location.reload()
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

/* 주문/취소 기록 카드 */
.order-record-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border-left: 4px solid #4caf50;
}

.order-record-card:has(.cancel-header) {
  border-left-color: #f44336;
}

/* 기록 헤더 */
.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.record-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.record-title h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.record-amount {
  font-size: 1.3rem;
  font-weight: 700;
}

.record-amount.positive {
  color: #4caf50;
}

.record-amount.negative {
  color: #f44336;
}

.record-date {
  color: #666;
  font-size: 0.9rem;
}

.record-status {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 0.8rem;
  font-weight: 600;
}

.record-status.order-status {
  background: #e8f5e8;
  color: #2e7d32;
}

.record-status.cancel-status {
  background: #ffebee;
  color: #c62828;
}

/* 기록 섹션 */
.record-section {
  margin-bottom: 24px;
}

.record-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

/* 상품/결제 목록 */
.item-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.item-name {
  font-weight: 600;
  color: #333;
}

.item-details {
  color: #666;
  font-size: 0.9rem;
}

/* 결제 요약 */
.payment-summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  color: #666;
}

.summary-row:first-child {
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
  margin-bottom: 4px;
}

.negative {
  color: #f44336;
}

/* 기록 액션 */
.record-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

/* 주문 그룹 */
.order-groups {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.order-group {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
}

/* 그룹 헤더 */
.group-header {
  background: #f8f9fa;
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
}

.group-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.group-status {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 0.8rem;
  font-weight: 600;
}

.group-status.ordered-status {
  background: #e8f5e8;
  color: #2e7d32;
}

.group-status.cancelled-status {
  background: #ffebee;
  color: #c62828;
}

/* 그룹 액션 */
.group-actions {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  background: #fafafa;
  border-top: 1px solid #e0e0e0;
}

/* 마일리지 충전 모달 */
.mileage-modal {
  border-radius: 16px;
}

.dialog-title {
  background: #f8f9fa;
  color: #333;
  font-weight: 600;
  padding: 20px 24px 16px;
  display: flex;
  align-items: center;
}

.dialog-content {
  padding: 20px 24px;
}

.dialog-actions {
  padding: 16px 24px 24px;
  gap: 8px;
}
</style>
