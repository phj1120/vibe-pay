<template>
  <div class="payment-return">
    <v-card class="mx-auto" max-width="600">
      <v-card-title class="text-center">
        <v-icon :color="statusColor" size="48" class="mb-2">
          {{ statusIcon }}
        </v-icon>
        <div class="text-h4">{{ statusTitle }}</div>
      </v-card-title>
      
      <v-card-text class="text-center">
        <p class="text-h6 mb-4">{{ statusMessage }}</p>
        
        <v-divider class="my-4"></v-divider>
        
        <div v-if="paymentInfo" class="payment-details">
          <h3 class="text-h6 mb-3">결제 정보</h3>
          <v-simple-table>
            <tbody>
              <tr>
                <td><strong>주문번호</strong></td>
                <td>{{ paymentInfo.oid }}</td>
              </tr>
              <tr>
                <td><strong>결제금액</strong></td>
                <td>{{ formatPrice(paymentInfo.price) }}원</td>
              </tr>
              <tr>
                <td><strong>결제수단</strong></td>
                <td>{{ paymentInfo.payMethod || '신용카드' }}</td>
              </tr>
              <tr v-if="paymentInfo.resultCode">
                <td><strong>결과코드</strong></td>
                <td>{{ paymentInfo.resultCode }}</td>
              </tr>
            </tbody>
          </v-simple-table>
        </div>
        
        <div v-if="errorMessage" class="error-message mt-4">
          <v-alert type="error" variant="outlined">
            {{ errorMessage }}
          </v-alert>
        </div>
      </v-card-text>
      
      <v-card-actions class="justify-center">
        <v-btn 
          color="primary" 
          size="large"
          @click="goToMemberPage"
          v-if="isSuccess"
        >
          주문 내역 보기
        </v-btn>
        <v-btn 
          color="secondary" 
          size="large"
          @click="goToOrderPage"
          v-else
        >
          다시 주문하기
        </v-btn>
        <v-btn 
          color="grey" 
          size="large"
          @click="goHome"
        >
          홈으로
        </v-btn>
      </v-card-actions>
    </v-card>
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
      
      localStorage.removeItem('pendingOrder')
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
.payment-return {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.payment-details {
  text-align: left;
}

.error-message {
  margin-top: 20px;
}
</style>
