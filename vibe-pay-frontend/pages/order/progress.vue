<template>
  <div>
    <h1>결제 처리 중...</h1>
    <p>잠시만 기다려주세요.</p>
  </div>
</template>

<script setup>
// SSR에서 POST 요청 처리 함수
const processPaymentReturn = async () => {
  const event = useRequestEvent()
  
  console.log('SSR에서 이니시스 POST 요청 처리 시작')
  console.log('Request method:', event.node.req.method)
  
  if (event.node.req.method === 'POST') {
    console.log('POST 요청 감지됨')
    
    try {
      // Content-Type 확인
      const contentType = event.node.req.headers['content-type']
      console.log('Content-Type:', contentType)
      
      // POST body 읽기 - Node.js 기본 방식
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
      
      // 백엔드 API에 승인 요청
      if (resultCode === '0000') {
        console.log('결제 성공 - 백엔드 승인 요청 시작')
        
        const response = await $fetch('http://localhost:8080/api/payments/confirm', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: {
            authToken: authToken,
            authUrl: authUrl,
            netCancelUrl: netCancelUrl,
            mid: mid,
            oid: oid,
            orderNumber: oid,
            price: price,
          }
        })
        
        console.log('Backend payment confirmation response:', response)
        
        // 성공 시 결과 페이지로 리다이렉트
        const successUrl = `/order/return?success=true&paymentId=${response.id || 'unknown'}&oid=${oid || 'unknown'}&price=${price || '0'}`
        console.log('Redirecting to success page:', successUrl)
        
        event.node.res.writeHead(302, {
          'Location': successUrl
        })
        event.node.res.end()
        return
        
      } else {
        console.log('결제 실패 - 실패 페이지로 리다이렉트')
        // 실패 시 실패 페이지로 리다이렉트
        const failureUrl = `/order/return?success=false&resultCode=${resultCode || 'unknown'}&resultMsg=${encodeURIComponent(resultMsg || '결제 실패')}`
        console.log('Redirecting to failure page:', failureUrl)
        
        event.node.res.writeHead(302, {
          'Location': failureUrl
        })
        event.node.res.end()
        return
      }
      
    } catch (error) {
      console.error('Payment return processing error:', error)
      // 에러 시 실패 페이지로 리다이렉트
      const errorMessage = error?.message || 'Unknown error'
      const errorUrl = `/order/return?success=false&error=${encodeURIComponent(errorMessage)}`
      console.log('Redirecting to error page:', errorUrl)
      
      event.node.res.writeHead(302, {
        'Location': errorUrl
      })
      event.node.res.end()
      return
    }
  } else {
    console.log('GET 요청 또는 다른 메서드:', event.node.req.method)
  }
}

// SSR에서 실행
if (process.server) {
  await processPaymentReturn()
}
</script>
