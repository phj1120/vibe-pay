<template>
  <div class="payment-close">
    <v-card class="mx-auto" max-width="500">
      <v-card-title class="text-center">
        <v-icon color="warning" size="48" class="mb-2">
          mdi-close-circle
        </v-icon>
        <div class="text-h4">결제 취소</div>
      </v-card-title>
      
      <v-card-text class="text-center">
        <p class="text-h6 mb-4">결제가 취소되었습니다.</p>
        <p class="text-body-1 mb-4">
          결제창을 닫으셨거나 결제를 취소하셨습니다.
        </p>
        
        <v-divider class="my-4"></v-divider>
        
        <p class="text-body-2">
          다시 주문하시려면 아래 버튼을 클릭해주세요.
        </p>
      </v-card-text>
      
      <v-card-actions class="justify-center">
        <v-btn 
          color="primary" 
          size="large"
          @click="goToOrderPage"
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
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const goToOrderPage = () => {
  router.push('/order')
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  // 임시 주문 정보 정리 - 쿠키 삭제
  const pendingOrderCookie = useCookie('pendingOrder')
  pendingOrderCookie.value = null
})
</script>

<style scoped>
.payment-close {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
</style>
