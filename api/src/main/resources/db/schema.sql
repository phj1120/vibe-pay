-- ==========================================
-- VibePay 결제 테스트용 간단 스키마
-- ==========================================

-- 1. 회원 테이블
CREATE TABLE IF NOT EXISTS member (
    member_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 상품 테이블
CREATE TABLE IF NOT EXISTS product (
    product_id BIGINT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price BIGINT NOT NULL,
    stock INT DEFAULT 0,
    category VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 리워드 포인트 테이블
CREATE TABLE IF NOT EXISTS reward_points (
    member_id BIGINT PRIMARY KEY,
    total_points BIGINT DEFAULT 0,
    available_points BIGINT DEFAULT 0,
    used_points BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(member_id)
);

-- 4. 주문 테이블
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(50) NOT NULL,
    ord_seq INT NOT NULL,
    ord_proc_seq INT NOT NULL,
    claim_id VARCHAR(50),
    member_id BIGINT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ORDERED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id, ord_seq, ord_proc_seq),
    FOREIGN KEY (member_id) REFERENCES member(member_id)
);

-- 5. 주문 상품 테이블
CREATE TABLE IF NOT EXISTS order_item (
    order_item_id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    ord_seq INT NOT NULL,
    ord_proc_seq INT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price_at_order DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id, ord_seq, ord_proc_seq) REFERENCES orders(order_id, ord_seq, ord_proc_seq),
    FOREIGN KEY (product_id) REFERENCES product(product_id)
);

-- 6. 시퀀스 생성
CREATE SEQUENCE IF NOT EXISTS member_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS product_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS order_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS payment_seq START WITH 1;
CREATE SEQUENCE IF NOT EXISTS claim_seq START WITH 1;

-- 완료 메시지
SELECT '✅ Schema created successfully!' AS status;
