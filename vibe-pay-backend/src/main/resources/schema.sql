DROP TABLE IF EXISTS payment_interface_request_log CASCADE;
DROP TABLE IF EXISTS order_item CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS "order" CASCADE;
DROP TABLE IF EXISTS reward_points CASCADE;
DROP TABLE IF EXISTS product CASCADE;
DROP TABLE IF EXISTS member CASCADE;

SELECT tablename FROM pg_tables;


-- Member Table
CREATE TABLE IF NOT EXISTS member (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255),
    phone_number VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product Table
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL
);

-- RewardPoints Table
CREATE TABLE IF NOT EXISTS reward_points (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    points DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_updated TIMESTAMP NOT NULL,
    CONSTRAINT fk_member_reward_points FOREIGN KEY (member_id) REFERENCES member(id)
);


-- Payment Table
CREATE TABLE IF NOT EXISTS payment (
                                       id VARCHAR(17) PRIMARY KEY,
                                       member_id BIGINT NOT NULL,
                                       amount DOUBLE PRECISION NOT NULL,
                                       payment_method VARCHAR(50) NOT NULL,
                                       pg_company VARCHAR(50) NOT NULL,
                                       status VARCHAR(50) NOT NULL,
                                       transaction_id VARCHAR(255),
                                       payment_date TIMESTAMP NOT NULL,
                                       CONSTRAINT fk_member_payment FOREIGN KEY (member_id) REFERENCES member(id)
);

-- Order Table
CREATE TABLE IF NOT EXISTS "order" (
    id VARCHAR(17) PRIMARY KEY,
    member_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    used_reward_points DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    final_payment_amount DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_id VARCHAR(17),
    CONSTRAINT fk_member_order FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_payment_order FOREIGN KEY (payment_id) REFERENCES payment(id)
);

-- OrderItem Table
CREATE TABLE IF NOT EXISTS order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(17) NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_order DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES "order"(id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id)
);


-- PaymentInterfaceRequestLog Table
-- payment_id는 임시 ID도 저장할 수 있도록 외래키 제약조건 제거
CREATE TABLE IF NOT EXISTS payment_interface_request_log (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(17), -- 외래키 제약조건 제거 (임시 ID 지원)
    request_type VARCHAR(50) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    timestamp TIMESTAMP NOT NULL
);

-- Order ID Sequence (8자리, 99999999까지, 순환)
CREATE SEQUENCE IF NOT EXISTS order_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999
    CYCLE;

-- Payment ID Sequence (8자리, 99999999까지, 순환)
CREATE SEQUENCE IF NOT EXISTS payment_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999
    CYCLE;


select * from payment_interface_request_log;
select * from order_item;
select * from payment;
select * from "order";
select * from reward_points;
select * from product;
select * from member;