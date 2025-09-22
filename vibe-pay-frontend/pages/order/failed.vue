<template>
  <div class="failed-page">
    <div class="failed-container">
      <!-- 실패 아이콘 -->
      <div class="failed-icon">
        <v-icon size="80" color="error">mdi-close-circle</v-icon>
      </div>

      <!-- 메인 메시지 -->
      <h1 class="failed-title">주문이 실패했습니다</h1>
      <p class="failed-message">{{ errorMessage }}</p>

      <!-- 상세 정보 -->
      <div class="failed-details" v-if="orderId">
        <div class="detail-row">
          <span class="label">주문번호:</span>
          <span class="value">{{ orderId }}</span>
        </div>
        <div class="detail-row">
          <span class="label">실패 시간:</span>
          <span class="value">{{ formatDateTime(new Date()) }}</span>
        </div>
      </div>

      <!-- 액션 버튼들 -->
      <div class="action-buttons">
        <v-btn
          color="primary"
          variant="elevated"
          size="large"
          @click="retryOrder"
          class="action-btn"
        >
          <v-icon left>mdi-refresh</v-icon>
          다시 주문하기
        </v-btn>

        <v-btn
          variant="outlined"
          size="large"
          @click="goHome"
          class="action-btn"
        >
          <v-icon left>mdi-home</v-icon>
          홈으로 돌아가기
        </v-btn>
      </div>

      <!-- 안내 메시지 -->
      <div class="help-section">
        <v-alert
          type="info"
          variant="tonal"
          class="help-alert"
        >
          <div class="help-content">
            <h4>문제가 지속될 경우</h4>
            <ul>
              <li>잠시 후 다시 시도해 주세요</li>
              <li>결제 정보를 다시 확인해 주세요</li>
              <li>문제가 계속되면 고객센터로 문의해 주세요</li>
            </ul>
          </div>
        </v-alert>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const errorMessage = ref('알 수 없는 오류가 발생했습니다.')
const orderId = ref('')

const formatDateTime = (date) => {
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const retryOrder = () => {
  router.push('/order')
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  if (route.query.error) {
    errorMessage.value = decodeURIComponent(route.query.error)
  }
  if (route.query.orderId) {
    orderId.value = route.query.orderId
  }
})
</script>

<style scoped>
.failed-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.failed-container {
  background: white;
  padding: 60px 40px;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  text-align: center;
  max-width: 500px;
  width: 100%;
}

.failed-icon {
  margin-bottom: 30px;
}

.failed-title {
  font-size: 2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.failed-message {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 40px;
  line-height: 1.5;
}

.failed-details {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 40px;
  text-align: left;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  font-weight: 500;
}

.value {
  color: #333;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 40px;
}

.action-btn {
  height: 50px !important;
  font-weight: 600 !important;
}

.help-section {
  text-align: left;
}

.help-alert {
  border-radius: 12px !important;
}

.help-content h4 {
  margin-bottom: 12px;
  color: #333;
}

.help-content ul {
  margin: 0;
  padding-left: 20px;
}

.help-content li {
  margin-bottom: 4px;
  color: #666;
}

@media (max-width: 600px) {
  .failed-container {
    padding: 40px 20px;
  }

  .failed-title {
    font-size: 1.5rem;
  }

  .failed-message {
    font-size: 1rem;
  }
}
</style>