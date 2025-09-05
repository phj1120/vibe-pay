<template>
  <v-card>
    <v-card-title>Create Order</v-card-title>
    <v-card-text>
      <!-- Member Selection -->
      <v-autocomplete
          v-model="selectedMember"
          :items="members"
          item-title="name"
          item-value="id"
          label="Select a Member"
          return-object
          class="mb-4"
      ></v-autocomplete>

      <!-- Product Selection -->
      <v-autocomplete
          v-model="selectedProduct"
          :items="products"
          item-title="name"
          item-value="id"
          label="Add a Product"
          return-object
          @update:modelValue="addProductToOrder"
          class="mb-4"
      ></v-autocomplete>

      <!-- Order Items -->
      <v-data-table
          :headers="orderHeaders"
          :items="orderItems"
          class="elevation-1 mb-4"
      >
        <template v-slot:item.quantity="{ item }">
          <v-text-field
              v-model.number="item.quantity"
              type="number"
              min="1"
              style="width: 100px"
              dense
              hide-details
          ></v-text-field>
        </template>
        <template v-slot:item.subtotal="{ item }">
          {{ (item.quantity * item.price).toFixed(2) }}
        </template>
        <template v-slot:item.actions="{ item }">
          <v-icon small @click="removeProductFromOrder(item)">mdi-delete</v-icon>
        </template>
      </v-data-table>

      <!-- Order Summary -->
      <v-row justify="end">
        <v-col cols="12" md="5">
          <v-card outlined>
            <v-card-text>
              <div class="d-flex justify-space-between">
                <span>Subtotal</span>
                <span>${{ subtotal.toFixed(2) }}</span>
              </div>
              <v-divider class="my-2"></v-divider>
              <div class="d-flex justify-space-between align-center">
                <span>Use Points</span>
                <v-text-field
                    v-model.number="usedPoints"
                    type="number"
                    min="0"
                    :max="selectedMember ? selectedMember.points : 0"
                    style="width: 100px"
                    dense
                    hide-details
                    class="ml-4"
                ></v-text-field>
              </div>
              <div class="d-flex justify-space-between caption" v-if="selectedMember">
                <span></span>
                <span>Available: {{ selectedMember.points }}</span>
              </div>
              <v-divider class="my-2"></v-divider>
              <div class="d-flex justify-space-between font-weight-bold text-h6">
                <span>Total</span>
                <span>${{ total.toFixed(2) }}</span>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Hidden INIStdPay form to prevent null reference and pass parameters -->
      <form id="inicisForm" style="display: none;">
        <input type="hidden" name="mid" :value="inicisParams.mid"/>
        <input type="hidden" name="oid" :value="inicisParams.oid"/>
        <input type="hidden" name="price" :value="inicisParams.price"/>
        <input type="hidden" name="timestamp" :value="inicisParams.timestamp"/>
        <input type="hidden" name="signature" :value="inicisParams.signature"/>
        <input type="hidden" name="verification" :value="inicisParams.verification"/>
        <input type="hidden" name="mKey" :value="inicisParams.mKey"/>
        <input type="hidden" name="version" :value="inicisParams.version"/>
        <input type="hidden" name="currency" :value="inicisParams.currency"/>
        <input type="hidden" name="moId" :value="inicisParams.moId"/>
        <!-- INIStdPay expects these lowercase keys -->
        <input type="hidden" name="goodname" :value="inicisParams.goodName"/>
        <input type="hidden" name="buyername" :value="inicisParams.buyerName"/>
        <input type="hidden" name="buyertel" :value="inicisParams.buyerTel"/>
        <input type="hidden" name="buyeremail" :value="inicisParams.buyerEmail"/>
        <input type="hidden" name="returnUrl" :value="inicisParams.returnUrl"/>
        <input type="hidden" name="closeUrl" :value="inicisParams.closeUrl"/>
        <!-- Required by INIStdPay -->
        <input type="hidden" name="gopaymethod" :value="inicisParams.gopaymethod"/>
        <input type="hidden" name="acceptmethod" :value="inicisParams.acceptmethod"/>
      </form>

    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn color="primary" large @click="proceedToPayment" :disabled="!selectedMember || orderItems.length === 0 || isProcessing" :loading="isProcessing">
        {{ isProcessing ? 'Processing...' : 'Proceed to Payment' }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'

const route = useRoute()
const router = useRouter()

// Mock Data
const members = ref([])
const products = ref([])
const selectedMember = ref(null)
const selectedProduct = ref(null)
const orderItems = ref([])
const usedPoints = ref(0)
const defaultApplied = ref(false)
const isProcessing = ref(false)

// Store the created order and payment IDs for confirmation
const currentOrderId = ref(null)
const currentPaymentId = ref(null)

// Inicis parameters from backend
const inicisParams = ref({
  mid: '', oid: '', price: '', timestamp: '', signature: '', mKey: '',
  version: '', currency: '', moId: '', goodName: '',
  buyerName: '', buyerTel: '', buyerEmail: '', returnUrl: '', closeUrl: '',
  gopaymethod: '', acceptmethod: ''
});

watch(selectedMember, async (newMember) => {
  if (newMember) {
    try {
      const response = await fetch(`/api/rewardpoints/member/${newMember.id}`);
      if (!response.ok) throw new Error('Failed to fetch points');
      const data = await response.json();
      newMember.points = data.points; // Assuming the response directly contains 'points'
    } catch (error) {
      console.error(error);
      newMember.points = 0; // Default to 0 on error
    }
  } else {
    usedPoints.value = 0;
  }
});

const orderHeaders = [
  {title: 'Product', key: 'name'},
  {title: 'Price', key: 'price'},
  {title: 'Quantity', key: 'quantity'},
  {title: 'Subtotal', key: 'subtotal'},
  {title: 'Actions', key: 'actions', sortable: false},
]

const subtotal = computed(() => {
  return orderItems.value.reduce((acc, item) => acc + (item.quantity * item.price), 0)
})

const total = computed(() => {
  const finalTotal = subtotal.value - usedPoints.value
  return finalTotal > 0 ? finalTotal : 0
})

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

    // URL 쿼리(memberId) 우선 적용
    const memberIdFromQuery = route.query.memberId;
    if (memberIdFromQuery) {
      const foundMember = members.value.find(m => m.id === parseInt(memberIdFromQuery));
      if (foundMember) {
        selectedMember.value = foundMember;
      }
    } else {
      // URL 쿼리가 없으면 첫 번째 회원 자동 선택
      if (members.value.length > 0) {
        selectedMember.value = members.value[0];
        console.log('Auto-selected first member:', selectedMember.value);
      }
    }

    // 첫 번째 상품을 자동으로 주문에 추가
    if (products.value.length > 0) {
      const firstProduct = products.value[0];
      addProductToOrder(firstProduct);
      console.log('Auto-added first product to order:', firstProduct);
    }

  } catch (error) {
    console.error(error);
  }
};

