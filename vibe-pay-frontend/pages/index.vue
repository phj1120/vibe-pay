<template>
  <div class="home-page">
    <!-- 헤더 -->
    <div class="header">
      <h1 class="app-title">VibePay</h1>
      <p class="app-subtitle">결제 시스템 데모</p>
    </div>

    <!-- 네비게이션 메뉴 -->
    <div class="nav-container">
      <div class="nav-grid">
        <v-card 
          class="nav-card"
          elevation="2"
          @click="goToMembers"
          hover
        >
          <v-card-text class="nav-content">
            <v-icon size="48" color="primary" class="mb-4">mdi-account-multiple</v-icon>
            <h3>회원 정보</h3>
            <p>회원 목록 및 정보 관리</p>
          </v-card-text>
        </v-card>
        
        <v-card 
          class="nav-card"
          elevation="2"
          @click="goToProducts"
          hover
        >
          <v-card-text class="nav-content">
            <v-icon size="48" color="success" class="mb-4">mdi-package-variant</v-icon>
            <h3>상품 정보</h3>
            <p>상품 목록 및 정보 관리</p>
          </v-card-text>
        </v-card>
        
        <v-card 
          class="nav-card"
          elevation="2"
          @click="goToOrder"
          hover
        >
          <v-card-text class="nav-content">
            <v-icon size="48" color="accent" class="mb-4">mdi-cart</v-icon>
            <h3>주문</h3>
            <p>새로운 주문 생성 및 결제</p>
          </v-card-text>
        </v-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToMembers = () => {
  router.push('/members')
}

const goToProducts = () => {
  router.push('/products')
}

const goToOrder = () => {
  router.push('/order')
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #fafafa;
  padding: 40px 20px;
}

.header {
  text-align: center;
  margin-bottom: 60px;
}

.app-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #0064FF;
  margin-bottom: 8px;
}

.app-subtitle {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

.nav-container {
  max-width: 800px;
  margin: 0 auto;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
}

.nav-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 16px !important;
}

.nav-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 100, 255, 0.15) !important;
}

.nav-content {
  text-align: center;
  padding: 40px 24px !important;
}

.nav-content h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.nav-content p {
  color: #666;
  line-height: 1.5;
  margin: 0;
}

@media (max-width: 768px) {
  .home-page {
    padding: 20px 16px;
  }
  
  .header {
    margin-bottom: 40px;
  }
  
  .app-title {
    font-size: 2rem;
  }
  
  .nav-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .nav-content {
    padding: 32px 20px !important;
  }
}
</style>