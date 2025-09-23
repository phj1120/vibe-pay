<template>
  <div class="order-complete-page">
    <!-- 헤더 -->
    <div class="page-header">
      <v-btn icon @click="$router.push('/')" class="back-btn">
        <v-icon>mdi-home</v-icon>
      </v-btn>
      <h1 class="page-title">주문 완료</h1>
    </div>

    <!-- 메인 컨텐츠 -->
    <div class="complete-content">
      <!-- 성공 아이콘 -->
      <div class="success-icon">
        <v-icon size="80" color="success">mdi-check-circle</v-icon>
      </div>

      <!-- 완료 메시지 -->
      <div class="success-message">
        <h2>주문이 완료되었습니다!</h2>
        <p>주문해주셔서 감사합니다.</p>
      </div>

      <!-- 주문 정보 -->
      <div class="order-info-card" v-if="orderDetail">
        <div class="info-row">
          <span class="label">주문번호</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <div class="info-row">
          <span class="label">주문일시</span>
          <span class="value">{{ formatDate(orderDetail.orderDate) }}</span>
        </div>
        <div class="info-row">
          <span class="label">주문상태</span>
          <span class="value" :class="getStatusClass(orderDetail)">{{ getStatusText(orderDetail) }}</span>
        </div>
      </div>

      <!-- 주문 상품 목록 -->
      <div class="order-items-card" v-if="orderDetail && orderDetail.orderItems">
        <h3 class="card-title">
          <v-icon color="primary" class="mr-2">mdi-shopping</v-icon>
          주문 상품
        </h3>
        <div class="item-list">
          <div v-for="item in orderDetail.orderItems" :key="item.productId" class="order-item">
            <div class="item-info">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-details">
                <span class="item-price">₩{{ item.priceAtOrder.toLocaleString() }}</span>
                <span class="item-quantity">x {{ item.quantity }}</span>
              </div>
            </div>
            <div class="item-total">
              ₩{{ (item.priceAtOrder * item.quantity).toLocaleString() }}
            </div>
          </div>
        </div>
      </div>

      <!-- 결제 정보 -->
      <div class="payment-info-card" v-if="orderDetail && orderDetail.payments">
        <h3 class="card-title">
          <v-icon color="success" class="mr-2">mdi-credit-card</v-icon>
          결제 정보
        </h3>

        <!-- 주문 결제 정보 -->
        <div v-if="getOrderPayments().length > 0" class="payment-section">
          <h4 class="section-subtitle">주문 결제</h4>
          <div class="payment-list">
            <div v-for="payment in getOrderPayments()" :key="payment.paymentId" class="payment-item">
              <div class="payment-info">
                <div class="payment-method">{{ getPaymentMethodText(payment.paymentMethod) }}</div>
                <div class="payment-status order-payment">결제</div>
              </div>
              <div class="payment-amount order-amount">
                ₩{{ payment.amount.toLocaleString() }}
              </div>
            </div>
          </div>
        </div>

        <!-- 취소 환불 정보 -->
        <div v-if="getCancelPayments().length > 0" class="payment-section">
          <h4 class="section-subtitle">취소 환불</h4>
          <div class="payment-list">
            <div v-for="payment in getCancelPayments()" :key="payment.paymentId" class="payment-item">
              <div class="payment-info">
                <div class="payment-method">{{ getPaymentMethodText(payment.paymentMethod) }}</div>
                <div class="payment-status cancel-payment">환불</div>
              </div>
              <div class="payment-amount cancel-amount">
                -₩{{ payment.amount.toLocaleString() }}
              </div>
            </div>
          </div>
        </div>

        <div class="payment-summary">
          <div class="summary-row">
            <span>총 주문금액</span>
            <span>₩{{ calculateTotalAmount().toLocaleString() }}</span>
          </div>
          <div class="summary-row">
            <span>최종 결제금액</span>
            <span class="final-amount">₩{{ calculateFinalAmount().toLocaleString() }}</span>
          </div>
        </div>
      </div>

      <!-- 액션 버튼들 -->
      <div class="action-buttons">
        <v-btn
          v-if="canCancelOrder"
          color="error"
          variant="outlined"
          size="large"
          @click="cancelOrder"
          :disabled="cancelling"
          :loading="cancelling"
          class="action-btn"
        >
          주문 취소
        </v-btn>
        <v-btn
          color="primary"
          variant="outlined"
          size="large"
          @click="goToMemberPage"
          class="action-btn"
        >
          주문내역 확인
        </v-btn>
        <v-btn
          color="primary"
          size="large"
          @click="$router.push('/')"
          class="action-btn"
        >
          홈으로
        </v-btn>
      </div>

    <!-- 주문 취소 확인 모달 -->
    <v-dialog v-model="showCancelDialog" max-width="400" persistent>
      <v-card class="cancel-dialog">
        <v-card-title class="dialog-title">
          <v-icon color="error" class="mr-2">mdi-alert-circle</v-icon>
          주문 취소 확인
        </v-card-title>
        <v-card-text class="dialog-content">
          <p>정말로 이 주문을 취소하시겠습니까?</p>
          <p class="warning-text">취소 후에는 되돌릴 수 없습니다.</p>
        </v-card-text>
        <v-card-actions class="dialog-actions">
          <v-btn
            variant="outlined"
            @click="showCancelDialog = false"
            :disabled="cancelling"
          >
            아니오
          </v-btn>
          <v-btn
            color="error"
            @click="confirmCancelOrder"
            :loading="cancelling"
          >
            네, 취소합니다
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    </div>
  </div>
