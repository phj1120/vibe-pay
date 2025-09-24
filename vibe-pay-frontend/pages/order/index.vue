<template>
  <div class="order-page">
    <!-- 헤더 -->
    <div class="page-header">
      <v-btn icon @click="$router.go(-1)" class="back-btn">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <h1 class="page-title">주문하기</h1>
    </div>

    <!-- 메인 컨텐츠 -->
    <div class="order-content">
      <!-- 회원 선택 -->
      <div class="section-card">
        <div class="section-header">
          <v-icon color="primary" class="mr-3">mdi-account</v-icon>
          <h3>주문자 정보</h3>
        </div>
        <v-select
          v-model="selectedMember"
          :items="members"
          item-title="name"
          item-value="id"
          return-object
          variant="outlined"
          placeholder="주문자를 선택해주세요"
          class="member-select"
        >
          <template v-slot:selection="{ item }">
            <div class="member-info">
              <div class="member-avatar">
                {{ item.raw.name.charAt(0) }}
              </div>
              <div class="member-details">
                <div class="member-name">{{ item.raw.name }}</div>
                <div class="member-points">{{ item.raw.points || 0 }}P 보유</div>
              </div>
            </div>
          </template>
          <template v-slot:item="{ item, props }">
            <v-list-item v-bind="props">
              <template v-slot:prepend>
                <div class="member-avatar small">
                  {{ item.raw.name.charAt(0) }}
                </div>
              </template>
              <v-list-item-title>{{ item.raw.name }}</v-list-item-title>
              <v-list-item-subtitle>{{ item.raw.points || 0 }}P 보유</v-list-item-subtitle>
            </v-list-item>
          </template>
        </v-select>
      </div>

      <!-- 상품 선택 -->
      <div class="section-card">
        <div class="section-header">
          <v-icon color="success" class="mr-3">mdi-shopping</v-icon>
          <h3>상품 선택</h3>
        </div>
        <v-select
          v-model="selectedProduct"
          :items="products"
          item-title="name"
          item-value="productId"
          return-object
          variant="outlined"
          placeholder="상품을 선택해주세요"
          @update:modelValue="addProductToOrder"
          class="product-select"
        >
          <template v-slot:selection="{ item }">
            <div class="product-info">
              <div class="product-name">{{ item.raw.name }}</div>
              <div class="product-price">₩{{ item.raw.price.toLocaleString() }}</div>
            </div>
          </template>
          <template v-slot:item="{ item, props }">
            <v-list-item v-bind="props">
              <v-list-item-title>{{ item.raw.name }}</v-list-item-title>
              <v-list-item-subtitle>₩{{ item.raw.price.toLocaleString() }}</v-list-item-subtitle>
            </v-list-item>
          </template>
        </v-select>
      </div>

      <!-- 주문 상품 목록 -->
      <div class="section-card" v-if="orderItems.length > 0">
        <div class="section-header">
          <v-icon color="accent" class="mr-3">mdi-cart</v-icon>
          <h3>주문 상품</h3>
        </div>
        <div class="order-items">
          <div 
            v-for="item in orderItems" 
            :key="item.productId"
            class="order-item"
          >
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-price">₩{{ item.price.toLocaleString() }}</div>
            </div>
            <div class="item-controls">
              <div class="quantity-control">
                <v-btn 
                  icon 
                  size="small" 
                  @click="updateQuantity(item, -1)"
                  :disabled="item.quantity <= 1"
                >
                  <v-icon size="16">mdi-minus</v-icon>
                </v-btn>
                <span class="quantity">{{ item.quantity }}</span>
                <v-btn 
                  icon 
                  size="small" 
                  @click="updateQuantity(item, 1)"
                >
                  <v-icon size="16">mdi-plus</v-icon>
                </v-btn>
              </div>
              <v-btn 
                icon 
                size="small" 
                color="error"
                @click="removeProductFromOrder(item)"
              >
                <v-icon size="16">mdi-delete</v-icon>
              </v-btn>
            </div>
            <div class="item-subtotal">
              ₩{{ (item.quantity * item.price).toLocaleString() }}
            </div>
          </div>
        </div>
      </div>

      <!-- 포인트 사용 -->
      <div class="section-card" v-if="selectedMember">
        <div class="section-header">
          <v-icon color="warning" class="mr-3">mdi-star</v-icon>
          <h3>포인트 사용</h3>
        </div>
        <div class="points-section">
          <div class="points-info">
            <span>보유 포인트: <strong>{{ selectedMember.points || 0 }}P</strong></span>
          </div>
          <div class="points-input">
            <v-text-field
              v-model.number="usedPoints"
              type="number"
              min="0"
              :max="selectedMember.points || 0"
              variant="outlined"
              placeholder="사용할 포인트"
              suffix="P"
              class="points-field"
            >
              <template v-slot:append-inner>
                <v-btn
                  size="small"
                  color="primary"
                  @click="useAllPoints"
                  :disabled="!selectedMember.points"
                >
                  전액사용
                </v-btn>
              </template>
            </v-text-field>
          </div>
        </div>
      </div>

      <!-- PG사 선택 -->
      <div class="section-card" v-if="orderItems.length > 0">
        <div class="section-header">
          <v-icon color="info" class="mr-3">mdi-credit-card-outline</v-icon>
          <h3>결제 방식</h3>
        </div>
        <div class="pg-selection">
          <v-radio-group
            v-model="selectedPgCompany"
            color="primary"
            class="pg-radio-group"
          >
            <v-radio
              value="INICIS"
              class="pg-radio"
            >
              <template v-slot:label>
                <div class="pg-option">
                  <div class="pg-logo">
                    <v-icon size="24" color="primary">mdi-bank</v-icon>
                  </div>
                  <div class="pg-info">
                    <div class="pg-name">이니시스</div>
                    <div class="pg-desc">KG 이니시스 결제</div>
                  </div>
                </div>
              </template>
            </v-radio>
            <v-radio
              value="NICEPAY"
              class="pg-radio"
            >
              <template v-slot:label>
                <div class="pg-option">
                  <div class="pg-logo">
                    <v-icon size="24" color="success">mdi-credit-card</v-icon>
                  </div>
                  <div class="pg-info">
                    <div class="pg-name">나이스페이</div>
                    <div class="pg-desc">NICE Payment 결제</div>
                  </div>
                </div>
              </template>
            </v-radio>
          </v-radio-group>
        </div>
      </div>

      <!-- 망취소 테스트 -->
      <div class="section-card" v-if="orderItems.length > 0">
        <div class="section-header">
          <v-icon color="warning" class="mr-3">mdi-test-tube</v-icon>
          <h3>테스트 옵션</h3>
        </div>
        <div class="test-section">
          <v-checkbox
            v-model="netCancel"
            color="warning"
            class="test-checkbox"
          >
            <template v-slot:label>
              <span class="test-text">
                <span>[테스트]</span> 망취소 테스트 (주문 생성 실패 시뮬레이션)
              </span>
            </template>
          </v-checkbox>
        </div>
      </div>

      <!-- 주문 요약 정보 (일반 영역) -->
      <div class="section-card" v-if="orderItems.length > 0">
        <div class="section-header">
          <v-icon color="accent" class="mr-3">mdi-calculator</v-icon>
          <h3>주문 요약</h3>
        </div>
        <div class="price-breakdown">
          <div class="price-row">
            <span>상품 금액</span>
            <span>₩{{ subtotal.toLocaleString() }}</span>
          </div>
          <div class="price-row" v-if="usedPoints > 0">
            <span>포인트 할인</span>
            <span class="discount">-₩{{ usedPoints.toLocaleString() }}</span>
          </div>
          <div class="price-row total">
            <span>총 결제금액</span>
            <span>₩{{ total.toLocaleString() }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 결제 실행 (하단 고정) -->
    <div class="order-summary-fixed" v-if="orderItems.length > 0 && !isProcessing">
      <div class="summary-content">
        <!-- 약관 동의 -->
        <div class="terms-section">
          <v-checkbox
            v-model="agreedToTerms"
            color="primary"
            class="terms-checkbox"
          >
            <template v-slot:label>
              <span class="terms-text">
                <span>[필수]</span> 구매조건 확인 및 결제서비스 약관에 동의합니다.
              </span>
            </template>
          </v-checkbox>
        </div>

        <v-btn
          color="primary"
          size="x-large"
          block
          rounded="xl"
          class="pay-btn"
          @click="proceedToPayment"
          :disabled="!selectedMember || orderItems.length === 0 || isProcessing || !agreedToTerms || !selectedPgCompany"
          :loading="isProcessing"
        >
          <v-icon left>mdi-credit-card</v-icon>
          {{ isProcessing ? '처리중...' : `₩${total.toLocaleString()} 결제하기` }}
        </v-btn>
      </div>
    </div>

    <!-- 빈 상태 -->
    <div class="empty-state" v-if="orderItems.length === 0">
      <v-icon size="80" color="grey-lighten-2">mdi-cart-outline</v-icon>
      <h3>장바구니가 비어있습니다</h3>
      <p>상품을 선택해서 주문을 시작해보세요</p>
    </div>

    <!-- 최소 결제 금액 모달 -->
    <v-dialog v-model="showMinPaymentDialog" max-width="400" persistent>
      <v-card class="min-payment-dialog">
        <v-card-title class="dialog-title">
          <v-icon color="warning" class="mr-2">mdi-alert-circle</v-icon>
          결제 불가
        </v-card-title>
        <v-card-text class="dialog-content">
          <p>카드로 결제할 금액이 100원 이하입니다.</p>
          <p class="error-detail">100원 이하는 결제가 불가능합니다.</p>
          <p class="suggestion">포인트 사용량을 조정하거나 상품을 추가해주세요.</p>
        </v-card-text>
        <v-card-actions class="dialog-actions">
          <v-btn
            color="primary"
            block
            @click="showMinPaymentDialog = false"
          >
            확인
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'

const route = useRoute()
const router = useRouter()

// 반응형 데이터
const members = ref([])
const products = ref([])
const selectedMember = ref(null)
const selectedProduct = ref(null)
const orderItems = ref([])
const usedPoints = ref(0)
const isProcessing = ref(false)
const agreedToTerms = ref(false)
const showMinPaymentDialog = ref(false)
const netCancel = ref(false)
const selectedPgCompany = ref('INICIS')



// 회원 변경 시 포인트 조회
watch(selectedMember, async (newMember) => {
  if (newMember) {
    try {
      const response = await fetch(`/api/rewardpoints/member/${newMember.memberId}`);
      if (!response.ok) throw new Error('Failed to fetch points');
      const data = await response.json();
      newMember.points = data.points;
    } catch (error) {
      console.error(error);
      newMember.points = 0;
    }
  } else {
    usedPoints.value = 0;
  }
});

// 계산된 속성
const subtotal = computed(() => {
  return orderItems.value.reduce((acc, item) => acc + (item.quantity * item.price), 0)
})

const total = computed(() => {
  const finalTotal = subtotal.value - usedPoints.value
  return finalTotal > 0 ? finalTotal : 0
})

// 메서드
const fetchInitialData = async () => {
  try {
    const [membersRes, productsRes] = await Promise.all([
      fetch('/api/members'),
      fetch('/api/products'),
    ]);
    
    if (!membersRes.ok || !productsRes.ok) {
      throw new Error('Failed to fetch initial data');
    }
    
    const membersData = await membersRes.json();
    members.value = membersData.map(m => ({...m, points: 0}));
    products.value = await productsRes.json();

    // URL 쿼리에서 회원 ID 확인
    const memberIdFromQuery = route.query.memberId;
    if (memberIdFromQuery) {
      const foundMember = members.value.find(m => m.memberId === parseInt(memberIdFromQuery));
      if (foundMember) {
        selectedMember.value = foundMember;
      }
    }
  } catch (error) {
    console.error(error);
  }
};

const addProductToOrder = (product) => {
  if (!product) return
  const existingItem = orderItems.value.find(item => item.productId === product.productId)
  if (existingItem) {
    existingItem.quantity++
  } else {
    orderItems.value.push({...product, quantity: 1})
  }
  selectedProduct.value = null
}

const updateQuantity = (item, change) => {
  item.quantity += change
  if (item.quantity <= 0) {
    removeProductFromOrder(item)
  }
}

const removeProductFromOrder = (itemToRemove) => {
  orderItems.value = orderItems.value.filter(item => item.productId !== itemToRemove.productId)
}

const useAllPoints = () => {
  if (selectedMember.value && selectedMember.value.points) {
    usedPoints.value = Math.min(selectedMember.value.points, subtotal.value)
  }
}

// 현재 주문 데이터 (팝업에서 참조)
const currentOrderData = ref(null);

const proceedToPayment = async () => {
  if (isProcessing.value) return;

  // 유효성 검사
  if (!selectedMember.value) {
    alert("주문자를 선택해주세요.");
    return;
  }
  if (usedPoints.value > (selectedMember.value.points || 0)) {
    alert("보유 포인트보다 많이 사용할 수 없습니다.");
    return;
  }
  if (orderItems.value.length === 0) {
    alert("주문할 상품을 선택해주세요.");
    return;
  }
  if (!agreedToTerms.value) {
    alert("구매조건 확인 및 결제서비스 약관에 동의해주세요.");
    return;
  }
  if (!selectedPgCompany.value) {
    alert("결제 방식을 선택해주세요.");
    return;
  }

  // 카드 결제 금액 100원 이하 체크
  const cardPaymentAmount = total.value; // 카드로 결제할 금액 (포인트 차감 후)
  if (cardPaymentAmount > 0 && cardPaymentAmount <= 100) {
    showMinPaymentDialog.value = true;
    return;
  }

  try {
    isProcessing.value = true;
    console.log('Starting payment process...');

    // 1. 주문번호 채번
    console.log('Generating order number...');
    const orderNumberResponse = await fetch('/api/orders/generateOrderNumber');
    if (!orderNumberResponse.ok) {
      throw new Error('주문번호 생성에 실패했습니다.');
    }
    const orderId = await orderNumberResponse.text();
    console.log('Generated order ID:', orderId);

    // 주문 정보 저장 (orderId 포함)
    const orderData = {
      orderId: orderId,
      memberId: selectedMember.value.memberId,
      items: orderItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      })),
      usedPoints: Math.round(usedPoints.value),
      totalAmount: Math.round(subtotal.value),
      finalPaymentAmount: Math.round(total.value),
      netCancel: netCancel.value // 망취소 테스트 플래그 추가
    };

    // 쿠키에 주문 정보 저장 (SSR에서 읽을 수 있도록)
    const pendingOrderCookie = useCookie('pendingOrder', {
      maxAge: 60 * 60, // 1시간
      secure: false, // 개발환경
      sameSite: 'none', // 더 관대한 설정
      httpOnly: false,
      domain: undefined, // 도메인 제한 없음
      path: '/' // 전체 경로에서 접근 가능
    });

    console.log('=== Cookie Save Debug ===');
    console.log('Order data to save:', orderData);

    pendingOrderCookie.value = JSON.stringify(orderData);

    console.log('Cookie saved:', pendingOrderCookie.value);

    // 상품명 생성
    const productNames = orderItems.value.map(item => item.name).join(', ');
    const goodName = productNames.length > 50 ? productNames.substring(0, 47) + '...' : productNames;

    const initiatePayload = {
      memberId: selectedMember.value.memberId,
      amount: Math.round(total.value), // 정수로 변환
      paymentMethod: 'CREDIT_CARD',
      pgCompany: selectedPgCompany.value, // 선택된 PG사 추가
      usedMileage: Math.round(usedPoints.value), // 적립금도 정수로 변환
      goodName: goodName,
      buyerName: selectedMember.value.name,
      buyerTel: selectedMember.value.phoneNumber,
      buyerEmail: selectedMember.value.email,
      orderId: orderId,
    };

    // 결제 준비
    console.log('Sending payment initiate request:', initiatePayload);

    const initiateResponse = await fetch('/api/payments/initiate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(initiatePayload),
    });

    console.log('Payment initiate response status:', initiateResponse.status);

    if (!initiateResponse.ok) {
      let errorText = '';
      try {
        const errorData = await initiateResponse.json();
        errorText = JSON.stringify(errorData);
        console.error('Payment initiate error data:', errorData);
      } catch (e) {
        errorText = await initiateResponse.text();
        console.error('Payment initiate error text:', errorText);
      }
      throw new Error(`Payment initiation failed (${initiateResponse.status}): ${errorText}`);
    }

    const paymentInitResponse = await initiateResponse.json();
    console.log('Payment initiated:', paymentInitResponse);

    // 주문 데이터 저장
    currentOrderData.value = orderData;

    // 팝업 열기 (PG사별 다른 크기)
    console.log('Opening payment popup...');
    const popupSize = selectedPgCompany.value === 'NICEPAY'
      ? 'width=570,height=830,scrollbars=yes,resizable=yes'
      : 'width=840,height=600,scrollbars=yes,resizable=yes';

    const popup = window.open(
      '/order/popup',
      'payment',
      popupSize
    );

    if (!popup) {
      throw new Error('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.');
    }

    // 팝업이 로드될 때까지 기다린 후 데이터 전송
    const sendPaymentParams = () => {
      console.log('Sending payment params to popup:', paymentInitResponse);
      popup.postMessage({
        type: 'PAYMENT_PARAMS',
        data: {
          ...paymentInitResponse,
          pgCompany: selectedPgCompany.value // PG사 정보 추가
        }
      }, '*');
    };

    // 팝업에서 메시지 수신 대기
    const handleMessage = (event) => {
      console.log('Received message from popup:', event.data);

      if (event.data.type === 'POPUP_READY') {
        console.log('Popup is ready, sending payment params');
        sendPaymentParams();
      } else if (event.data.type === 'PAYMENT_RESULT') {
        // progress-popup.vue에서 전달받은 결제 결과 처리
        console.log('Received PAYMENT_RESULT from progress-popup');
        if (event.data.data.success) {
          clearInterval(checkClosed);
          window.removeEventListener('message', handleMessage);

          // 팝업이 닫힐 때까지 기다린 후 주문 생성 처리
          const waitForPopupClose = setInterval(() => {
            if (popup.closed) {
              clearInterval(waitForPopupClose);
              // 주문 생성 후 결과에 따라 페이지 이동
              handleOrderCreation(event.data.data);
            }
          }, 100);
        } else {
          clearInterval(checkClosed);
          window.removeEventListener('message', handleMessage);
          handlePaymentError(event.data.data.resultMsg || '결제가 실패했습니다.');
        }
      } else if (event.data.type === 'PAYMENT_ERROR') {
        clearInterval(checkClosed);
        window.removeEventListener('message', handleMessage);
        handlePaymentError(event.data.error);
      } else if (event.data.type === 'PAYMENT_CANCELLED') {
        console.log('Payment cancelled by user');
        clearInterval(checkClosed);
        window.removeEventListener('message', handleMessage);
        isProcessing.value = false;

        // 쿠키 정리
        const pendingOrderCookie = useCookie('pendingOrder');
        pendingOrderCookie.value = null;

        // 사용자에게 알림 (선택사항)
        // alert('결제가 취소되었습니다.');
      }
    };

    window.addEventListener('message', handleMessage);

    // 안전을 위해 1초 후에도 데이터 전송 (팝업이 준비되지 않았을 경우를 대비)
    setTimeout(() => {
      sendPaymentParams();
    }, 1000);

    // 팝업이 닫힌 경우 처리 (3초 딜레이 후 체크)
    const checkClosed = setInterval(() => {
      if (popup.closed) {
        clearInterval(checkClosed);
        // 3초 후에 메시지가 처리되지 않았으면 취소로 간주
        setTimeout(() => {
          window.removeEventListener('message', handleMessage);
          isProcessing.value = false;
          console.log('Payment popup was closed - likely cancelled by user');
          alert('결제 창이 닫혔습니다. 결제를 다시 시도해주세요.');
        }, 3000);
      }
    }, 1000);

  } catch (error) {
    console.error('Payment process error:', error);
    alert(`결제 시작 중 오류가 발생했습니다: ${error.message}`);
    isProcessing.value = false;
  }
}

