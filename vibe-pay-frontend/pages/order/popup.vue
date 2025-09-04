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
  </div>
</template>

<script setup lang="ts">
const directAccess = ref(false);

// 이 팝업은 SDK가 띄우는 컨테이너 역할만 수행합니다. 어떠한 pay 호출도 하지 않습니다.
onMounted(() => {
  if (!window.opener) {
    directAccess.value = true;
  }
  try { window.focus(); } catch (_) {}
});

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
