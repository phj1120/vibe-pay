## 포인트

필요한 기능: 포인트 사용/적립 내역 조회, 포인트 사용/적립 처리

관련테이블: point_history

- 포인트 사용/적립 처리
    - Request

      | amount | 금액 | varchar(50) |  |  |
              | --- | --- | --- | --- | --- |
      | point_transaction_code | 포인트적립사용코드 | varchar(50) |  | MEM002 / 001: 적립 / 002: 사용 |
      | point_transaction_reson_code | 포인트적립사용사유코드 | varchar(3) |  | MEM003 / 001: 기타 / 002: 주문 |
      | point_transaction_reson_no | 포인트적립사용번호 | varchar(50) |  |  |
    - Response
        - 응답 없음. HTTP 응답 코드로 성공 실패 처리. 실패시 예외 처리 메시지
    - 프로세스
        - start_date_time: 오늘 날짜
          end_date_time: 오늘 날짜 + ME003 의 참조값 1번값 유효기간일수
        - 사용 처리 시 현재 총 사용가능한 금액이상 사용 못하도록 검증.
        - 사용 처리 시 종료기간이 임박한 적립 건 부터 사용 처리 시작.
            - 예시.  현재 시간 2025-10-28 10:22:00
                - 예시 1: 1000 포인트 충전

                  | point_history_no | upper_point_history_no | remain_point | amount | point_transaction_code | start_date_time | end_date_time | point_transaction_reson_code | point_transaction_reson_no |
                                      | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                  | 1 |  | 1000 | 1000 | 001(적립) | 2025-10-11 00:00:00 | 2026-02-12 00:00:00 | 001 |  |
                - 예시 2 1000포인트 있는 회원 202510228O00001 주문으로 600포인트 사용.

                  | point_history_no | upper_point_history_no | remain_point | amount | point_transaction_code | start_date_time | end_date_time | point_transaction_reson_code | point_transaction_reson_no |
                                      | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                  | 1 |  | 400 | 1000 | 001(적립) | 2025-10-11 00:00:00 | 2026-02-12 00:00:00 | 001 |  |
                  | 2 | 1 |  | 600 | 002(사용) |  |  | 002 | 202510228O00001 |
                - 예시 3 1000포인트 충전

                  | point_history_no | upper_point_history_no | remain_point | amount | point_transaction_code | start_date_time | end_date_time | point_transaction_reson_code | point_transaction_reson_no |
                                      | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                  | 1 |  | 400 | 1000 | 001(적립) | 2025-10-11 00:00:00 | 2026-02-12 00:00:00 | 001 |  |
                  | 2 | 1 |  | 600 | 002(사용) |  |  | 002 | 202510228O00001 |
                  | 3 |  | 1000 | 1000 | 001(적립) | 2025-10-12 00:00:00 | 2026-02-13 00:00:00 | 001 |  |

                - 예시4 1400포인트 있는 회원 202510228O00002 주문으로 1000 포인트 사용

                  | point_history_no | upper_point_history_no | remain_point | amount | point_transaction_code | start_date_time | end_date_time | point_transaction_reson_code | point_transaction_reson_no |
                                      | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                  | 1 |  | 0 | 1000 | 001(적립) | 2025-10-11 00:00:00 | 2026-02-12 00:00:00 | 001 |  |
                  | 2 | 1 |  | 600 | 002(사용) |  |  | 002 | 202510228O00001 |
                  | 3 |  | 600 | 1000 | 001(적립) | 2025-10-11 00:00:00 | 2026-02-12 00:00:00 | 001 |  |
                  | 4 | 1 |  | 400 | 002(사용) |  |  | 002 | 202510228O00002 |
                  | 5 | 3 | 400 | 600 | 002(사용) |  |  | 002 | 202510228O00002 |

                - 예시 5

                  | point_history_no | upper_point_history_no | remain_point | amount | point_transaction_code | start_date_time | end_date_time | point_transaction_reson_code | point_transaction_reson_no |
                                      | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                  | 7 |  | 1000 | 1000 | 001(적립) | 2024-10-11 00:00:00 | 2025-02-12 00:00:00 | 001 |  |

                  사용 기간이 지난 포인트는 사용 불가능.