// 주문 생성 처리 (결제 성공 후)
const handleOrderCreation = async (paymentData) => {
  console.log('Processing order creation:', paymentData);

  try {
    // 주문 생성 요청 - 저장된 주문 정보와 결제 정보를 함께 전송
    console.log('Creating order after successful payment...');
    console.log('Payment data received:', paymentData);
    console.log('Current order data:', currentOrderData.value);

    if (!currentOrderData.value) {
      throw new Error('주문 정보를 찾을 수 없습니다.');
    }

    // 결제 방법 배열 구성
    const paymentMethods = [];

    // 포인트 사용이 있으면 포인트 결제 추가
    if (currentOrderData.value.usedPoints > 0) {
      paymentMethods.push({
        paymentMethod: 'POINT',
        amount: currentOrderData.value.usedPoints,
        authToken: paymentData.authToken,
        authUrl: paymentData.authUrl,
        mid: paymentData.mid,
        netCancelUrl: paymentData.netCancelUrl,
      });
    }

    // 카드 결제 금액이 있으면 카드 결제 추가 (포인트 차감 후 잔여 금액)
    const cardPaymentAmount = currentOrderData.value.finalPaymentAmount;
    if (cardPaymentAmount > 0) {
      paymentMethods.push({
        paymentMethod: 'CREDIT_CARD',
        pgCompany: paymentData.pgCompany, // PG사 정보 추가
        amount: cardPaymentAmount,
        authToken: paymentData.authToken,
        nextAppUrl: paymentData.nextAppURL,
        mid: paymentData.mid,
        netCancelUrl: paymentData.netCancelUrl,
        txTid: paymentData.txTid // txTid 추가
      });
    }

    const orderRequestPayload = {
      // 주문 기본 정보 (OrderRequest 필드와 일치)
      orderNumber: currentOrderData.value.orderId,
      memberId: currentOrderData.value.memberId,
      items: currentOrderData.value.items, // { productId: Long, quantity: Integer }
      netCancel: currentOrderData.value.netCancel, // 저장된 망취소 테스트 플래그 사용

      // 결제 방법 정보 (새로운 구조 - 결제 정보 포함)
      paymentMethods: paymentMethods
    };

    console.log('=== Order Request Debug ===');
    console.log('paymentData:', paymentData);
    console.log('currentOrderData.value:', currentOrderData.value);
    console.log('Generated paymentMethods:', paymentMethods);
    console.log('Final price used:', orderRequestPayload.price, typeof orderRequestPayload.price);

    console.log('Sending order creation request:', orderRequestPayload);

    const orderResponse = await fetch('/api/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(orderRequestPayload),
    });

    if (orderResponse.ok) {
      const orderResult = await orderResponse.json();
      console.log('Order created successfully:', orderResult);

      // 주문 생성 성공 시 완료 페이지로 이동 (첫 번째 주문의 orderId 사용)
      const firstOrder = Array.isArray(orderResult) ? orderResult[0] : orderResult;
      router.push(`/order/complete?orderId=${firstOrder.orderId || paymentData.orderNumber}`);
    } else {
      const errorText = await orderResponse.text();
      console.error('Order creation failed:', errorText);

      // 주문 생성 실패 시 실패 페이지로 이동
      router.push(`/order/failed?error=${encodeURIComponent('주문 생성 중 오류가 발생했습니다.')}`);
    }
  } catch (error) {
    console.error('Order creation error:', error);

    // 주문 처리 중 오류 발생 시 실패 페이지로 이동
    router.push(`/order/failed?error=${encodeURIComponent(error.message || '주문 처리 중 오류가 발생했습니다.')}`);
  } finally {
    isProcessing.value = false;
  }
}

