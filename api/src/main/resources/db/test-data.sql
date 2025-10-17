-- ==========================================
-- VibePay 결제 테스트용 초기 데이터
-- ==========================================

-- 1. 테스트 회원 추가
INSERT INTO member (member_id, name, email, phone, created_at)
VALUES
    (1, '홍길동', 'hong@test.com', '01012345678', NOW()),
    (2, '김철수', 'kim@test.com', '01087654321', NOW()),
    (3, '이영희', 'lee@test.com', '01011112222', NOW())
ON CONFLICT (member_id) DO NOTHING;

-- 2. 테스트 상품 추가
INSERT INTO product (product_id, name, description, price, stock, category, status, created_at)
VALUES
    (1, '노트북', 'LG 그램 17인치 노트북', 1500000, 50, 'ELECTRONICS', 'ACTIVE', NOW()),
    (2, '무선 마우스', '로지텍 MX Master 3', 120000, 100, 'ELECTRONICS', 'ACTIVE', NOW()),
    (3, '키보드', '해피해킹 프로페셔널 타입-S', 290000, 30, 'ELECTRONICS', 'ACTIVE', NOW()),
    (4, '모니터', '삼성 오디세이 32인치', 650000, 20, 'ELECTRONICS', 'ACTIVE', NOW()),
    (5, '이어폰', 'AirPods Pro 2세대', 350000, 80, 'ELECTRONICS', 'ACTIVE', NOW()),
    (6, '스마트워치', 'Apple Watch Series 9', 590000, 40, 'ELECTRONICS', 'ACTIVE', NOW()),
    (7, '태블릿', 'iPad Pro 12.9', 1490000, 25, 'ELECTRONICS', 'ACTIVE', NOW()),
    (8, '외장 SSD', '삼성 T7 Shield 2TB', 280000, 60, 'ELECTRONICS', 'ACTIVE', NOW()),
    (9, '웹캠', '로지텍 BRIO 4K', 250000, 35, 'ELECTRONICS', 'ACTIVE', NOW()),
    (10, '휴대폰 거치대', '벨킨 MagSafe 3-in-1', 180000, 70, 'ELECTRONICS', 'ACTIVE', NOW())
ON CONFLICT (product_id) DO NOTHING;

-- 3. 회원 포인트 초기화
INSERT INTO reward_points (member_id, total_points, available_points, used_points, created_at)
VALUES
    (1, 100000, 100000, 0, NOW()),
    (2, 50000, 50000, 0, NOW()),
    (3, 30000, 30000, 0, NOW())
ON CONFLICT (member_id) DO NOTHING;

-- 4. 테스트 주문 추가 (선택적)
-- 필요 시 주석 해제하여 사용
-- INSERT INTO orders (order_id, ord_seq, ord_proc_seq, claim_id, member_id, order_date, total_amount, status, created_at)
-- VALUES
--     ('ORD20250101001', 1, 1, NULL, 1, NOW(), 1620000.00, 'ORDERED', NOW()),
--     ('ORD20250101002', 1, 1, NULL, 2, NOW(), 650000.00, 'ORDERED', NOW())
-- ON CONFLICT (order_id, ord_seq, ord_proc_seq) DO NOTHING;

-- 5. 테스트 주문 상품 추가 (선택적)
-- 필요 시 주석 해제하여 사용
-- INSERT INTO order_item (order_id, ord_seq, ord_proc_seq, product_id, quantity, price_at_order, created_at)
-- VALUES
--     ('ORD20250101001', 1, 1, 1, 1, 1500000.00, NOW()),  -- 노트북 1개
--     ('ORD20250101001', 1, 1, 2, 1, 120000.00, NOW()),   -- 무선 마우스 1개
--     ('ORD20250101002', 1, 1, 4, 1, 650000.00, NOW())    -- 모니터 1개
-- ON CONFLICT DO NOTHING;

-- 시퀀스 초기화 (PostgreSQL)
SELECT setval('member_seq', (SELECT COALESCE(MAX(member_id), 0) + 1 FROM member), false);
SELECT setval('product_seq', (SELECT COALESCE(MAX(product_id), 0) + 1 FROM product), false);
SELECT setval('order_seq', 1, false);
SELECT setval('payment_seq', 1, false);
SELECT setval('claim_seq', 1, false);

-- 완료 메시지
SELECT '✅ Test data inserted successfully!' AS status;
