# Orders API 명세

결제 승인 후 실제 주문을 생성하고, 생성된 주문을 관리(조회, 취소)하는 API 명세입니다.

---

### `POST /api/orders`

-   **설명**: PG 결제 성공 후, 서버 간 통신으로 결제를 최종 검증/승인하고, 관련 데이터를 DB에 트랜잭션으로 저장합니다.
-   **요청 본문 (Request Body)**:

| 필드명 | 타입 | 필수 | 설명 |
| :--- | :--- | :--- | :--- |
| `orderNumber`| String | Y | `initiate` 단계에서 사용된 주문번호 |
| `memberId` | Long | Y | 회원 ID |
| `items` | List | Y | 주문 상품 배열. `[{ "productId": Long, "quantity": Integer }]` |
| `netCancel` | Boolean| Y | `true`일 경우, 망취소 테스트를 위해 DB 저장을 강제 실패시킴 |
| `paymentMethods`| List | Y | 실제 결제/사용한 수단 정보 배열 |

-   **`paymentMethods` 상세**:

| 필드명 | 타입 | 필수 | 설명 |
| :--- | :--- | :--- | :--- |
| `paymentMethod`| String | Y | `POINT` 또는 `CREDIT_CARD` |
| `amount` | Long | Y | 해당 수단으로 결제/사용한 금액 |
| `pgCompany` | String | N | `CREDIT_CARD`일 경우 필수. `INICIS` 또는 `NICEPAY` |
| `authToken` | String | N | `CREDIT_CARD`일 경우 필수. PG로부터 받은 승인용 토큰 |

-   **핵심 로직 (의사코드)**:
    ```
    function createOrder(request):
      // --- 1. 서버 간 결제 검증 및 승인 (S2S) ---
      creditCardPayment = request.paymentMethods.find(p -> p.paymentMethod == 'CREDIT_CARD')
      if creditCardPayment:
        // PG사 API 명세에 따라 승인 요청 API 호출
        // 예: INICIS의 경우, authToken을 사용하여 https://iniapi.inicis.com/api/v1/payment 승인 요청
        isSuccess, pgResponse = callPgAuthApi(creditCardPayment.pgCompany, creditCardPayment.authToken)

        // 요청/응답 전체를 payment_interface_request_log 테이블에 저장
        logPgInterface(request.orderNumber, pgResponse)

        // 검증 실패 시: PG사에 즉시 취소 요청(망취소) 후 예외 발생
        if not isSuccess or pgResponse.amount != creditCardPayment.amount:
          callPgCancelApi(creditCardPayment.pgCompany, pgResponse.transactionId)
          throw new PaymentVerificationFailedException()

      // --- 2. 데이터베이스 트랜잭션 --- 
      try:
        // @Transactional 시작
        1. orders 테이블에 주문 정보 INSERT
        2. request.items를 순회하며 order_item 테이블에 INSERT
        3. request.paymentMethods를 순회하며 payment 테이블에 INSERT (POINT, CREDIT_CARD 각각)
        4. 포인트 사용 시, reward_points 테이블 UPDATE 및 point_history 테이블 INSERT

        // 망취소 테스트 로직
        if request.netCancel == true: throw new IntentionalException()

        // @Transactional 커밋
      catch (Exception e):
        // @Transactional 롤백
        // DB 저장 실패 시, 이미 승인된 PG 결제를 취소 (망취소)
        if creditCardPayment: callPgCancelApi(creditCardPayment.pgCompany, pgResponse.transactionId)
        throw new OrderCreationException()

      return 생성된 주문 정보
    ```
-   **성공 응답 (200 OK)**: `List<Order>` (생성된 주문 정보 객체 배열)
-   **클라이언트 유의사항**: 이 API는 클라이언트가 PG사로부터 성공(1차) 응답을 받은 후에만 호출해야 합니다.

---

### `POST /api/orders/{id}/cancel`

-   **설명**: 특정 주문을 취소합니다. PG 결제 건이 포함된 경우, 해당 PG사에 취소 API를 호출하여 환불을 진행합니다.
-   **경로 파라미터 (Path Parameter)**:

| 필드명 | 타입 | 설명 |
| :--- | :--- | :--- |
| `id` | String | 취소할 주문의 `order_id` |

-   **핵심 로직 (의사코드)**:
    ```
    function cancelOrder(orderId):
      1. orderId로 원본 주문(orders) 및 결제 내역(payment)을 DB에서 조회
      2. 이미 취소된 주문인지 확인
      3. PG 결제 내역(CREDIT_CARD)이 있는지 확인
      4. if pgPayment exists:
         // PG사 취소 API 호출 (S2S)
         isSuccess, pgResponse = callPgCancelApi(pgPayment.pgCompany, pgPayment.transactionId)
         logPgInterface(orderId, pgResponse)
         if not isSuccess: throw new PaymentCancelFailedException()

      5. // --- 데이터베이스 트랜잭션 ---
      try:
        // @Transactional 시작
        1. 원본 orders 레코드의 status를 'CANCELLED'로 UPDATE
        2. 환불 내역을 payment 테이블에 INSERT (pay_type='REFUND')
        3. 포인트 사용 건이 있었다면, reward_points 테이블에 포인트 복구 UPDATE 및 point_history 테이블에 환불 내역 INSERT
        // @Transactional 커밋
      catch (Exception e):
        // @Transactional 롤백
        // 만약 PG 취소는 성공했는데 DB 작업이 실패하면, 심각한 데이터 불일치 상태. 별도 로깅 및 수동 처리 필요.
        throw new OrderCancelFailedException()

      return 취소 처리된 주문 정보
    ```
-   **성공 응답 (200 OK)**: `Order` (업데이트된 주문 정보 객체)