</template>

<script setup>
const route = useRoute();
const router = useRouter();

const orderId = ref(route.query.orderId || '');
const orderDetail = ref(null);
const loading = ref(false);
const cancelling = ref(false);
const showCancelDialog = ref(false);

// 주문 상세 정보 조회
const fetchOrderDetail = async () => {
  if (!orderId.value) {
    console.error('Order number not provided');
    return;
  }

  loading.value = true;
  try {
    // 주문 상세 정보 조회 (상품 정보 + 결제 정보 포함)
    const response = await fetch(`/api/orders/details/${orderId.value}`);
    if (response.ok) {
      const orderDetails = await response.json();
      // 배열에서 첫 번째 주문 상세 정보 사용
      orderDetail.value = orderDetails.length > 0 ? orderDetails[0] : null;
      console.log('Order detail loaded:', orderDetail.value);
    } else {
      console.error('Failed to fetch order detail');
    }
  } catch (error) {
    console.error('Error fetching order detail:', error);
  } finally {
    loading.value = false;
  }
};

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};



// 상태 관련 함수들
const getStatusText = (order) => {
  if (!order) return '';
  const hasCancel = order.orderProcesses?.some(proc => proc.status === 'CANCELLED');
  return hasCancel ? '취소완료' : '주문완료';
};

const getStatusClass = (order) => {
  if (!order) return '';
  const hasCancel = order.orderProcesses?.some(proc => proc.status === 'CANCELLED');
  return hasCancel ? 'cancelled' : 'success';
};

const getPaymentMethodText = (method) => {
  const methods = {
    'CREDIT_CARD': '신용카드',
    'POINT': '포인트',
    'CASH': '현금'
  };
  return methods[method] || method;
};

// 주문 결제 정보 필터링 (order_status = 'ORDER')
const getOrderPayments = () => {
  if (!orderDetail.value || !orderDetail.value.payments) return [];
  return orderDetail.value.payments.filter(payment => payment.orderStatus === 'ORDER');
};

// 취소 환불 정보 필터링 (order_status = 'CANCELED')
const getCancelPayments = () => {
  if (!orderDetail.value || !orderDetail.value.payments) return [];
  return orderDetail.value.payments.filter(payment => payment.orderStatus === 'CANCELED');
};

// 주문 데이터 존재 여부
const hasOrderData = () => {
  return getOrderPayments().length > 0;
};

// 취소 데이터 존재 여부
const hasCancelData = () => {
  return getCancelPayments().length > 0;
};

// 주문 총액 계산
const getOrderTotalAmount = () => {
  return getOrderPayments().reduce((sum, payment) => sum + payment.amount, 0);
};

// 취소 총액 계산
const getCancelTotalAmount = () => {
  return getCancelPayments().reduce((sum, payment) => sum + payment.amount, 0);
};

// 취소 클레임 ID 가져오기
const getCancelClaimId = () => {
  const cancelPayment = getCancelPayments()[0];
  return cancelPayment?.claimId || 'N/A';
};

