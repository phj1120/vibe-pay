<template>
  <div class="payment-result">
    <!-- 결과 표시 -->
    <div class="result-container">
      <div class="result-animation">
        <div class="result-icon" :class="{ success: isSuccess, error: !isSuccess }">
          <v-icon :color="statusColor" size="80">
            {{ statusIcon }}
          </v-icon>
        </div>
        <div class="result-ripple" v-if="isSuccess"></div>
      </div>
      
      <div class="result-content">
        <h1 class="result-title">{{ statusTitle }}</h1>
        <p class="result-message">{{ statusMessage }}</p>
      </div>
    </div>

    <!-- 결제 정보 카드 -->
    <div class="info-card" v-if="paymentInfo">
      <div class="info-header">
        <h3>결제 정보</h3>
        <div class="payment-badge" :class="{ success: isSuccess }">
          {{ isSuccess ? '결제완료' : '결제실패' }}
        </div>
      </div>
      
      <div class="info-content">
        <div class="info-row">
          <span class="info-label">주문번호</span>
          <span class="info-value">{{ paymentInfo.oid || '-' }}</span>
        </div>
        
        <div class="info-row highlight" v-if="paymentInfo.price">
          <span class="info-label">결제금액</span>
          <span class="info-value amount">₩{{ formatPrice(paymentInfo.price) }}</span>
        </div>
        
        <div class="info-row">
          <span class="info-label">결제수단</span>
          <span class="info-value">{{ getPaymentMethod(paymentInfo.payMethod) }}</span>
        </div>
        
        <div class="info-row">
          <span class="info-label">결제일시</span>
          <span class="info-value">{{ getCurrentDateTime() }}</span>
        </div>
      </div>
    </div>

    <!-- 에러 메시지 -->
    <div class="error-card" v-if="errorMessage">
      <div class="error-content">
        <v-icon color="error" class="mb-2">mdi-alert-circle</v-icon>
        <p>{{ errorMessage }}</p>
      </div>
    </div>

    <!-- 액션 버튼들 -->
    <div class="action-buttons">
      <template v-if="isSuccess">
        <v-btn 
          color="primary" 
          size="x-large"
          block
          rounded="xl"
          class="action-btn primary-btn"
          @click="goToMemberPage"
        >
          <v-icon left>mdi-receipt</v-icon>
          주문 내역 보기
        </v-btn>
        
        <v-btn 
          variant="outlined"
          color="primary"
          size="large"
          block
          rounded="xl"
          class="action-btn secondary-btn"
          @click="goToOrderPage"
        >
          <v-icon left>mdi-cart</v-icon>
          다시 주문하기
        </v-btn>
      </template>
      
      <template v-else>
        <v-btn 
          color="primary" 
          size="x-large"
          block
          rounded="xl"
          class="action-btn primary-btn"
          @click="goToOrderPage"
        >
          <v-icon left>mdi-refresh</v-icon>
          다시 시도하기
        </v-btn>
      </template>
      
      <v-btn 
        variant="text"
        color="grey"
        size="large"
        block
        class="action-btn text-btn"
        @click="goHome"
      >
        <v-icon left>mdi-home</v-icon>
        홈으로 돌아가기
      </v-btn>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// API에서 리다이렉트된 결과를 처리

const paymentInfo = ref(null)
const errorMessage = ref('')
const isProcessing = ref(true)

const isSuccess = computed(() => {
  return route.query.success === 'true' || paymentInfo.value?.resultCode === '0000'
})

const statusColor = computed(() => {
  return isSuccess.value ? 'success' : 'error'
})

const statusIcon = computed(() => {
  return isSuccess.value ? 'mdi-check-circle' : 'mdi-alert-circle'
})

const statusTitle = computed(() => {
  return isSuccess.value ? '결제 완료' : '결제 실패'
})

const statusMessage = computed(() => {
  if (isProcessing.value) {
    return '결제 결과를 확인하는 중입니다...'
  }
  return isSuccess.value 
    ? '결제가 성공적으로 완료되었습니다.' 
    : '결제 처리 중 오류가 발생했습니다.'
})

const formatPrice = (price) => {
  if (!price) return '0'
  return parseInt(price).toLocaleString()
}

const getPaymentMethod = (method) => {
  const methods = {
    'CREDIT_CARD': '신용카드',
    'DEBIT_CARD': '체크카드',
    'BANK_TRANSFER': '계좌이체',
    'VIRTUAL_ACCOUNT': '가상계좌'
  }
  return methods[method] || '신용카드'
}

