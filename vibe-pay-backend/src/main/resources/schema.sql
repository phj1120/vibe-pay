DROP TABLE IF EXISTS payment_interface_request_log CASCADE;
DROP TABLE IF EXISTS point_history CASCADE;
DROP TABLE IF EXISTS order_item CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS reward_points CASCADE;
DROP TABLE IF EXISTS product CASCADE;
DROP TABLE IF EXISTS member CASCADE;

SELECT tablename FROM pg_tables;

-- Member Table
CREATE TABLE IF NOT EXISTS member (
    member_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255),
    phone_number VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product Table
CREATE TABLE IF NOT EXISTS product (
    product_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL
);

-- RewardPoints Table
CREATE TABLE IF NOT EXISTS reward_points (
    reward_points_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    points DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_updated TIMESTAMP NOT NULL,
    CONSTRAINT fk_member_reward_points FOREIGN KEY (member_id) REFERENCES member(member_id)
);

-- Order Table
CREATE TABLE IF NOT EXISTS orders (
                                      order_id VARCHAR(17),
                                      ord_seq INTEGER NOT NULL,
                                      ord_proc_seq INTEGER NOT NULL,
                                      claim_id VARCHAR(17),
                                      member_id BIGINT NOT NULL,
                                      order_date TIMESTAMP NOT NULL,
                                      total_amount DOUBLE PRECISION NOT NULL,
                                      used_reward_points DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                                      final_payment_amount DOUBLE PRECISION NOT NULL,
                                      status VARCHAR(50) NOT NULL,
                                      CONSTRAINT fk_member_order FOREIGN KEY (member_id) REFERENCES member(member_id),
                                      CONSTRAINT pk PRIMARY KEY (order_id, ord_seq, ord_proc_seq)
);

-- Payment Table
CREATE TABLE IF NOT EXISTS payment (
                                       payment_id VARCHAR(17) NOT NULL,
                                       member_id BIGINT NOT NULL,
                                       order_id VARCHAR(17) NOT NULL,
                                       claim_id VARCHAR(17),
                                       amount DOUBLE PRECISION NOT NULL,
                                       used_points DOUBLE PRECISION DEFAULT 0.0, -- 사용된 포인트
                                       payment_method VARCHAR(50) NOT NULL,
                                       pay_type VARCHAR(20) NOT NULL DEFAULT 'PAYMENT', -- PAYMENT(결제), REFUND(환불)
                                       pg_company VARCHAR(50), -- 포인트 결제 시 null
                                       status VARCHAR(50) NOT NULL,
                                       transaction_id VARCHAR(255),
                                       payment_date TIMESTAMP NOT NULL,
                                       CONSTRAINT fk_member_payment FOREIGN KEY (member_id) REFERENCES member(member_id),
                                       CONSTRAINT pk_payment PRIMARY KEY (payment_id, payment_method, order_id, pay_type)
);


-- OrderItem Table
CREATE TABLE IF NOT EXISTS order_item (
    order_item_id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(17) NOT NULL,
    ord_seq INTEGER NOT NULL,
    ord_proc_seq INTEGER NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_order DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);


-- PaymentInterfaceRequestLog Table
CREATE TABLE IF NOT EXISTS payment_interface_request_log (
    log_id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(17), -- payment 테이블의 payment_id 참조
    request_type VARCHAR(50) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    timestamp TIMESTAMP NOT NULL
);

-- PointHistory Table
CREATE TABLE IF NOT EXISTS point_history (
    point_history_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    point_amount DOUBLE PRECISION NOT NULL, -- 포인트 변동량 (+ 적립, - 사용)
    balance_after DOUBLE PRECISION NOT NULL, -- 변동 후 잔액
    transaction_type VARCHAR(20) NOT NULL, -- EARN(적립), USE(사용), REFUND(환불)
    reference_type VARCHAR(20), -- PAYMENT(결제), CANCEL(취소), MANUAL(수동)
    reference_id VARCHAR(50), -- 연관된 ID (payment_id, order_id 등)
    description VARCHAR(255), -- 설명
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_point_history FOREIGN KEY (member_id) REFERENCES member(member_id)
);

select * from payment_interface_request_log;

-- Member ID Sequence (8자리, 99999999까지)
CREATE SEQUENCE IF NOT EXISTS member_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999
;

-- Payment ID Sequence (8자리, 99999999까지, 순환)
CREATE SEQUENCE IF NOT EXISTS payment_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999
    CYCLE;

-- Order ID Sequence (8자리, 99999999까지, 순환)
CREATE SEQUENCE IF NOT EXISTS order_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999
    CYCLE;

-- Claim ID Sequence (8자리, 99999999까지, 순환)
CREATE SEQUENCE IF NOT EXISTS claim_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999
    CYCLE;

-- payment_interface_request_log ID Sequence (8자리, 99999999까지, 순환)
CREATE SEQUENCE IF NOT EXISTS payment_interface_request_log_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999
    CYCLE;

-- Product ID Sequence (8자리, 99999999까지)
CREATE SEQUENCE IF NOT EXISTS product_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999;

-- Product ID Sequence (8자리, 99999999까지)
CREATE SEQUENCE IF NOT EXISTS product_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999;

-- reward_points_id_seq ID Sequence (8자리, 99999999까지)
CREATE SEQUENCE IF NOT EXISTS reward_points_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999;

-- point_history_id_seq ID Sequence (8자리, 99999999까지)
CREATE SEQUENCE IF NOT EXISTS point_history_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999;


-- 초기 데이터 세팅
insert into member (member_id, name, email, phone_number) values (nextval('member_id_seq'), '현준', 'test@test.com', '010-1234-5678');
insert into product values (nextval('product_id_seq'), '상품1', 1000);
insert into product values (nextval('product_id_seq'), '상품2', 1000);
insert into reward_points (reward_points_id, member_id, points, last_updated) values (nextval('reward_points_id_seq'), currval('member_id_seq'), 1000, now());


select * from payment_interface_request_log order by timestamp desc;
select * from order_item ;
select * from payment;
select * from orders;
select * from reward_points;
select * from product;
select * from member;
select * from point_history;

select nextval('payment_id_seq')
