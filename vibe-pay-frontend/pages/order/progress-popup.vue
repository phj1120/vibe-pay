<template>
  <div class="payment-progress">
    <div class="progress-container">
      <!-- 로딩 애니메이션 -->
      <div class="loading-spinner">
        <div class="spinner"></div>
      </div>

      <!-- 메인 메시지 -->
      <h1 class="main-title">결제 처리 중</h1>
      <p class="sub-message">안전한 결제를 위해 처리 중입니다</p>

      <!-- 진행 단계 표시 -->
      <div class="progress-steps">
        <div class="step active">
          <div class="step-icon">✓</div>
          <span>결제 정보 확인</span>
        </div>
        <div class="step active">
          <div class="step-icon">
            <div class="mini-spinner"></div>
          </div>
          <span>결제 승인 처리</span>
        </div>
        <div class="step">
          <div class="step-icon">3</div>
          <span>결과 전달</span>
        </div>
      </div>

      <!-- 주의사항 -->
      <div class="notice">
        <p>• 결제가 완료될 때까지 창을 닫지 마세요</p>
        <p>• 잠시 후 자동으로 결과 페이지로 이동합니다</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// 팝업창에서 이니시스 POST 요청 처리 함수
const processPaymentReturnInPopup = async () => {
  const event = useRequestEvent()

  console.log('=== 팝업창에서 이니시스 POST 요청 처리 시작 ===')
  console.log('Request method:', event.node.req.method)

  if (event.node.req.method === 'POST') {
    console.log('POST 요청 감지됨 (팝업)')

    try {
      // POST body 읽기
      let body = ''
      const chunks = []

      for await (const chunk of event.node.req) {
        chunks.push(chunk)
      }

      body = Buffer.concat(chunks).toString('utf8')
      console.log('Raw POST body:', body)

      // URL encoded 데이터를 객체로 파싱
      const parsedBody = {}
      if (body) {
        const params = new URLSearchParams(body)
        for (const [key, value] of params) {
          parsedBody[key] = value
        }
      }
      console.log('Parsed POST body:', parsedBody)

      // 이니시스 파라미터 추출
      const resultCode = parsedBody.resultCode
      const resultMsg = parsedBody.resultMsg
      const oid = parsedBody.orderNumber || parsedBody.oid
      const price = parsedBody.price
      const payMethod = parsedBody.payMethod
      const authToken = parsedBody.authToken
      const authUrl = parsedBody.authUrl
      const netCancelUrl = parsedBody.netCancelUrl
      const mid = parsedBody.mid

      console.log('Extracted parameters:', {
        resultCode,
        resultMsg,
        oid,
        price,
        payMethod,
        authToken,
        authUrl,
        netCancelUrl,
        mid
      })

      // 부모창에 전달할 데이터 준비
      const paymentData = {
        success: resultCode === '0000',
        resultCode,
        resultMsg,
        orderNumber: oid,
        price,
        payMethod,
        authToken,
        authUrl,
        netCancelUrl,
        mid
      }

      // 클라이언트로 리다이렉트해서 결제 데이터 전달
      const redirectUrl = `/order/progress-popup?` +
        `success=true&` +
        `resultCode=${encodeURIComponent(paymentData.resultCode)}&` +
        `resultMsg=${encodeURIComponent(paymentData.resultMsg)}&` +
        `orderNumber=${encodeURIComponent(paymentData.orderNumber)}&` +
        `price=${encodeURIComponent(paymentData.price)}&` +
        `payMethod=${encodeURIComponent(paymentData.payMethod)}&` +
        `authToken=${encodeURIComponent(paymentData.authToken)}&` +
        `authUrl=${encodeURIComponent(paymentData.authUrl)}&` +
        `netCancelUrl=${encodeURIComponent(paymentData.netCancelUrl)}&` +
        `mid=${encodeURIComponent(paymentData.mid)}`;

      event.node.res.writeHead(302, {
        'Location': redirectUrl
      })
      event.node.res.end()
      return

    } catch (error) {
      console.error('팝업 결제 처리 중 오류:', error)

      // 에러 시에도 리다이렉트로 전달
      const errorUrl = `/order/progress-popup?` +
        `success=false&` +
        `error=${encodeURIComponent(error.message || '알 수 없는 오류')}`;

      event.node.res.writeHead(302, {
        'Location': errorUrl
      })
      event.node.res.end()
      return
    }
  } else {
    console.log('GET 요청 또는 다른 메서드:', event.node.req.method)
    // GET 요청인 경우 기본 대기 화면
  }
}

// SSR에서 실행
if (process.server) {
  processPaymentReturnInPopup()
}

// 반응형 데이터
const paymentResult = ref(null);
const paymentError = ref(null);

