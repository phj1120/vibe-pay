<template>
  <div>
    <h1>결제 처리 중...</h1>
    <p>잠시만 기다려주세요.</p>
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

      // 성공/실패에 관계없이 부모창에 결과 전달하는 HTML 반환
      const postMessageScript =
        '<scr' + 'ipt>' +
          'console.log("팝업에서 부모창으로 메시지 전송:", ' + JSON.stringify(paymentData) + ');' +
          'if (window.opener) {' +
            'window.opener.postMessage({' +
              'type: "PAYMENT_RESULT",' +
              'data: ' + JSON.stringify(paymentData) +
            '}, "*");' +
            'setTimeout(() => { window.close(); }, 1000);' +
          '} else {' +
            'console.error("부모창을 찾을 수 없습니다.");' +
          '}' +
        '</scr' + 'ipt>' +
        '<div>' +
          '<h2>' + (resultCode === '0000' ? '결제 성공' : '결제 실패') + '</h2>' +
          '<p>결과를 부모창에 전달 중입니다...</p>' +
          '<p>잠시 후 창이 자동으로 닫힙니다.</p>' +
        '</div>';

      // HTML 응답 반환
      event.node.res.writeHead(200, {
        'Content-Type': 'text/html; charset=utf-8'
      })
      event.node.res.end(postMessageScript)
      return

    } catch (error) {
      console.error('팝업 결제 처리 중 오류:', error)

      // 에러 시에도 부모창에 에러 메시지 전달
      const errorScript =
        '<scr' + 'ipt>' +
          'if (window.opener) {' +
            'window.opener.postMessage({' +
              'type: "PAYMENT_ERROR",' +
              'error: "' + (error.message || '알 수 없는 오류') + '"' +
            '}, "*");' +
            'window.close();' +
          '}' +
        '</scr' + 'ipt>' +
        '<div>' +
          '<h2>오류 발생</h2>' +
          '<p>결제 처리 중 오류가 발생했습니다.</p>' +
        '</div>';

      event.node.res.writeHead(200, {
        'Content-Type': 'text/html; charset=utf-8'
      })
      event.node.res.end(errorScript)
      return
    }
  } else {
    console.log('GET 요청 또는 다른 메서드:', event.node.req.method)
    // GET 요청인 경우 기본 대기 화면
  }
}

// SSR에서 실행
if (process.server) {
  await processPaymentReturnInPopup()
}
</script>