// 취소 날짜 가져오기
const getCancelDate = () => {
  const cancelPayment = getCancelPayments()[0];
  return cancelPayment?.paymentDate || orderDetail.value?.orderDate;
};

// 금액 포맷팅 (마이너스 옵션 포함)
const formatCurrency = (amount, isNegative = false) => {
  if (!amount && amount !== 0) return '0원';
  const formatted = new Intl.NumberFormat('ko-KR').format(Math.abs(amount)) + '원';
  return isNegative ? '-' + formatted : formatted;
};

// 금액 계산 함수들
const calculateTotalAmount = () => {
  if (!orderDetail.value?.orderItems) return 0;
  return orderDetail.value.orderItems.reduce((sum, item) =>
    sum + (item.priceAtOrder * item.quantity), 0);
};

const calculateFinalAmount = () => {
  if (!orderDetail.value?.payments) return 0;
  return orderDetail.value.payments
    .filter(payment => payment.payType === 'PAYMENT')
    .reduce((sum, payment) => sum + payment.amount, 0);
};

// 주문 취소 가능 여부
const canCancelOrder = computed(() => {
  if (!orderDetail.value) return false;
  const hasCancel = orderDetail.value.orderProcesses?.some(proc => proc.status === 'CANCELLED');
  return !hasCancel;
});

// 주문 취소 관련 함수들
const cancelOrder = () => {
  showCancelDialog.value = true;
};

const confirmCancelOrder = async () => {
  cancelling.value = true;
  try {
    const response = await fetch(`/api/orders/${orderId.value}/cancel`, {
      method: 'POST'
    });

    if (response.ok) {
      showCancelDialog.value = false;
      alert('주문이 성공적으로 취소되었습니다.');
      // 주문 정보 새로고침
      await fetchOrderDetail();
    } else {
      const errorText = await response.text();
      alert(`주문 취소 중 오류가 발생했습니다: ${errorText}`);
    }
  } catch (error) {
    console.error('Order cancellation error:', error);
    alert(`주문 취소 중 오류가 발생했습니다: ${error.message}`);
  } finally {
    cancelling.value = false;
  }
};

// 회원 페이지로 이동
const goToMemberPage = () => {
  if (orderDetail.value && orderDetail.value.memberId) {
    router.push(`/members/${orderDetail.value.memberId}`);
  } else {
    // 주문 정보가 없을 경우 기본 회원 ID로 이동 (임시)
    router.push('/members/1');
  }
};

// 컴포넌트 마운트 시 주문 상세 정보 조회
onMounted(() => {
  fetchOrderDetail();
});
</script>

<style scoped>
.order-complete-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}

.page-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.complete-content {
  padding: 40px 20px;
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.success-icon {
  margin-bottom: 24px;
}

.success-message {
  margin-bottom: 40px;
}

.success-message h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.success-message p {
  color: #666;
  font-size: 1rem;
  margin: 0;
}

.order-info-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  text-align: left;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  color: #666;
  font-weight: 500;
}

.value {
  color: #333;
  font-weight: 600;
}

.value.amount {
  color: #1976d2;
  font-size: 1.1rem;
}

.value.success {
  color: #4caf50;
}

.value.cancelled {
  color: #f44336;
}

/* 주문/취소 기록 카드 */
.order-record-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  text-align: left;
  border-left: 4px solid #4caf50;
}

.order-record-card.cancel-record {
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

/* 상품 목록 */
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

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  height: 48px;
  font-weight: 600;
}

/* 취소 확인 모달 */
.cancel-dialog {
  border-radius: 16px;
}

.dialog-title {
  background: #ffebee;
  color: #c62828;
  font-weight: 600;
  padding: 20px 24px 16px;
  display: flex;
  align-items: center;
}

.dialog-content {
  padding: 20px 24px;
  text-align: center;
}

.dialog-content p {
  margin-bottom: 12px;
  color: #333;
}

.warning-text {
  color: #f44336;
  font-weight: 500;
  font-size: 0.9rem;
}

.dialog-actions {
  padding: 16px 24px 24px;
  gap: 8px;
}

/* 반응형 */
@media (max-width: 768px) {
  .complete-content {
    padding: 24px 16px;
  }

  .order-info-card {
    padding: 20px;
  }

  .success-message h2 {
    font-size: 1.3rem;
  }
}
</style>
