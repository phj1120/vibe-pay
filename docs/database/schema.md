### basket_base / 장바구니 정보

| basket_no | 장바구니번호 | varchar(15) | PK | 시퀀스 |
| --- | --- | --- | --- | --- |
| member_no | 회원번호 | varchar(15) |  |  |
| goods_no | 상품번호 | varchar(15) |  |  |
| item_no | 단품번호 | varchar(3) |  |  |
| quantity | 수량 | numeric |  |  |
| is_order | 주문여부 | boolean |  |  |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

### order_base / 주문 기본 정보

| order_no | 주문번호 | varchar(15) | PK | 날짜+O+시퀀스 / ex.20251027O000001 |
| --- | --- | --- | --- | --- |
| member_no | 회원번호 | varchar(15) |  |  |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

### order_detail /  주문 상세 정보

| order_no | 주문번호 | varchar(15) | PK |  |
| --- | --- | --- | --- | --- |
| order_sequence | 주문순번 | numeric | PK |  |
| order_process_sequence | 주문처리순번 | numeric | PK |  |
| upper_order_process_sequence | 상위주문처리순번 | numeric |  |  |
| claim_no | 클레임번호 | varchar(15) |  | 날짜+C+시퀀스 / ex.20251027O000001 |
| goods_no | 상품번호 | varchar(15) |  |  |
| item_no | 단품번호 | varchar(3) |  |  |
| quantity | 수량 | numeric |  |  |
| order_status_code | 주문상태코드 | varchar(3) |  | ORD002/ 001: 주문접수/ 002: 주문완료/ 003: 주문취소/ 107: 배송완료 / 207: 반품완료 |
| delivery_type_code | 배송구분코드 | varchar(3) |  | DLV001 / 001: 출하 /002: 회수 |
| order_type_code | 주문유형코드 | varchar(3) |  | ORD001 / 001: 주문 / 002: 주문취소 / 101: 반품 / 102: 반품취소 / 201: 교환 / 202: 교환취소 |
| order_accept_dtm | 주문접수일시 | Date |  |  |
| order_finish_dtm | 주문완료일시 | Date |  |  |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

상위주문처리순번

단순히 -1 하는 방향으로는 안될까 하다가, 원주문을 찾기 위해서는 꼭 필요.

1. 교환 시 출하, 회수로우 관리

2. 클레임 취소 시엔 원클레임 건이 원번호 기준.

3. 클레임 취소 후 재 클레임 시 원번호 기준으로 상위 주문 처리 순번 세팅

### order_goods /  주문 상품 정보

| order_no | 주문번호 | varchar(15) | PK |  |
| --- | --- | --- | --- | --- |
| goods_no | 상품번호 | varchar(15) | PK |  |
| item_no | 단품번호 | varchar(3) | PK |  |
| sale_price | 판매가 | numeric |  |  |
| supply_price | 공급원가 | numeric |  |  |
| goods_name | 상품명 | varchar |  |  |
| item_name | 단품명 | varchar |  |  |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

### pay_base /  결제 기본 정보

| pay_no | 결제 번호 | varchar(15) | PK | 시퀀스 |
| --- | --- | --- | --- | --- |
| pay_type_code | 결제유형코드 | varchar(3) |  | PAY001 / 001: 결제 / 002: 환불 |
| pay_way_code | 결제방식코드 | varchar(3) |  | PAY002 / 001: 신용카드 /002: 포인트 |
| pay_status_code | 결제상태코드 | varchar(3) |  | PAY003/ 001: 결제대기/ 002: 결제완료/ 003: 결제취소/ 101: 환불접수/ 102: 환불완료 / 103: 환불접수취소 |
| approve_no | 승인번호 | varchar(15) |  |  |
| order_no | 주문번호 | varchar(15) |  |  |
| claim_no | 클레임번호 | varchar(15) |  |  |
| upper_pay_no | 상위결제번호 | varchar(15) |  |  |
| trd_no | 거래번호 | varchar(15) |  |  |
| pay_finish_date_time | 결제완료일시 | date |  |  |
| member_no | 회원번호 | varchar(15) |  |  |
| amount | 결제금액 | numeric |  |  |
| cancelable_amount | 취소가능금액 | numeric |  |  |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

### pay_interface_log /  결제 인터페이스 로그