const addProductToOrder = (product) => {
  if (!product) return
  const existingItem = orderItems.value.find(item => item.id === product.id)
  if (existingItem) {
    existingItem.quantity++
  } else {
    orderItems.value.push({...product, productId: product.id, quantity: 1})
  }
  selectedProduct.value = null
}

const removeProductFromOrder = (itemToRemove) => {
  orderItems.value = orderItems.value.filter(item => item.id !== itemToRemove.id)
}

const proceedToPayment = async () => {
  // 중복 클릭 방지
  if (isProcessing.value) {
    return;
  }

  // 유효성 검사
  if (!selectedMember.value) {
    alert("Please select a member.");
    return;
  }
  if (usedPoints.value > (selectedMember.value.points || 0)) {
    alert("Cannot use more points than available.");
    return;
  }
  if (orderItems.value.length === 0) {
    alert("Please add at least one product.");
    return;
  }

  try {
    isProcessing.value = true;
    console.log('Starting payment process...');
    
    // 주문 정보를 로컬 스토리지에 저장 (return 페이지에서 사용)
    const orderData = {
      memberId: selectedMember.value.id,
      items: orderItems.value.map(item => ({ 
        productId: item.productId, 
        quantity: item.quantity 
      })),
      usedPoints: usedPoints.value
    };
    localStorage.setItem('pendingOrder', JSON.stringify(orderData));
    console.log('Stored pending order:', orderData);
    
    // 상품명 생성 (선택된 상품들)
    const productNames = orderItems.value.map(item => item.name).join(', ');
    const goodName = productNames.length > 50 ? productNames.substring(0, 47) + '...' : productNames;
    
    const initiatePayload = {
      memberId: selectedMember.value.id,
      amount: total.value,
      paymentMethod: 'CREDIT_CARD',
      goodName: goodName || '주문결제', // 실제 상품명 사용
      buyerName: selectedMember.value.name || '구매자', // 실제 회원명 사용
      buyerTel: selectedMember.value.phoneNumber || '010-0000-0000',
      buyerEmail: selectedMember.value.email || 'buyer@example.com',
    };
    
    console.log('Selected member:', selectedMember.value);
    console.log('Initiate payload:', initiatePayload);
    
    // 결제 준비 (백엔드 호출)
    const initiateResponse = await fetch('/api/payments/initiate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(initiatePayload),
    });
    
    if (!initiateResponse.ok) {
      const errorText = await initiateResponse.text();
      throw new Error(`Payment initiation failed: ${errorText}`);
    }

    const inicisResponse = await initiateResponse.json();
    console.log('Payment initiated:', inicisResponse);
    inicisParams.value = inicisResponse;
    
    // INIStdPay.js 로드 확인 및 로드
    if (!window.INIStdPay) {
      console.log('Loading INIStdPay.js...');
      await new Promise((resolve, reject) => {
        const src = 'https://stdpay.inicis.com/stdjs/INIStdPay.js';
        const existing = document.querySelector(`script[src="${src}"]`);
        if (existing) {
          const start = Date.now();
          const t = setInterval(() => {
            if (window.INIStdPay) { 
              clearInterval(t); 
              console.log('INIStdPay.js loaded from existing script');
              resolve(null); 
            }
            else if (Date.now() - start > 10000) { 
              clearInterval(t); 
              reject(new Error('Timeout waiting INIStdPay')); 
            }
          }, 50);
        } else {
          const s = document.createElement('script');
          s.src = src; 
          s.async = true;
          s.onload = () => {
            console.log('INIStdPay.js loaded successfully');
            resolve(null);
          };
          s.onerror = () => reject(new Error('Failed to load INIStdPay.js'));
          document.head.appendChild(s);
        }
      });
    }

    // 폼 확인
    const form = document.getElementById('inicisForm');
    if (!form) {
      throw new Error('결제 폼을 찾을 수 없습니다. 페이지를 새로고침 후 다시 시도해주세요.');
    }

    // 인코딩 명시(UTF-8)
    const ensureInput = (name, value) => {
      let input = form.querySelector(`input[name="${name}"]`);
      if (!input) {
        input = document.createElement('input');
        input.type = 'hidden';
        input.name = name;
        form.appendChild(input);
      }
      input.value = value != null ? String(value) : '';
    };
    ensureInput('charset', 'UTF-8');

    if (!window.INIStdPay || typeof window.INIStdPay.pay !== 'function') {
      throw new Error('INIStdPay.js가 아직 로드되지 않았습니다. 잠시 후 다시 시도해주세요.');
    }
    
    console.log('Calling INIStdPay.pay with form:', 'inicisForm');
    
    // 이니시스 결제창 호출 (return 페이지로 리다이렉트됨)
    window.INIStdPay.pay('inicisForm');
    
  } catch (error) {
    console.error('Payment process error:', error);
    alert(`결제 시작 중 오류가 발생했습니다: ${error.message}`);
  } finally {
    isProcessing.value = false;
  }
}


onMounted(() => {
  fetchInitialData()
})
</script>
