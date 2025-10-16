# Point History API 명세

포인트의 변동 내역 전체를 다양한 조건으로 조회하는 API 명세입니다. 주로 내역 확인 및 관리자 페이지에 사용됩니다.

---

### `GET /api/point-history/member/{memberId}`
- **설명**: 특정 회원의 전체 포인트 변동 내역을 조회합니다.
- **경로 파라미터**: `memberId` (회원 ID)
- **성공 응답 (200 OK)**: `List<PointHistory>`

---

### `GET /api/point-history/member/{memberId}/statistics`
- **설명**: 특정 회원의 포인트 통계(현재 잔액, 총 적립, 총 사용)를 조회합니다.
- **경로 파라미터**: `memberId` (회원 ID)
- **성공 응답 (200 OK)**: `PointStatistics` DTO

---

### `GET /api/point-history/reference/{referenceType}/{referenceId}`
- **설명**: 특정 거래(주문, 취소 등)와 관련된 포인트 변동 내역을 조회합니다.
- **경로 파라미터**:
  - `referenceType`: 참조 타입 (예: `PAYMENT`, `CANCEL`)
  - `referenceId`: 참조 ID (예: `payment_id`, `claim_id`)
- **성공 응답 (200 OK)**: `List<PointHistory>`

---

### `GET /api/point-history/all`
- **설명**: 전체 회원의 모든 포인트 변동 내역을 조회합니다. (관리자용)
- **성공 응답 (200 OK)**: `List<PointHistory>`
