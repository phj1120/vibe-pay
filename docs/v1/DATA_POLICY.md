# Vibe Pay 데이터 정책 및 흐름 명세서

이 문서는 Vibe Pay의 핵심 비즈니스 시나리오가 발생했을 때, 데이터베이스의 각 테이블에 어떤 데이터가 어떤 조건으로 기록되고 변경되는지에 대한 정책을 정의합니다. 이 문서는 데이터 무결성을 보장하기 위한 개발의 기준이 됩니다.

---

## 시나리오 1: 신용카드와 포인트를 함께 사용하여 주문

**상황**: 총 주문 금액 15,000원 중 1,000 포인트를 사용하고, 나머지 14,000원을 신용카드로 결제하는 경우. (회원의 기존 보유 포인트: 5,000)

### 영향을 받는 테이블 및 데이터 변경 정책

#### `orders` 테이블
- **작업**: **INSERT** (1건)

| 속성 | 내용 |
| :--- | :--- |
| **`order_id`** | **값:** 생성된 주문번호 (예: `20251016O12345678`) |
| **`member_id`**| **값:** 주문한 회원의 ID |
| **`total_amount`**| **값:** `15000`<br>**설명:** 포인트 할인 전 총 상품 원금 |
| **`status`** | **값:** `'PAID'`<br>**설명:** 결제 완료 상태 |

#### `payment` 테이블
- **작업**: **INSERT** (2건, 동일 `payment_id`)

| 속성 | 레코드 1: 신용카드 결제 | 레코드 2: 포인트 사용 |
| :--- | :--- | :--- |
| `payment_id` | `20251016P...` (신규) | `20251016P...` (동일) |
| `order_id` | `orders.order_id` | `orders.order_id` |
| `amount` | `14000` | `1000` |
| `payment_method`| `'CREDIT_CARD'` | `'POINT'` |
| `pg_company` | `'INICIS'` 또는 `'NICEPAY'` | `NULL` |
| `transaction_id`| PG사 거래 ID | `NULL` |
| `status` | `'COMPLETED'` | `'COMPLETED'` |
| `pay_type` | `'PAYMENT'` | `'PAYMENT'` |

#### `reward_points` 테이블
- **작업**: **UPDATE** (1건)

| 속성 | 내용 |
| :--- | :--- |
| **`points`** | **AS-IS:** `5000` (예시)<br>**TO-BE:** `4000`<br>**설명:** 사용한 1000 포인트 차감 |
| **`last_updated`**| **AS-IS:** 이전 최종 수정 시간<br>**TO-BE:** 현재 시간<br>**설명:** 최종 수정 시간 갱신 |

#### `point_history` 테이블
- **작업**: **INSERT** (1건)

| 속성 | 내용 |
| :--- | :--- |
| **`member_id`** | **값:** 주문한 회원의 ID |
| **`point_amount`**| **값:** `-1000`<br>**설명:** 사용한 포인트 (음수) |
| **`balance_after`**| **값:** `4000` (변경 후 최종 잔액) |
| **`transaction_type`**| **값:** `'USE'` |
| **`reference_type`**| **값:** `'PAYMENT'` |
| **`reference_id`**| **값:** `payment.payment_id` (생성된 결제번호) |

#### `payment_interface_request_log` 테이블
- **작업**: **INSERT** (1건) 후 **UPDATE** (1건)
- **설명**: PG사와의 서버 간 통신을 기록합니다. 요청 직전 INSERT, 응답 수신 후 UPDATE합니다.

**1. INSERT (요청 시점)**
| 속성 | 내용 |
| :--- | :--- |
| **`payment_id`** | **값:** `payment.payment_id` |
| **`request_type`** | **값:** `'INICIS_AUTH'` 등 |
| **`request_payload`**| **값:** PG사에 보낸 요청 전문 |
| **`response_payload`**| **값:** `NULL` |

**2. UPDATE (응답 수신 후)**
- **대상 레코드**: 위에서 INSERT된 `log_id`에 해당하는 레코드
| 속성 | 내용 |
| :--- | :--- |
| **`response_payload`**| **AS-IS:** `NULL`<br>**TO-BE:** PG사로부터 받은 응답 전문 |

---

## 시나리오 2: 신용카드만 사용하여 주문

- **`payment` 테이블**: **1건**의 `CREDIT_CARD` 레코드만 **INSERT** 됩니다.
- **`reward_points`, `point_history` 테이블**: **변경 없음**.
*(그 외 테이블은 시나리오 1과 동일)*

---

## 시나리오 3: 포인트만 사용하여 전액 주문

- **`payment` 테이블**: **1건**의 `POINT` 레코드만 **INSERT** 됩니다.
- **`reward_points`, `point_history` 테이블**: 포인트 사용 내역이 기록되고 잔액이 **UPDATE** 됩니다.
- **`payment_interface_request_log` 테이블**: PG사 연동이 없으므로, **레코드 생성 없음**.
*(그 외 테이블은 시나리오 1과 동일)*

---

## 시나리오 4: 결제 완료된 주문 취소

**상황**: 시나리오 1(카드 14,000원 + 포인트 1,000원)로 결제된 주문을 전체 취소하는 경우.

#### `orders` 테이블
- **작업**: **UPDATE** (1건)

| 속성 | 내용 |
| :--- | :--- |
| **`status`** | **AS-IS:** `'PAID'`<br>**TO-BE:** `'CANCELLED'`<br>**설명:** 주문 상태를 '취소'로 변경 |

#### `payment` 테이블
- **작업**: **INSERT** (2건, `pay_type` = `'REFUND'`)

| 속성 | 레코드 1: 신용카드 환불 | 레코드 2: 포인트 환불 |
| :--- | :--- | :--- |
| `payment_id` | 원본 결제의 `payment_id` | 원본 결제의 `payment_id` |
| `amount` | `14000` | `1000` |
| `payment_method`| `'CREDIT_CARD'` | `'POINT'` |
| `status` | `'COMPLETED'` | `'COMPLETED'` |
| `pay_type` | `'REFUND'` | `'REFUND'` |
| `claim_id` | 신규 클레임 ID | 동일한 클레임 ID |

#### `reward_points` 테이블
- **작업**: **UPDATE** (1건)

| 속성 | 내용 |
| :--- | :--- |
| **`points`** | **AS-IS:** `4000` (예시)<br>**TO-BE:** `5000`<br>**설명:** 사용했던 1000 포인트 복구 |

#### `point_history` 테이블
- **작업**: **INSERT** (1건)

| 속성 | 내용 |
| :--- | :--- |
| **`point_amount`**| **값:** `1000`<br>**설명:** 환불된 포인트 (양수) |
| **`transaction_type`**| **값:** `'REFUND'` |
| **`reference_id`**| **값:** `claim_id` (생성된 클레임 ID) |

#### `payment_interface_request_log` 테이블
- **작업**: **INSERT** 후 **UPDATE** (PG사 취소 통신 1건)
- **설명**: `request_type`이 `'INICIS_CANCEL'` 등으로 기록됩니다.