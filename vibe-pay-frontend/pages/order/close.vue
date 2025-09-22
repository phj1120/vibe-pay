<template>
  <div class="close-wrap">
    <div class="close-card">
      <div class="close-icon">🔒</div>
      <h2>결제 창 닫기</h2>
      <p>결제가 취소되었습니다.<br>잠시 후 이 창이 자동으로 닫힙니다.</p>
      <div class="countdown">{{ countdown }}초 후 닫기</div>
      <button class="close-btn" @click="closeWindow">지금 닫기</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const countdown = ref(3)

const closeWindow = () => {
  try {
    // 부모창에 결제 취소 알림
    if (window.opener) {
      window.opener.postMessage({
        type: 'PAYMENT_CANCELLED',
        data: { reason: 'user_cancelled' }
      }, '*')
    }
    window.close()
  } catch (error) {
    console.error('창 닫기 실패:', error)
    alert('창을 수동으로 닫아주세요.')
  }
}

onMounted(() => {
  console.log('Payment close page mounted')

  // 임시 주문 정보 정리 - 쿠키 삭제
  try {
    const pendingOrderCookie = useCookie('pendingOrder')
    pendingOrderCookie.value = null
  } catch (error) {
    console.log('쿠키 정리 실패:', error)
  }

  // 3초 카운트다운
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      closeWindow()
    }
  }, 1000)

  // 페이지가 언마운트될 때 타이머 정리
  onUnmounted(() => {
    clearInterval(timer)
  })
})
</script>

<style scoped>
.close-wrap {
  height: 100vh;
  display: grid;
  place-items: center;
  background: #0f172a;
}

.close-card {
  width: min(400px, 90vw);
  padding: 32px 24px;
  border-radius: 16px;
  background: #1f2937;
  color: #e5e7eb;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,.4);
}

.close-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

h2 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 12px;
  color: #f9fafb;
}

p {
  line-height: 1.5;
  margin-bottom: 20px;
  color: #d1d5db;
}

.countdown {
  font-size: 1.1rem;
  font-weight: 600;
  color: #fbbf24;
  margin-bottom: 20px;
}

.close-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  border: 1px solid #374151;
  background: #374151;
  color: #e5e7eb;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #4b5563;
  border-color: #4b5563;
}

.close-btn:active {
  transform: translateY(1px);
}
</style>
