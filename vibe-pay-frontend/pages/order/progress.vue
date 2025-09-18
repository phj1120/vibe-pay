<template>
  <div>
    <h1>결제 처리 중...</h1>
    <p>잠시만 기다려주세요.</p>
  </div>
</template>

<script setup>
// 쿠키 파싱 헬퍼 함수 (개선된 버전)
const getCookie = (cookieString, name) => {
  console.log('=== Cookie Debug Info ===')
  console.log('Full cookie string:', cookieString)
  console.log('Looking for cookie name:', name)

  if (!cookieString) {
    console.log('No cookie string found')
    return null
  }

  const cookies = cookieString.split(';')
  console.log('Split cookies:', cookies)

  for (const cookie of cookies) {
    const trimmedCookie = cookie.trim()
    const equalIndex = trimmedCookie.indexOf('=')

    if (equalIndex === -1) continue

    const key = trimmedCookie.substring(0, equalIndex)
    const value = trimmedCookie.substring(equalIndex + 1)

    console.log(`Cookie found: "${key}" = "${value}"`)

    if (key === name) {
      console.log(`Target cookie found: ${value}`)
      return value
    }
  }

  console.log(`Cookie "${name}" not found`)
  return null
}

// SSR에서 POST 요청 처리 함수
const processPaymentReturn = async () => {
  const event = useRequestEvent()
  
  console.log('=== 이니시스 호출 방식 분석 ===')
  console.log('Request method:', event.node.req.method)
  console.log('User-Agent:', event.node.req.headers['user-agent'])
  console.log('Referer:', event.node.req.headers['referer'])
  console.log('X-Forwarded-For:', event.node.req.headers['x-forwarded-for'])
  console.log('Content-Type:', event.node.req.headers['content-type'])
  console.log('All headers:', event.node.req.headers)
  
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
      
      // 백엔드 API에 주문 생성 + 결제 승인 요청
      if (resultCode === '0000') {
        console.log('결제 성공 - 주문 생성 및 결제 승인 요청 시작')

        // 쿠키에서 주문 정보 가져오기 (SSR 환경)
        const pendingOrderCookie = getCookie(event.node.req.headers.cookie, 'pendingOrder')
        let orderData = null

        if (pendingOrderCookie) {
          try {
            orderData = JSON.parse(decodeURIComponent(pendingOrderCookie))
            console.log('Retrieved order data from cookie:', orderData)
          } catch (e) {
            console.error('Failed to parse order data from cookie:', e)
          }
        }

        // 쿠키에서 찾지 못한 경우 다른 방법들 시도
        if (!orderData) {
          console.log('=== Alternative Cookie Search ===')

          let value1 = useCookie('pendingOrder');
          let value2 = useCookie('pendingOrder').value;


          // 모든 쿠키를 출력해서 확인
          if (event.node.req.headers.cookie) {
            const allCookies = event.node.req.headers.cookie.split(';')
            console.log('All available cookies:')
            allCookies.forEach(cookie => {
              const [name, value] = cookie.trim().split('=', 2)
              console.log(`  "${name}" = "${value}"`)
            })
          }

          throw new Error('주문 정보를 찾을 수 없습니다. 쿠키를 확인해주세요.')
        }

        const response = await $fetch('http://localhost:8080/api/orders', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: {
            // 쿠키에서 가져온 주문 정보
            orderNumber: orderData.orderNumber,
            memberId: orderData.memberId,
            items: orderData.items,
            usedPoints: orderData.usedPoints,
            // 결제 승인 정보
            authToken: authToken,
            authUrl: authUrl,
            netCancelUrl: netCancelUrl,
            mid: mid,
            price: price,
            paymentMethod: 'CREDIT_CARD',
            usedMileage: orderData.usedPoints || 0
          }
        })
        
        console.log('Backend order creation response:', response)

        // 주문 완료 후 쿠키 삭제
        const expiredCookie = 'pendingOrder=; Max-Age=0; Path=/'

        // 성공 시 결과 페이지로 리다이렉트
        const successUrl = `/order/return?success=true&orderId=${response.id || 'unknown'}&oid=${oid || 'unknown'}&price=${price || '0'}`
        console.log('Redirecting to success page:', successUrl)

        event.node.res.writeHead(302, {
          'Location': successUrl,
          'Set-Cookie': expiredCookie
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
// if (process.server) {
  await processPaymentReturn()
// }
</script>
