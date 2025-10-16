# Members API 명세

회원 정보의 생성, 조회, 수정, 삭제(CRUD) 및 연관 정보 조회를 담당하는 API 명세입니다.

---

### `GET /api/members`
- **설명**: 전체 회원 목록을 조회합니다.
- **성공 응답 (200 OK)**: `List<Member>`

---

### `GET /api/members/{memberId}`
- **설명**: 특정 회원의 상세 정보를 조회합니다.
- **경로 파라미터**: `memberId` (회원 ID)
- **성공 응답 (200 OK)**: `Member`

---

### `POST /api/members`
- **설명**: 새로운 회원을 등록합니다.
- **요청 본문**: `Member` 객체
- **성공 응답 (200 OK)**: 생성된 `Member` 객체 (ID 포함)

---

### `PUT /api/members/{memberId}`
- **설명**: 특정 회원의 정보를 수정합니다.
- **경로 파라미터**: `memberId` (회원 ID)
- **요청 본문**: `Member` 객체 (업데이트할 정보)
- **성공 응답 (200 OK)**: 수정된 `Member` 객체

---

### `DELETE /api/members/{memberId}`
- **설명**: 특정 회원을 삭제합니다.
- **경로 파라미터**: `memberId` (회원 ID)
- **성공 응답 (204 No Content)**: 응답 본문 없음

---

### `GET /api/members/{memberId}/point-history`
- **설명**: 특정 회원의 포인트 변동 내역을 페이징하여 조회합니다.
- **경로 파라미터**: `memberId` (회원 ID)
- **쿼리 파라미터**:
  - `page`: 페이지 번호 (0부터 시작, 기본값 0)
  - `size`: 페이지당 항목 수 (기본값 10)
- **성공 응답 (200 OK)**: `List<PointHistory>`

---

### `GET /api/members/{memberId}/order-history`
- **설명**: 특정 회원의 주문 내역을 페이징하여 조회합니다.
- **경로 파라미터**: `memberId` (회원 ID)
- **쿼리 파라미터**:
  - `page`: 페이지 번호 (0부터 시작, 기본값 0)
  - `size`: 페이지당 항목 수 (기본값 10)
- **성공 응답 (200 OK)**: `List<OrderDetailDto>` (주문 상세 정보 DTO 배열)