| pay_interface_no | 결제 인터페이스 번호 | varchar(15) | PK | 시퀀스 |
| --- | --- | --- | --- | --- |
| member_no | 회원번호 | varchar(15) |  |  |
| pay_no | 결제 번호 | varchar(15) |  |  |
| pay_log_code | 로그 유형 코드 | varchar(3) |  | PAY004 / 001: 결제 / 002: 승인 / 003: 망취소 |
| request_json | 요청 JSON | JSON |  |  |
| response_json | 응답 JSON | JSON |  |  |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

### code_base /  공통 코드 정보

| group_code | 그룹코드 | varchar(6) | PK |  |
| --- | --- | --- | --- | --- |
| group_code_name | 그룹코드명 | varchar(15) |  |  |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

### code_detail /  공통 코드 상세 정보

| group_code | 그룹코드 | varchar(15) | PK |  |
| --- | --- | --- | --- | --- |
| code | 코드 | varchar(3) | PK |  |
| code_name | 코드명 | varchar(15) |  |  |
| reference_value_1 | 참조값1 | varchar |  |  |
| reference_value_2 | 참조값2 | varchar |  |  |
| display_sequence | 정렬순서 | numeric |  |  |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

### member_base /  회원 정보

| member_no | 회원번호 | varchar(15) | PK | 시퀀스 |
| --- | --- | --- | --- | --- |
| member_name | 회원명 | varchar(50) |  |  |
| phone | 전화번호 | varchar(50) |  |  |
| email | 이메일 | varchar(50) |  |  |
| password | 비밀번호 | varchar(255) |  |  |
| member_status_code | 회원상태코드 | varchar(3) |  | MEM001 / 001: 정상회원 / 002: 탈퇴회원 |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

### point_history /  포인트 기록

| point_history_no | 포인트기록번호 | varchar(15) | PK | 시퀀스 |
| --- | --- | --- | --- | --- |
| member_no | 회원번호 | varchar(50) |  |  |
| amount | 금액 | varchar(50) |  |  |
| pointTransactionCode | 포인트적립사용코드 | varchar(50) |  | MEM002 / 001: 적립 / 002: 사용 |
| point_transaction_reson_code | 포인트적립사용사유코드 | varchar(3) |  | MEM003 / 001: 기타 / 002: 주문 |
| point_transaction_reson_no | 포인트적립사용번호 | varchar(50) |  |  |
| start_date_time | 시작일시 | date |  |  |
| end_date_time | 종료일시 | date |  |  |
| upper_point_history_no | 원포인트기록번호 | varchar(15) |  |  |
| remain_point | 잔여유효포인트 | long |  |  |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

### goods_base /  상품 정보

| goods_no | 상품번호 | varchar(15)  | PK | G+시퀀스 |
| --- | --- |--------------| --- | --- |
| goods_name | 상품명 | varchar(50)  |  |  |
| goods_status_code | 상품상태코드 | varchar(3)   |  | PRD001 / 001: 판매중, 002: 판매중단, 003: 판매중단 |
| goods_main_image_url | 상품대표이미지주소 | varchar(200) |  |  |
| regist_id | 등록자 | varchar(15)  |  |  |
| regist_date_time | 등록일시 | timestamp    |  |  |
| modify_id | 수정자 | varchar(15)  |  |  |
| modify_date_time | 수정일시 | timestamp    |  |  |

### goods_item /  단품 정보

| goods_no | 상품번호 | varchar(15) | PK |  |
| --- | --- | --- | --- | --- |
| item_no | 단품번호 | varchar(3) | PK |  |
| item_name | 단품명 | varchar(50) |  |  |
| item_price | 단품금액 | numeric |  |  |
| stock | 재고수량 | numeric |  |  |
| goods_status_code | 단품상태코드 | varchar(3) |  | PRD001 |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |

### goods_price_hist /  상품 가격 정보

| goods_no | 상품번호 | varchar(15) | PK |  |
| --- | --- | --- | --- | --- |
| start_date_time | 시작일 | date | PK |  |
| end_date_time | 종료일 | date | PK |  |
| sale_price | 판매가 | numeric |  |  |
| supply_price | 공급원가 | numeric |  |  |
| regist_id | 등록자 | varchar(15) |  |  |
| regist_date_time | 등록일시 | timestamp |  |  |
| modify_id | 수정자 | varchar(15) |  |  |
| modify_date_time | 수정일시 | timestamp |  |  |