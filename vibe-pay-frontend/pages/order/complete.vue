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
      <div class="order-info-card" v-if="orderInfo">
        <div class="info-row">
          <span class="label">주문번호</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <div class="info-row">
          <span class="label">주문일시</span>
          <span class="value">{{ formatDate(orderInfo.orderDate) }}</span>
        </div>
        <div class="info-row">
          <span class="label">결제금액</span>
          <span class="value amount">{{ formatCurrency(orderInfo.finalPaymentAmount) }}</span>
        </div>
        <div class="info-row">
          <span class="label">결제상태</span>
          <span class="value success">결제완료</span>
        </div>
      </div>

      <!-- 액션 버튼들 -->
      <div class="action-buttons">
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
    </div>
  </div>
</template>

<script setup>
const route = useRoute();
const router = useRouter();

const orderId = ref(route.query.orderId || '');
const orderInfo = ref(null);
const loading = ref(false);

// 주문 정보 조회
const fetchOrderInfo = async () => {
  if (!orderId.value) {
    console.error('Order number not provided');
    return;
  }

  loading.value = true;
  try {
    const response = await fetch(`/api/orders/${orderId.value}`);
    if (response.ok) {
      const orders = await response.json();
      // 배열에서 첫 번째 주문(원본 주문, ord_proc_seq=1)을 사용
      orderInfo.value = orders.length > 0 ? orders[0] : null;
      console.log('Order info loaded:', orderInfo.value);
    } else {
      console.error('Failed to fetch order info');
    }
  } catch (error) {
    console.error('Error fetching order info:', error);
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

// 금액 포맷팅
const formatCurrency = (amount) => {
  if (!amount) return '0원';
  return new Intl.NumberFormat('ko-KR').format(amount) + '원';
};

// 회원 페이지로 이동
const goToMemberPage = () => {
  if (orderInfo.value && orderInfo.value.memberId) {
    router.push(`/members/${orderInfo.value.memberId}`);
  } else {
    // 주문 정보가 없을 경우 기본 회원 ID로 이동 (임시)
    router.push('/members/1');
  }
};

// 컴포넌트 마운트 시 주문 정보 조회
onMounted(() => {
  fetchOrderInfo();
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

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  height: 48px;
  font-weight: 600;
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
