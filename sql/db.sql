select *
from "order"
where id = '20250918O00000010'
;

select * from payment
-- where order_id = '20250918O00000010'
;

select * from payment_interface_request_log

-- order 테이블명을 orders 로 바꿔줘.
-- orders 테이블의 id 를 order_id 로 바꿔줘
-- payment 테이블의 id 를 payment_id 로 바꿔줘
-- payment 테이블의 에 order_id 를 추가해줘.
-- payment_interface_request_log 의 payment_id 에 order_id 가 들어가는 중이야. payment 테이블의 id 가 들어가게끔해줘.

