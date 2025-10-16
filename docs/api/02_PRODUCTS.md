# Products API 명세

상품 정보의 생성, 조회, 수정, 삭제(CRUD)를 담당하는 API 명세입니다.

---

### `GET /api/products`
- **설명**: 전체 상품 목록을 조회합니다.
- **성공 응답 (200 OK)**: `List<Product>`

---

### `GET /api/products/{productId}`
- **설명**: 특정 상품의 상세 정보를 조회합니다.
- **경로 파라미터**: `productId` (상품 ID)
- **성공 응답 (200 OK)**: `Product`

---

### `POST /api/products`
- **설명**: 새로운 상품을 등록합니다.
- **요청 본문**: `Product` 객체
- **성공 응답 (200 OK)**: 생성된 `Product` 객체 (ID 포함)

---

### `PUT /api/products/{productId}`
- **설명**: 특정 상품의 정보를 수정합니다.
- **경로 파라미터**: `productId` (상품 ID)
- **요청 본문**: `Product` 객체 (업데이트할 정보)
- **성공 응답 (200 OK)**: 수정된 `Product` 객체

---

### `DELETE /api/products/{productId}`
- **설명**: 특정 상품을 삭제합니다.
- **경로 파라미터**: `productId` (상품 ID)
- **성공 응답 (204 No Content)**: 응답 본문 없음