// 클라이언트에서 실행
onMounted(() => {
  console.log('progress-popup mounted');

  if (process.client) {
    // 전역 데이터에서 결제 결과 확인 (POST 요청 처리 결과)
    // @ts-ignore
    const paymentData = globalThis.paymentResultData;
    // @ts-ignore
    const errorData = globalThis.paymentErrorData;

    if (paymentData) {
      console.log('전역 데이터에서 결제 성공 데이터 발견:', paymentData);

      // popup.vue를 거치지 않고 직접 부모창(order/index.vue)에 CREATE_ORDER 메시지 전달
      if (window.opener && window.opener.opener) {
        console.log('최상위 부모창에 주문 생성 요청 직접 전달');
        window.opener.opener.postMessage({
          type: "CREATE_ORDER",
          data: paymentData
        }, "*");

        // popup.vue도 닫기
        setTimeout(() => {
          try {
            window.opener.close();
          } catch(e) {
            console.log('popup.vue 닫기 실패:', e);
          }
        }, 500);
      } else if (window.opener) {
        console.log('부모창에 결제 결과 전달 (fallback)');
        window.opener.postMessage({
          type: "PAYMENT_RESULT",
          data: paymentData
        }, "*");
      }

      setTimeout(() => {
        console.log('progress-popup 닫기');
        window.close();
      }, 1000);
    } else if (errorData) {
      console.log('전역 데이터에서 결제 에러 발견:', errorData);

      // 에러도 최상위 부모창에 직접 전달
      if (window.opener && window.opener.opener) {
        console.log('최상위 부모창에 결제 에러 직접 전달');
        window.opener.opener.postMessage({
          type: "PAYMENT_ERROR",
          error: errorData
        }, "*");

        // popup.vue도 닫기
        setTimeout(() => {
          try {
            window.opener.close();
          } catch(e) {
            console.log('popup.vue 닫기 실패:', e);
          }
        }, 500);
      } else if (window.opener) {
        console.log('부모창에 에러 메시지 전달 (fallback)');
        window.opener.postMessage({
          type: "PAYMENT_ERROR",
          error: errorData
        }, "*");
      }

      setTimeout(() => {
        console.log('progress-popup 닫기');
        window.close();
      }, 1000);
    } else {
      // URL 파라미터도 확인 (redirect 방식)
      const urlParams = new URLSearchParams(window.location.search);
      const resultCode = urlParams.get('resultCode');

      if (resultCode) {
        console.log('URL에서 결제 결과 파라미터 발견');

        const urlPaymentData = {
          success: resultCode === '0000',
          resultCode: resultCode,
          resultMsg: urlParams.get('resultMsg'),
          orderNumber: urlParams.get('orderNumber') || urlParams.get('oid'),
          price: urlParams.get('price'),
          payMethod: urlParams.get('payMethod'),
          authToken: urlParams.get('authToken'),
          authUrl: urlParams.get('authUrl'),
          netCancelUrl: urlParams.get('netCancelUrl'),
          mid: urlParams.get('mid')
        };

        console.log('URL 결제 데이터:', urlPaymentData);

        if (window.opener) {
          console.log('부모창에 URL 결제 결과 전달');
          window.opener.postMessage({
            type: "PAYMENT_RESULT",
            data: urlPaymentData
          }, "*");

          setTimeout(() => {
            console.log('팝업 닫기');
            window.close();
          }, 1000);
        }
      }
    }
  }
})
</script>

<style scoped>
.payment-progress {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.progress-container {
  background: white;
  padding: 60px 40px;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  text-align: center;
  max-width: 500px;
  width: 90%;
}

.loading-spinner {
  margin-bottom: 30px;
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.main-title {
  font-size: 2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.sub-message {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 40px;
}

.progress-steps {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40px;
  padding: 0 20px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: relative;
}

.step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 20px;
  right: -50%;
  width: 100%;
  height: 2px;
  background: #e0e0e0;
  z-index: 1;
}

.step.active:not(:last-child)::after {
  background: #667eea;
}

.step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e0e0e0;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 10px;
  position: relative;
  z-index: 2;
}

.step.active .step-icon {
  background: #667eea;
}

.mini-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.step span {
  font-size: 0.9rem;
  color: #666;
}

.step.active span {
  color: #333;
  font-weight: 500;
}

.notice {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.notice p {
  margin: 5px 0;
  color: #666;
  font-size: 0.9rem;
  text-align: left;
}

/* 반응형 디자인 */
@media (max-width: 600px) {
  .progress-container {
    padding: 40px 20px;
  }

  .main-title {
    font-size: 1.5rem;
  }

  .progress-steps {
    padding: 0 10px;
  }

  .step span {
    font-size: 0.8rem;
  }

  .step-icon {
    width: 35px;
    height: 35px;
  }
}
</style>