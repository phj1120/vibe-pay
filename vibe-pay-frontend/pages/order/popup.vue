<template>
  <div class="popup-wrap">
    <!-- 결제 준비 중 상태 -->
    <div v-if="uiState === 'preparing'" class="popup-card">
      <div class="spinner" aria-label="loading"></div>
      <h2>결제창을 준비 중입니다...</h2>
      <p class="hint">
        이 창은 결제 진행을 위한 전용 팝업입니다.<br />
        잠시만 기다려 주세요. 브라우저 설정에 따라 화면 전환에 시간이 걸릴 수 있습니다.
      </p>
      <div v-if="directAccess" class="warning">
        이 페이지는 결제 과정에서만 열립니다. 직접 접근하셨다면 닫아주세요.
      </div>
      <button class="close-btn" type="button" @click="handleClose">창 닫기</button>
    </div>

    <!-- 결제 완료 상태 -->
    <div v-else-if="uiState === 'success'" class="popup-card success-card">
      <div class="result-icon success">✅</div>
      <h2 class="result-title">결제 완료</h2>
      <p class="result-message">
        결제가 성공적으로 완료되었습니다.<br>
        주문 정보를 처리하고 있습니다
        <span class="loading-dots"></span>
      </p>
      <div class="payment-info">
        <div class="info-row">
          <span>주문번호:</span>
          <span>{{ paymentResult?.orderNumber }}</span>
        </div>
        <div class="info-row">
          <span>결제금액:</span>
          <span>{{ formatPrice(paymentResult?.price) }}원</span>
        </div>
      </div>
    </div>

    <!-- 결제 실패 상태 -->
    <div v-else-if="uiState === 'failed'" class="popup-card error-card">
      <div class="result-icon error">❌</div>
      <h2 class="result-title">결제 실패</h2>
      <p class="result-message">
        결제 처리 중 문제가 발생했습니다.<br>
        잠시 후 이전 페이지로 돌아갑니다
        <span class="loading-dots"></span>
      </p>
      <div class="error-details">
        <p><strong>오류 코드:</strong> {{ paymentResult?.resultCode }}</p>
        <p><strong>오류 메시지:</strong> {{ paymentResult?.resultMsg }}</p>
      </div>
    </div>

    <!-- 오류 상태 -->
    <div v-else-if="uiState === 'error'" class="popup-card error-card">
      <div class="result-icon error">⚠️</div>
      <h2 class="result-title">오류 발생</h2>
      <p class="result-message">
        결제 처리 중 오류가 발생했습니다.<br>
        잠시 후 이전 페이지로 돌아갑니다
        <span class="loading-dots"></span>
      </p>
      <div class="error-details">
        <p><strong>오류 내용:</strong> {{ errorMessage }}</p>
      </div>
    </div>

    <!-- 이니시스 결제 폼 -->
    <form id="inicisForm" method="post" style="display: none;">
      <input type="hidden" name="mid" :value="paymentParams?.mid || ''" />
      <input type="hidden" name="oid" :value="paymentParams?.oid || ''" />
      <input type="hidden" name="price" :value="paymentParams?.price || ''" />
      <input type="hidden" name="timestamp" :value="paymentParams?.timestamp || ''" />
      <input type="hidden" name="signature" :value="paymentParams?.signature || ''" />
      <input type="hidden" name="verification" :value="paymentParams?.verification || ''" />
      <input type="hidden" name="mKey" :value="paymentParams?.mkey || ''" />
      <input type="hidden" name="version" :value="paymentParams?.version || ''" />
      <input type="hidden" name="currency" :value="paymentParams?.currency || 'WON'" />
      <input type="hidden" name="moId" :value="paymentParams?.moId || ''" />
      <input type="hidden" name="goodname" :value="paymentParams?.goodName || ''" />
      <input type="hidden" name="buyername" :value="paymentParams?.buyerName || ''" />
      <input type="hidden" name="buyertel" :value="paymentParams?.buyerTel || ''" />
      <input type="hidden" name="buyeremail" :value="paymentParams?.buyerEmail || ''" />
      <input type="hidden" name="returnUrl" :value="paymentParams?.returnUrl || ''" />
      <input type="hidden" name="closeUrl" :value="paymentParams?.closeUrl || ''" />
      <input type="hidden" name="gopaymethod" :value="paymentParams?.gopaymethod || ''" />
      <input type="hidden" name="acceptmethod" :value="paymentParams?.acceptmethod || ''" />
      <input type="hidden" name="charset" value="UTF-8" />
    </form>
  </div>
</template>

<script setup lang="ts">
const directAccess = ref(false);
const paymentParams = ref(null);
const uiState = ref('preparing'); // 'preparing', 'success', 'failed', 'error'
const paymentResult = ref(null);
const errorMessage = ref('');
const paymentExecuted = ref(false); // 중복 실행 방지 플래그

// 가격 포맷팅 함수
const formatPrice = (price) => {
  if (!price) return '0';
  return parseInt(price).toLocaleString();
};

