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
      <v-btn color="primary" large @click="proceedToPayment" :disabled="!selectedMember || orderItems.length === 0">
        Proceed to Payment
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
const inicisPrepared = ref(false)

// Store the created order and payment IDs for confirmation
const currentOrderId = ref(null)
const currentPaymentId = ref(null)

// Inicis parameters from backend
const inicisParams = ref({
  mid: '', oid: '', price: '', timestamp: '', signature: '', mKey: '',
  version: '', currency: '', moId: '', goodName: '',
  buyerName: '', buyerTel: '', buyerEmail: '', returnUrl: '', closeUrl: '',
  gopaymethod: 'Card', acceptmethod: 'below1000'
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
    // 1) 준비 단계: 최초 클릭 시 파라미터 발급 + 스크립트 로드만 수행
    if (!inicisPrepared.value) {
      const initiatePayload = {
        memberId: selectedMember.value.id,
        amount: total.value,
        paymentMethod: 'CREDIT_CARD',
        goodName: '주문결제',
        buyerName: selectedMember.value.name || '구매자',
        buyerTel: selectedMember.value.phone || '',
        buyerEmail: selectedMember.value.email || '',
      };
      const initiateResponse = await fetch('/api/payments/initiate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(initiatePayload),
      });
      if (!initiateResponse.ok) throw new Error('Payment initiation failed');

      const inicisResponse = await initiateResponse.json();
      inicisParams.value = inicisResponse;
      if (!inicisParams.value.gopaymethod) inicisParams.value.gopaymethod = 'Card';
      if (!inicisParams.value.acceptmethod) inicisParams.value.acceptmethod = 'below1000';

      if (!window.INIStdPay) {
        await new Promise((resolve, reject) => {
          const src = 'https://stdpay.inicis.com/stdjs/INIStdPay.js';
          const existing = document.querySelector(`script[src="${src}"]`);
          if (existing) {
            const start = Date.now();
            const t = setInterval(() => {
              if (window.INIStdPay) { clearInterval(t); resolve(null); }
              else if (Date.now() - start > 10000) { clearInterval(t); reject(new Error('Timeout waiting INIStdPay')); }
            }, 50);
          } else {
            const s = document.createElement('script');
            s.src = src; s.async = true;
            s.onload = () => resolve(null);
            s.onerror = () => reject(new Error('Failed to load INIStdPay.js'));
            document.head.appendChild(s);
          }
        });
      }
      inicisPrepared.value = true;
      alert('결제 준비가 완료되었습니다. 결제 버튼을 다시 눌러 진행해 주세요.');
      return;
    }

    // 2) 실행 단계: 두 번째 클릭에서 동기적으로 pay 호출
    // 콜백명은 영문/숫자만 사용(특수문자 금지)
    const callbackName = `INIStdPay${Date.now()}`;
    window[callbackName] = async (rsp) => {
      try {
        console.log('rsp', rsp)
        if (rsp && rsp.resultCode === '0000') {
          const confirmRes = await fetch('/api/payments/confirm', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              authToken: rsp.authToken,
              authUrl: rsp.authUrl,
              netCancelUrl: rsp.netCancelUrl,
              mid: rsp.mid || inicisParams.value.mid,
              oid: inicisParams.value.oid,
              price: inicisParams.value.price,
            }),
          });
          if (!confirmRes.ok) throw new Error('Payment approval failed');

          const orderRes = await fetch('/api/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              memberId: selectedMember.value.id,
              items: orderItems.value.map(item => ({ productId: item.productId, quantity: item.quantity })),
              usedPoints: usedPoints.value,
            }),
          });
          if (!orderRes.ok) throw new Error('Order creation failed after payment');

          const createdOrder = await orderRes.json();
          currentOrderId.value = createdOrder.id;
          alert(`결제가 완료되어 주문 #${createdOrder.id}가 생성되었습니다.`);
          router.push(`/members/${selectedMember.value.id}`);
        } else {
          alert(rsp?.resultMsg || '결제 실패');
        }
      } catch (e) {
        console.error(e);
        alert('결제 처리 중 오류가 발생했습니다.');
      } finally {
        delete window[callbackName];
        inicisPrepared.value = false;
      }
    };

    const form = document.getElementById('inicisForm');
    if (!form) {
      alert('결제 폼을 찾을 수 없습니다. 페이지를 새로고침 후 다시 시도해주세요.');
      inicisPrepared.value = false;
      return;
    }
    // 콜백 이름을 폼에 넘겨주기(선택)
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
    ensureInput('callback', callbackName);
    // 인코딩 명시(UTF-8)
    ensureInput('charset', 'UTF-8');
    // 인코딩 명시
    ensureInput('charset', 'UTF-8');

    if (!window.INIStdPay || typeof window.INIStdPay.pay !== 'function') {
      alert('INIStdPay.js가 아직 로드되지 않았습니다. 잠시 후 다시 시도해주세요.');
      inicisPrepared.value = false;
      return;
    }
    // 폼 아이디 문자열로 호출(권장)
    window.INIStdPay.pay('inicisForm');
  } catch (error) {
    console.error(error);
    alert('결제 시작 중 오류가 발생했습니다.');
    inicisPrepared.value = false;
  }
}

// Helper function for manual payment confirmation (for testing)
const confirmPayment = async (paymentId, status) => {
  const transactionId = status === 'SUCCESS' ? `TXN-${Date.now()}` : null;
  try {
    const response = await fetch('/api/payments/confirm', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({paymentId, status, transactionId}),
    });

    if (!response.ok) {
      throw new Error('Payment confirmation failed');
    }
    const confirmedPayment = await response.json();
    alert(`Payment ${confirmedPayment.id} confirmed with status: ${confirmedPayment.status}`);
    // Optionally, redirect or update UI after confirmation
    router.push(`/members/${selectedMember.value.id}`);
  } catch (error) {
    console.error(error);
    alert('Error confirming payment.');
  }
};

onMounted(() => {
  fetchInitialData()
})
</script>