// 결제 오류 처리
const handlePaymentError = (error) => {
  console.error('Payment error:', error);
  alert(`결제 처리 중 오류가 발생했습니다: ${error}`);
  isProcessing.value = false;
  router.push('/order');
}

onMounted(() => {
  fetchInitialData()
})
</script>

<style scoped>
.order-page {
  min-height: 100vh;
  background-color: #fafafa;
  padding-bottom: 200px;
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
}

.order-content {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.section-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

/* 회원 정보 */
.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  background: #0064FF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.member-avatar.small {
  width: 32px;
  height: 32px;
  border-radius: 16px;
}

.member-details {
  flex: 1;
}

.member-name {
  font-weight: 600;
  color: #333;
}

.member-points {
  font-size: 0.875rem;
  color: #666;
}

/* 상품 정보 */
.product-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.product-name {
  font-weight: 500;
  color: #333;
}

.product-price {
  font-weight: 600;
  color: #0064FF;
}

/* 주문 상품 목록 */
.order-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.item-price {
  font-size: 0.875rem;
  color: #666;
}

.item-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  border-radius: 8px;
  padding: 4px;
}

.quantity {
  min-width: 24px;
  text-align: center;
  font-weight: 600;
}

.item-subtotal {
  font-weight: 600;
  color: #333;
  min-width: 80px;
  text-align: right;
}

