# Payments API 명세

결제 프로세스 시작 및 PG사 연동 파라미터 생성을 담당하는 API 명세입니다.

---

### `POST /api/payments/initiate`

-   **설명**: 클라이언트로부터 주문 정보를 받아, 연동할 PG사를 결정하고 해당 PG사에 필요한 암호화된 파라미터를 생성하여 반환합니다.
-   **요청 본문 (Request Body)**:

| 필드명 | 타입 | 필수 | 설명 |
| :--- | :--- | :--- | :--- |
| `memberId` | Long | Y | 회원 ID |
| `amount` | Long | Y | 포인트 사용 전 총 주문 금액 |
| `usedMileage`| Long | Y | 사용할 포인트 금액 |
| `pgCompany` | String | Y | `INICIS`, `NICEPAY`, `WEIGHTED` 중 하나 |
| `goodName` | String | Y | PG 결제창에 표시될 상품명 |
| `buyerName` | String | Y | 구매자 이름 |
| `buyerTel` | String | Y | 구매자 연락처 |
| `buyerEmail` | String | Y | 구매자 이메일 |
| `orderId` | String | Y | 클라이언트에서 미리 채번한 주문번호 |

-   **핵심 로직 (의사코드)**:
    ```
    function initiatePayment(request):
      1. 요청 파라미터 유효성 검증 (금액, 회원 존재 여부 등)
      2. pgCompany가 'WEIGHTED'이면, 설정된 가중치에 따라 'INICIS' 또는 'NICEPAY'로 결정
      3. 최종 결제 금액 계산: finalAmount = request.amount - request.usedMileage
      4. PG사별 로직 분기:
         if pgCompany == 'INICIS':
           timestamp = 현재 시간
           // signature: SHA256 해시(oid + price + timestamp + secretKey)
           signature = createInicisSignature(request.orderId, finalAmount, timestamp)
           return { mid, oid, price, timestamp, signature, ... }
         if pgCompany == 'NICEPAY':
           ediDate = 현재 시간 (YYYYMMDDHHMMSS)
           // SignData: SHA256 해시(ediDate + mid + amt + secretKey)
           signData = createNicePaySignData(ediDate, mid, finalAmount)
           return { MID, Moid, Amt, EdiDate, SignData, ... }
    ```
-   **성공 응답 (200 OK)**: PG사별 요청 파라미터 객체 (JSON)
-   **클라이언트 유의사항**: 이 API의 응답값을 사용하여 PG사 결제창을 호출해야 합니다. `selectedPgCompany` 필드를 통해 실제 어떤 PG사로 결정되었는지 확인해야 합니다.
