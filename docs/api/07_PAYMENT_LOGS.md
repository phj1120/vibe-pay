# Payment Logs API 명세

PG사와의 서버 간 통신(S2S) 내역을 조회하는 API 명세입니다. 주로 문제 발생 시 원인을 추적하거나 디버깅하는 용도로 사용됩니다.

---

### `GET /api/paymentlogs`
- **설명**: 전체 PG사 통신 로그 목록을 조회합니다. (관리자용)
- **성공 응답 (200 OK)**: `List<PaymentInterfaceRequestLog>`

---

### `GET /api/paymentlogs/{id}`
- **설명**: 특정 로그 ID에 해당하는 통신 로그의 상세 정보를 조회합니다.
- **경로 파라미터**: `id` (로그 ID)
- **성공 응답 (200 OK)**: `PaymentInterfaceRequestLog`