/* 포인트 섹션 */
.points-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.points-info {
  color: #666;
}

.points-field {
  max-width: 200px;
}

/* PG 선택 섹션 */
.pg-selection {
  margin-top: 4px;
}

.pg-radio-group {
  margin: 0 !important;
}

.pg-radio {
  margin-bottom: 16px !important;
}

.pg-radio:last-child {
  margin-bottom: 0 !important;
}

.pg-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  background: #fafafa;
  transition: all 0.3s ease;
  cursor: pointer;
  width: calc(100% - 40px);
  margin-left: 40px;
  min-height: 70px;
}

.pg-option:hover {
  border-color: #0064FF;
  background: #f0f7ff;
}

.pg-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.pg-info {
  flex: 1;
}

.pg-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.pg-desc {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.3;
}

/* 테스트 섹션 */
.test-section {
  margin-top: 4px;
}

.test-checkbox {
  margin: 0;
}

/* 주문 요약 (하단 고정) */
.order-summary-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #eee;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  z-index: 20;
}

.summary-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.price-breakdown {
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.875rem;
  color: #666;
}

.price-row.total {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  border-top: 1px solid #eee;
  padding-top: 12px;
  margin-top: 12px;
}

.discount {
  color: #00C896;
}

/* 약관 동의 */
.terms-section {
  margin-bottom: 16px;
}

.terms-checkbox,
.test-checkbox {
  margin: 0;
}

.terms-text,
.test-text {
  font-size: 0.875rem;
  color: #333;
  line-height: 1.4;
}

.terms-text span:first-child {
  color: #ff5252;
  font-weight: 600;
}

.test-text span:first-child {
  color: #ff9800;
  font-weight: 600;
}

.pay-btn {
  font-weight: 600;
  font-size: 1.1rem !important;
  height: 56px !important;
}

/* 최소 결제 금액 모달 */
.min-payment-dialog {
  border-radius: 16px;
}

.dialog-title {
  background: #fff3e0;
  color: #e65100;
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

.error-detail {
  color: #d32f2f;
  font-weight: 600;
}

.suggestion {
  color: #666;
  font-size: 0.875rem;
}

.dialog-actions {
  padding: 16px 24px 24px;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-state h3 {
  margin: 20px 0 8px;
  color: #333;
}

/* 반응형 */
@media (max-width: 768px) {
  .order-content {
    padding: 16px;
  }
  
  .section-card {
    padding: 20px;
  }
  
  .order-item {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .item-subtotal {
    width: 100%;
    text-align: left;
    margin-top: 8px;
  }
}
</style>