onMounted(async () => {
  console.log('Payment popup mounted');

  if (!window.opener) {
    directAccess.value = true;
    return;
  }

  // 부모창에 팝업 준비 완료 알림
  console.log('Sending POPUP_READY message to parent');
  window.opener.postMessage({ type: 'POPUP_READY' }, '*');

  // 부모창으로부터 결제 파라미터 및 결과 수신 대기
  const handleMessage = async (event) => {
    console.log('Received message in popup:', event.data);

    if (event.data.type === 'PAYMENT_PARAMS') {
      paymentParams.value = event.data.data;
      console.log('Payment params received via postMessage:', paymentParams.value);

      // 중복 실행 방지
      if (paymentExecuted.value) {
        console.log('Payment already executed, ignoring duplicate call');
        return;
      }

      // 이니시스 SDK 로드 및 결제 실행
      try {
        await loadInicisSDK();
        executePayment();
      } catch (error) {
        console.error('Failed to load Inicis SDK or execute payment:', error);
        uiState.value = 'error';
        errorMessage.value = 'SDK 로딩 실패: ' + error.message;
      }
    }
    else if (event.data.type === 'PAYMENT_RESULT') {
      // 결제 결과 처리
      paymentResult.value = event.data.data;
      uiState.value = event.data.data.success ? 'success' : 'failed';

      // 결제 성공시 부모창에 주문 생성 요청
      if (event.data.data.success) {
        console.log('Payment successful, sending order creation request to parent');
        window.opener.postMessage({
          type: 'CREATE_ORDER',
          data: event.data.data
        }, '*');
      }

      // 2초 후 창 닫기
      setTimeout(() => {
        window.close();
      }, 2000);
    }
    else if (event.data.type === 'PAYMENT_ERROR') {
      // 결제 오류 처리
      uiState.value = 'error';
      errorMessage.value = event.data.error;

      // 2초 후 창 닫기
      setTimeout(() => {
        window.close();
      }, 2000);
    }
  };

  window.addEventListener('message', handleMessage);

  // 5초 후에도 파라미터를 받지 못했으면 에러 처리
  setTimeout(() => {
    if (!paymentParams.value && uiState.value === 'preparing') {
      window.removeEventListener('message', handleMessage);
      console.error('결제 파라미터를 받지 못했습니다.');
      alert('결제 파라미터를 받지 못했습니다. 창을 닫고 다시 시도해주세요.');
      window.close();
    }
  }, 5000);

  try { window.focus(); } catch (_) {}
});

const loadInicisSDK = async () => {
  if (!window.INIStdPay) {
    await new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://stdpay.inicis.com/stdjs/INIStdPay.js';
      script.async = true;
      script.onload = resolve;
      script.onerror = () => reject(new Error('Failed to load INIStdPay.js'));
      document.head.appendChild(script);
    });
  }
};

const executePayment = () => {
  // 중복 실행 방지
  if (paymentExecuted.value) {
    console.log('Payment already executed, skipping');
    return;
  }

  const form = document.getElementById('inicisForm');
  if (!form) {
    console.error('결제 폼을 찾을 수 없습니다.');
    uiState.value = 'error';
    errorMessage.value = '결제 폼을 찾을 수 없습니다.';
    return;
  }

  if (!window.INIStdPay) {
    console.error('INIStdPay SDK가 로드되지 않았습니다.');
    uiState.value = 'error';
    errorMessage.value = 'INIStdPay SDK가 로드되지 않았습니다.';
    return;
  }

  console.log('Executing payment with INIStdPay...');
  paymentExecuted.value = true; // 실행 상태 플래그 설정
  window.INIStdPay.pay('inicisForm');
};

function handleClose() {
  try { window.close(); } catch (_) { alert('창을 닫아주세요.'); }
}
</script>

<style scoped>
.popup-wrap {
  height: 100vh;
  display: grid;
  place-items: center;
  background: #0f172a;
}
.popup-card {
  width: min(480px, 92vw);
  padding: 28px 24px;
  border-radius: 14px;
  background: #111827;
  color: #e5e7eb;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,.35);
}

/* 결제 성공 카드 */
.success-card {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

/* 결제 실패/오류 카드 */
.error-card {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

/* 결과 아이콘 */
.result-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.result-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 10px;
}

.result-message {
  margin-bottom: 20px;
  line-height: 1.5;
}

/* 결제 정보 */
.payment-info {
  background: rgba(255, 255, 255, 0.1);
  padding: 16px;
  border-radius: 8px;
  margin-top: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

/* 오류 상세 */
.error-details {
  background: rgba(255, 255, 255, 0.1);
  padding: 16px;
  border-radius: 8px;
  margin-top: 20px;
  text-align: left;
}

.error-details p {
  margin: 8px 0;
  font-size: 14px;
}

/* 로딩 점 애니메이션 */
.loading-dots {
  display: inline-block;
}

.loading-dots::after {
  content: '';
  animation: dots 1.5s infinite;
}

@keyframes dots {
  0%, 20% { content: ''; }
  40% { content: '.'; }
  60% { content: '..'; }
  80%, 100% { content: '...'; }
}

.spinner {
  width: 42px;
  height: 42px;
  margin: 8px auto 14px;
  border: 4px solid rgba(255,255,255,0.2);
  border-top-color: #60a5fa;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
.hint {
  font-size: 14px;
  line-height: 1.5;
  color: #cbd5e1;
  margin: 6px 0 14px;
}
.warning {
  font-size: 13px;
  color: #fca5a5;
  margin-bottom: 12px;
}
.close-btn {
  margin-top: 4px;
  width: 100%;
  height: 40px;
  border-radius: 10px;
  border: 1px solid #374151;
  background: #1f2937;
  color: #e5e7eb;
  cursor: pointer;
  transition: all .2s ease;
}
.close-btn:hover {
  background: #374151;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
