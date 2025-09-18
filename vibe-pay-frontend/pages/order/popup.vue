<template>
  <div class="popup-wrap">
    <div class="popup-card">
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

    <!-- 이니시스 결제 폼 -->
    <form id="inicisForm" method="post" style="display: none;">
      <input type="hidden" name="mid" :value="paymentParams?.mid || ''" />
      <input type="hidden" name="oid" :value="paymentParams?.oid || ''" />
      <input type="hidden" name="price" :value="paymentParams?.price || ''" />
      <input type="hidden" name="timestamp" :value="paymentParams?.timestamp || ''" />
      <input type="hidden" name="signature" :value="paymentParams?.signature || ''" />
      <input type="hidden" name="verification" :value="paymentParams?.verification || ''" />
      <input type="hidden" name="mKey" :value="paymentParams?.mKey || ''" />
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

onMounted(async () => {
  console.log('Payment popup mounted');

  if (!window.opener) {
    directAccess.value = true;
    return;
  }

  // 부모창에 팝업 준비 완료 알림
  console.log('Sending POPUP_READY message to parent');
  window.opener.postMessage({ type: 'POPUP_READY' }, '*');

  // 부모창으로부터 결제 파라미터 수신 대기
  const handleMessage = async (event) => {
    console.log('Received message in popup:', event.data);

    if (event.data.type === 'PAYMENT_PARAMS') {
      paymentParams.value = event.data.data;
      console.log('Payment params received via postMessage:', paymentParams.value);

      // 메시지 리스너 제거
      window.removeEventListener('message', handleMessage);

      // 이니시스 SDK 로드 및 결제 실행
      await loadInicisSDK();
      executePayment();
    }
  };

  window.addEventListener('message', handleMessage);

  // 5초 후에도 파라미터를 받지 못했으면 에러 처리
  setTimeout(() => {
    if (!paymentParams.value) {
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
  const form = document.getElementById('inicisForm');
  if (!form) {
    console.error('결제 폼을 찾을 수 없습니다.');
    return;
  }

  console.log('Executing payment with INIStdPay...');
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
