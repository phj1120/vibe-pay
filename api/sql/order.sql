-- orderNo
-- 이니시스
-- 20251103O000022: 상품 1개 카드 결제 -> 1개 취소
-- 20251103O000002: 상품 2개 카드 결제 -> 1개 취소 -> 1개 취소
-- 20251103O000023: 상품 1개 카드, 포인트 결제 -> 1개 취소
-- 20251103O000026: 상품 2개 카드, 포인트 결제 -> 1개 취소 (카드 부분 취소) -> 1개 취소(카드 전체 취소 + 포인트 전체 취소)
-- 20251103O000024: 상품 2개 카드, 포인트 결제 -> 1개 취소 (카드 전체취소 + 포인트 부분취소) -> 1개 취소(포인트 전체 취소)

-- 나이스
-- 20251103O000028: 상품 1개 카드 결제 -> 1개 취소
-- 20251103O000081: 상품 2개 카드 결제 -> 1개 취소 -> 1개 취소
-- 20251103O000029: 상품 1개 카드, 포인트 결제 -> 1개 취소
-- 20251103O000101: 상품 2개 카드, 포인트 결제 -> 1개 취소 (카드 부분 취소) -> 1개 취소(카드 전체 취소 + 포인트 전체 취소)
-- 20251103O000102: 상품 2개 카드, 포인트 결제 -> 1개 취소 (카드 전체취소 + 포인트 부분취소) -> 1개 취소(포인트 전체 취소)

select :ordNo;

select * from order_base where 1=1 and order_no = :ordNo order by regist_date_time;
select * from order_detail where 1=1 and order_no = :ordNo order by regist_date_time;
select * from order_goods where 1=1 and order_no = :ordNo order by regist_date_time;
select * from pay_base where 1=1 and order_no = :ordNo order by regist_date_time;
select * from pay_interface_log where 1=1 and pay_no in (select pay_no from pay_base where order_no = :ordNo) order by regist_date_time;
select * from point_history where 1=1 and point_transaction_reason_no in (select pay_no from pay_base where order_no = :ordNo) order by regist_date_time;
select * from basket_base order by regist_date_time;


ALTER TABLE point_history ALTER COLUMN start_date_time DROP NOT NULL;


