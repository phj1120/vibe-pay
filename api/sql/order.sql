select * from order_base;
select * from order_detail;
select * from order_goods;
select * from pay_base;
select * from pay_interface_log;
select * from point_history;
select * from basket_base;


ALTER TABLE point_history ALTER COLUMN start_date_time DROP NOT NULL;
