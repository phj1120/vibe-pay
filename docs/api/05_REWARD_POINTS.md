# Reward Points API 명세

회원의 포인트(적립금)를 조회하고, 사용/추가하는 API 명세입니다.

---

### `GET /api/rewardpoints/member/{memberId}`
- **설명**: 특정 회원의 현재 보유 포인트를 조회합니다.
- **경로 파라미터**: `memberId` (회원 ID)
- **성공 응답 (200 OK)**: `RewardPoints` 객체
  ```json
  {
    "rewardPointsId": 10,
    "memberId": 1,
    "points": 5000,
    "lastUpdated": "2025-10-16T12:00:00Z"
  }
  ```

---

### `PUT /api/rewardpoints/add`
- **설명**: 특정 회원에게 포인트를 수동으로 추가(적립)합니다. (주로 관리자용)
- **요청 본문**: `RewardPointsRequest` 객체
  ```json
  {
    "memberId": 1,
    "points": 500
  }
  ```
- **성공 응답 (200 OK)**: `RewardPoints` (업데이트된 포인트 정보)
- **핵심 로직**: `point_history` 테이블에 `EARN` 타입의 레코드를 생성합니다.

---

### `PUT /api/rewardpoints/use`
- **설명**: 특정 회원의 포인트를 수동으로 차감(사용)합니다. (주로 관리자용)
- **요청 본문**: `RewardPointsRequest` 객체
  ```json
  {
    "memberId": 1,
    "points": 200
  }
  ```
- **성공 응답 (200 OK)**: `RewardPoints` (업데이트된 포인트 정보)
- **핵심 로직**: `point_history` 테이블에 `USE` 타입의 레코드를 생성합니다. 보유 포인트보다 많이 사용하려고 하면 에러를 반환합니다.