const getCurrentDateTime = () => {
  return new Date().toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const processPaymentReturn = async () => {
  try {
    console.log('Processing payment return...')
    
    // API에서 리다이렉트된 쿼리 파라미터 확인
    const success = route.query.success === 'true'
    const paymentId = route.query.paymentId
    const oid = route.query.oid
    const price = route.query.price
    const resultCode = route.query.resultCode
    const resultMsg = route.query.resultMsg
    const error = route.query.error
    
    console.log('Payment return parameters:', {
      success,
      paymentId,
      oid,
      price,
      resultCode,
      resultMsg,
      error
    })
    
    if (success) {
      // 결제 성공
      paymentInfo.value = {
        resultCode: '0000',
        resultMsg: '결제가 성공적으로 완료되었습니다.',
        oid: oid || '',
        price: price || '',
        payMethod: 'CREDIT_CARD',
        authToken: '',
        authUrl: '',
        netCancelUrl: '',
        mid: '',
        orderId: paymentId
      }
      
      // 쿠키 삭제
      const pendingOrderCookie = useCookie('pendingOrder')
      pendingOrderCookie.value = null
      console.log('Payment completed successfully!')
    } else {
      // 결제 실패
      const errorMsg = error || resultMsg || '결제가 실패했습니다.'
      errorMessage.value = errorMsg
      paymentInfo.value = {
        resultCode: resultCode || '9999',
        resultMsg: errorMsg,
        oid: oid || '',
        price: price || '',
        payMethod: 'CREDIT_CARD',
        authToken: '',
        authUrl: '',
        netCancelUrl: '',
        mid: ''
      }
    }
  } catch (error) {
    console.error('Payment processing error:', error)
    errorMessage.value = '결제 처리 중 오류가 발생했습니다: ' + error.message
    paymentInfo.value = {
      resultCode: '9999',
      resultMsg: '결제 처리 오류',
      oid: '', price: '', payMethod: '', authToken: '', authUrl: '', netCancelUrl: '', mid: ''
    }
  } finally {
    isProcessing.value = false
  }
}

// confirmPaymentWithBackend와 createOrder 함수는 이제 서버사이드 API에서 처리됩니다.

const goToMemberPage = () => {
  if (paymentInfo.value?.memberId) {
    router.push(`/members/${paymentInfo.value.memberId}`)
  } else {
    router.push('/members')
  }
}

const goToOrderPage = () => {
  router.push('/order')
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  processPaymentReturn()
})
</script>

<style scoped>
.payment-result {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  padding: 20px;
}

/* 결과 컨테이너 */
.result-container {
  text-align: center;
  padding: 60px 0;
  color: white;
}

.result-animation {
  position: relative;
  margin-bottom: 40px;
  display: inline-block;
}

.result-icon {
  position: relative;
  z-index: 2;
  width: 120px;
  height: 120px;
  border-radius: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  animation: bounceIn 0.6s ease-out;
}

.result-icon.success {
  background: rgba(0, 200, 150, 0.2);
  backdrop-filter: blur(10px);
  border: 2px solid #00C896;
}

.result-icon.error {
  background: rgba(255, 71, 87, 0.2);
  backdrop-filter: blur(10px);
  border: 2px solid #FF4757;
}

.result-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 2px solid #00C896;
  animation: ripple 2s infinite;
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes ripple {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(2);
    opacity: 0;
  }
}

.result-content {
  max-width: 400px;
  margin: 0 auto;
}

.result-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 12px;
}

.result-message {
  font-size: 1.1rem;
  opacity: 0.9;
  line-height: 1.6;
}

/* 정보 카드 */
.info-card {
  background: white;
  border-radius: 20px;
  margin: 20px auto;
  max-width: 500px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.info-header h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
}

.payment-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  background: #f5f5f5;
  color: #666;
}

.payment-badge.success {
  background: rgba(0, 200, 150, 0.1);
  color: #00C896;
}

.info-content {
  padding: 24px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f8f9fa;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row.highlight {
  background: rgba(0, 100, 255, 0.05);
  margin: 0 -24px;
  padding: 16px 24px;
  border-radius: 12px;
  border: none;
}

.info-label {
  color: #666;
  font-weight: 500;
}

.info-value {
  color: #333;
  font-weight: 600;
}

.info-value.amount {
  color: #0064FF;
  font-size: 1.25rem;
}

/* 에러 카드 */
.error-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  margin: 20px auto;
  max-width: 500px;
  backdrop-filter: blur(10px);
}

.error-content {
  padding: 24px;
  text-align: center;
  color: #FF4757;
}

/* 액션 버튼들 */
.action-buttons {
  margin-top: auto;
  padding: 20px 0;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
  width: 100%;
}

.action-btn {
  margin-bottom: 12px;
  font-weight: 600;
}

.primary-btn {
  height: 56px !important;
  font-size: 1.1rem !important;
  background: rgba(255, 255, 255, 0.95) !important;
  color: #0064FF !important;
  backdrop-filter: blur(10px);
}

.secondary-btn {
  height: 48px !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.3) !important;
  color: white !important;
  backdrop-filter: blur(10px);
}

.text-btn {
  height: 44px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

/* 반응형 */
@media (max-width: 768px) {
  .payment-result {
    padding: 16px;
  }
  
  .result-container {
    padding: 40px 0;
  }
  
  .result-title {
    font-size: 1.75rem;
  }
  
  .result-message {
    font-size: 1rem;
  }
  
  .info-card,
  .error-card {
    margin: 16px auto;
  }
  
  .info-header,
  .info-content,
  .error-content {
    padding: 20px;
  }
  
  .info-row.highlight {
    margin: 0 -20px;
    padding: 16px 20px;
  }
}
</style>
