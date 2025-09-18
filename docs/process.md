1. 주문, 결제 프로세스
   1. 주문 번호 채번 FO -> API orderService 의 generateOrderNumber 사용.
   2. /api/payments/initiate 호출 해 결제 요청 파라미터 생성 (FO -> API)
   3. 생성된 파라미터로 결제 요청 window.INIStdPay.pay('inicisForm') (FO)
   4. 결제 요청 응답을 returnUrl(FO 의 /order/progress) 받아 성공 했을 경우 이후 프로세스 진행
   5. 현재 주문에 관련된 정보들을 세션에 저장.
   6. progress.vue 에서 processPaymentReturn 에서
      현재는 /payments/confirm 를 호출 하지만,
      5번에 저장된 주문의 정보들을 기반으로 주문을 진행하는 /api/orders/order 를 호출하고,
      이 API 내에서 주문 정보를 insert 하고 paymentService.confirmPayment 를 호출 하도록 바꿀거야.
   7. 주문이 완료 되면 주문 완료 페이지로, 실패 했을 경우 실패 페이지로 이동.

    