-- =============================================
-- Database DDL Script
-- Project: vibe-pay
-- Description: 테이블 생성 스크립트
-- =============================================

-- =============================================
-- 시퀀스 생성
-- =============================================

-- basket_no 시퀀스 (15자리)
CREATE SEQUENCE SEQ_BASKET_NO
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999999999999
    CACHE 20;

COMMENT ON SEQUENCE SEQ_BASKET_NO IS '장바구니번호 시퀀스';

-- order_no 시퀀스 (날짜(8) + O(1) + 시퀀스(6) = 15자리)
CREATE SEQUENCE SEQ_ORDER_NO
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999
    CYCLE
    CACHE 20;

COMMENT ON SEQUENCE SEQ_ORDER_NO IS '주문번호 시퀀스';

-- claim_no 시퀀스 (날짜(8) + C(1) + 시퀀스(6) = 15자리)
CREATE SEQUENCE SEQ_CLAIM_NO
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999
    CYCLE
    CACHE 20;

COMMENT ON SEQUENCE SEQ_CLAIM_NO IS '클레임번호 시퀀스';

-- pay_no 시퀀스 (15자리)
CREATE SEQUENCE SEQ_PAY_NO
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999999999999
    CACHE 20;

COMMENT ON SEQUENCE SEQ_PAY_NO IS '결제번호 시퀀스';

-- pay_interface_no 시퀀스 (15자리)
CREATE SEQUENCE SEQ_PAY_INTERFACE_NO
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999999999999
    CACHE 20;

COMMENT ON SEQUENCE SEQ_PAY_INTERFACE_NO IS '결제인터페이스번호 시퀀스';

-- member_no 시퀀스 (15자리)
CREATE SEQUENCE SEQ_MEMBER_NO
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999999999999
    CACHE 20;

COMMENT ON SEQUENCE SEQ_MEMBER_NO IS '회원번호 시퀀스';

-- point_history_no 시퀀스 (15자리)
CREATE SEQUENCE SEQ_POINT_HISTORY_NO
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999999999999
    CACHE 20;

COMMENT ON SEQUENCE SEQ_POINT_HISTORY_NO IS '포인트기록번호 시퀀스';

-- goods_no 시퀀스 (G + 14자리 시퀀스 = 15자리)
CREATE SEQUENCE SEQ_GOODS_NO
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 99999999999999
    CACHE 20;

COMMENT ON SEQUENCE SEQ_GOODS_NO IS '상품번호 시퀀스';

-- =============================================
-- 테이블 생성
-- =============================================

-- =============================================
-- 1. basket_base (장바구니 정보)
-- =============================================
CREATE TABLE BASKET_BASE (
    BASKET_NO       VARCHAR(15)     NOT NULL,   -- 장바구니번호 (PK, 시퀀스)
    MEMBER_NO       VARCHAR(15)     NOT NULL,   -- 회원번호
    GOODS_NO        VARCHAR(15)     NOT NULL,   -- 상품번호
    ITEM_NO         VARCHAR(3)      NOT NULL,   -- 단품번호
    QUANTITY        NUMERIC         NOT NULL,   -- 수량
    IS_ORDER        BOOLEAN         NOT NULL DEFAULT FALSE,   -- 주문여부
    CONSTRAINT PK_BASKET_BASE PRIMARY KEY (BASKET_NO)
);

COMMENT ON TABLE BASKET_BASE IS '장바구니 정보';
COMMENT ON COLUMN BASKET_BASE.BASKET_NO IS '장바구니번호';
COMMENT ON COLUMN BASKET_BASE.MEMBER_NO IS '회원번호';
COMMENT ON COLUMN BASKET_BASE.GOODS_NO IS '상품번호';
COMMENT ON COLUMN BASKET_BASE.ITEM_NO IS '단품번호';
COMMENT ON COLUMN BASKET_BASE.QUANTITY IS '수량';
COMMENT ON COLUMN BASKET_BASE.IS_ORDER IS '주문여부';

-- =============================================
-- 2. order_base (주문 기본 정보)
-- =============================================
CREATE TABLE ORDER_BASE (
    ORDER_NO        VARCHAR(15)     NOT NULL,   -- 주문번호 (PK, 날짜+O+시퀀스 ex.20251027O000001)
    MEMBER_NO       VARCHAR(15)     NOT NULL,   -- 회원번호
    CONSTRAINT PK_ORDER_BASE PRIMARY KEY (ORDER_NO)
);

COMMENT ON TABLE ORDER_BASE IS '주문 기본 정보';
COMMENT ON COLUMN ORDER_BASE.ORDER_NO IS '주문번호';
COMMENT ON COLUMN ORDER_BASE.MEMBER_NO IS '회원번호';

-- =============================================
-- 3. order_detail (주문 상세 정보)
-- =============================================
CREATE TABLE ORDER_DETAIL (
    ORDER_NO                        VARCHAR(15)     NOT NULL,   -- 주문번호 (PK)
    ORDER_SEQUENCE                  NUMERIC         NOT NULL,   -- 주문순번 (PK)
    ORDER_PROCESS_SEQUENCE          NUMERIC         NOT NULL,   -- 주문처리순번 (PK)
    UPPER_ORDER_PROCESS_SEQUENCE    NUMERIC,                    -- 상위주문처리순번
    CLAIM_NO                        VARCHAR(15),                -- 클레임번호 (날짜+C+시퀀스 ex.20251027C000001)
    GOODS_NO                        VARCHAR(15)     NOT NULL,   -- 상품번호
    ITEM_NO                         VARCHAR(3)      NOT NULL,   -- 단품번호
    QUANTITY                        NUMERIC         NOT NULL,   -- 수량
    ORDER_STATUS_CODE               VARCHAR(3)      NOT NULL,   -- 주문상태코드 (ORD002)
    DELIVERY_TYPE_CODE              VARCHAR(3)      NOT NULL,   -- 배송구분코드 (DLV001)
    ORDER_TYPE_CODE                 VARCHAR(3)      NOT NULL,   -- 주문유형코드 (ORD001)
    ORDER_ACCEPT_DTM                TIMESTAMP,                  -- 주문접수일시
    ORDER_FINISH_DTM                TIMESTAMP,                  -- 주문완료일시
    CONSTRAINT PK_ORDER_DETAIL PRIMARY KEY (ORDER_NO, ORDER_SEQUENCE, ORDER_PROCESS_SEQUENCE)
);

COMMENT ON TABLE ORDER_DETAIL IS '주문 상세 정보';
COMMENT ON COLUMN ORDER_DETAIL.ORDER_NO IS '주문번호';
COMMENT ON COLUMN ORDER_DETAIL.ORDER_SEQUENCE IS '주문순번';
COMMENT ON COLUMN ORDER_DETAIL.ORDER_PROCESS_SEQUENCE IS '주문처리순번';
COMMENT ON COLUMN ORDER_DETAIL.UPPER_ORDER_PROCESS_SEQUENCE IS '상위주문처리순번';
COMMENT ON COLUMN ORDER_DETAIL.CLAIM_NO IS '클레임번호';
COMMENT ON COLUMN ORDER_DETAIL.GOODS_NO IS '상품번호';
COMMENT ON COLUMN ORDER_DETAIL.ITEM_NO IS '단품번호';
COMMENT ON COLUMN ORDER_DETAIL.QUANTITY IS '수량';
COMMENT ON COLUMN ORDER_DETAIL.ORDER_STATUS_CODE IS '주문상태코드 (ORD002: 001-주문접수/002-주문완료/003-주문취소/107-배송완료/207-반품완료)';
COMMENT ON COLUMN ORDER_DETAIL.DELIVERY_TYPE_CODE IS '배송구분코드 (DLV001: 001-출하/002-회수)';
COMMENT ON COLUMN ORDER_DETAIL.ORDER_TYPE_CODE IS '주문유형코드 (ORD001: 001-주문/002-주문취소/101-반품/102-반품취소/201-교환/202-교환취소)';
COMMENT ON COLUMN ORDER_DETAIL.ORDER_ACCEPT_DTM IS '주문접수일시';
COMMENT ON COLUMN ORDER_DETAIL.ORDER_FINISH_DTM IS '주문완료일시';

-- =============================================
-- 4. order_goods (주문 상품 정보)
-- =============================================
CREATE TABLE ORDER_GOODS (
    ORDER_NO        VARCHAR(15)     NOT NULL,   -- 주문번호 (PK)
    GOODS_NO        VARCHAR(15)     NOT NULL,   -- 상품번호 (PK)
    ITEM_NO         VARCHAR(3)      NOT NULL,   -- 단품번호 (PK)
    SALE_PRICE      NUMERIC         NOT NULL,   -- 판매가
    SUPPLY_PRICE    NUMERIC         NOT NULL,   -- 공급원가
    GOODS_NAME      VARCHAR(200)    NOT NULL,   -- 상품명
    ITEM_NAME       VARCHAR(200)    NOT NULL,   -- 단품명
    CONSTRAINT PK_ORDER_GOODS PRIMARY KEY (ORDER_NO, GOODS_NO, ITEM_NO)
);

COMMENT ON TABLE ORDER_GOODS IS '주문 상품 정보';
COMMENT ON COLUMN ORDER_GOODS.ORDER_NO IS '주문번호';
COMMENT ON COLUMN ORDER_GOODS.GOODS_NO IS '상품번호';
COMMENT ON COLUMN ORDER_GOODS.ITEM_NO IS '단품번호';
COMMENT ON COLUMN ORDER_GOODS.SALE_PRICE IS '판매가';
COMMENT ON COLUMN ORDER_GOODS.SUPPLY_PRICE IS '공급원가';
COMMENT ON COLUMN ORDER_GOODS.GOODS_NAME IS '상품명';
COMMENT ON COLUMN ORDER_GOODS.ITEM_NAME IS '단품명';

-- =============================================
-- 5. pay_base (결제 기본 정보)
-- =============================================
CREATE TABLE PAY_BASE (
    PAY_NO              VARCHAR(15)     NOT NULL,   -- 결제번호 (PK, 시퀀스)
    PAY_TYPE_CODE       VARCHAR(3)      NOT NULL,   -- 결제유형코드 (PAY001: 001-결제/002-환불)
    PAY_WAY_CODE        VARCHAR(3)      NOT NULL,   -- 결제방식코드 (PAY002: 001-신용카드/002-포인트)
    PAY_STATUS_CODE     VARCHAR(3)      NOT NULL,   -- 결제상태코드 (PAY003)
    APPROVE_NO          VARCHAR(15),                -- 승인번호
    ORDER_NO            VARCHAR(15),                -- 주문번호
    CLAIM_NO            VARCHAR(15),                -- 클레임번호
    UPPER_PAY_NO        VARCHAR(15),                -- 상위결제번호
    TRD_NO              VARCHAR(15),                -- 거래번호
    PAY_FINISH_DATE_TIME TIMESTAMP,                 -- 결제완료일시
    MEMBER_NO           VARCHAR(15)     NOT NULL,   -- 회원번호
    AMOUNT              NUMERIC         NOT NULL,   -- 결제금액
    CANCELABLE_AMOUNT   NUMERIC         NOT NULL,   -- 취소가능금액
    CONSTRAINT PK_PAY_BASE PRIMARY KEY (PAY_NO)
);

COMMENT ON TABLE PAY_BASE IS '결제 기본 정보';
COMMENT ON COLUMN PAY_BASE.PAY_NO IS '결제번호';
COMMENT ON COLUMN PAY_BASE.PAY_TYPE_CODE IS '결제유형코드 (PAY001: 001-결제/002-환불)';
COMMENT ON COLUMN PAY_BASE.PAY_WAY_CODE IS '결제방식코드 (PAY002: 001-신용카드/002-포인트)';
COMMENT ON COLUMN PAY_BASE.PAY_STATUS_CODE IS '결제상태코드 (PAY003: 001-결제대기/002-결제완료/003-결제취소/101-환불접수/102-환불완료/103-환불접수취소)';
COMMENT ON COLUMN PAY_BASE.APPROVE_NO IS '승인번호';
COMMENT ON COLUMN PAY_BASE.ORDER_NO IS '주문번호';
COMMENT ON COLUMN PAY_BASE.CLAIM_NO IS '클레임번호';
COMMENT ON COLUMN PAY_BASE.UPPER_PAY_NO IS '상위결제번호';
COMMENT ON COLUMN PAY_BASE.TRD_NO IS '거래번호';
COMMENT ON COLUMN PAY_BASE.PAY_FINISH_DATE_TIME IS '결제완료일시';
COMMENT ON COLUMN PAY_BASE.MEMBER_NO IS '회원번호';
COMMENT ON COLUMN PAY_BASE.AMOUNT IS '결제금액';
COMMENT ON COLUMN PAY_BASE.CANCELABLE_AMOUNT IS '취소가능금액';

-- =============================================
-- 6. pay_interface_log (결제 인터페이스 로그)
-- =============================================
CREATE TABLE PAY_INTERFACE_LOG (
    PAY_INTERFACE_NO    VARCHAR(15)     NOT NULL,   -- 결제인터페이스번호 (PK, 시퀀스)
    MEMBER_NO           VARCHAR(15)     NOT NULL,   -- 회원번호
    PAY_NO              VARCHAR(15),                -- 결제번호
    PAY_LOG_CODE        VARCHAR(3)      NOT NULL,   -- 로그유형코드 (PAY_004: 001-결제/002-승인/003-망취소)
    REQUEST_JSON        TEXT,                       -- 요청JSON
    RESPONSE_JSON       TEXT,                       -- 응답JSON
    CONSTRAINT PK_PAY_INTERFACE_LOG PRIMARY KEY (PAY_INTERFACE_NO)
);

COMMENT ON TABLE PAY_INTERFACE_LOG IS '결제 인터페이스 로그';
COMMENT ON COLUMN PAY_INTERFACE_LOG.PAY_INTERFACE_NO IS '결제인터페이스번호';
COMMENT ON COLUMN PAY_INTERFACE_LOG.MEMBER_NO IS '회원번호';
COMMENT ON COLUMN PAY_INTERFACE_LOG.PAY_NO IS '결제번호';
COMMENT ON COLUMN PAY_INTERFACE_LOG.PAY_LOG_CODE IS '로그유형코드 (PAY_004: 001-결제/002-승인/003-망취소)';
COMMENT ON COLUMN PAY_INTERFACE_LOG.REQUEST_JSON IS '요청JSON';
COMMENT ON COLUMN PAY_INTERFACE_LOG.RESPONSE_JSON IS '응답JSON';

-- =============================================
-- 7. code_base (공통 코드 정보)
-- =============================================
CREATE TABLE CODE_BASE (
    GROUP_CODE      VARCHAR(6)      NOT NULL,   -- 그룹코드 (PK)
    GROUP_CODE_NAME VARCHAR(15)     NOT NULL,   -- 그룹코드명
    CONSTRAINT PK_CODE_BASE PRIMARY KEY (GROUP_CODE)
);

COMMENT ON TABLE CODE_BASE IS '공통 코드 정보';
COMMENT ON COLUMN CODE_BASE.GROUP_CODE IS '그룹코드';
COMMENT ON COLUMN CODE_BASE.GROUP_CODE_NAME IS '그룹코드명';

-- =============================================
-- 8. code_detail (공통 코드 상세 정보)
-- =============================================
CREATE TABLE CODE_DETAIL (
    GROUP_CODE          VARCHAR(15)     NOT NULL,   -- 그룹코드 (PK)
    CODE                VARCHAR(3)      NOT NULL,   -- 코드 (PK)
    CODE_NAME           VARCHAR(15)     NOT NULL,   -- 코드명
    REFERENCE_VALUE_1   VARCHAR(255),               -- 참조값1
    REFERENCE_VALUE_2   VARCHAR(255),               -- 참조값2
    DISPLAY_SEQUENCE    NUMERIC         NOT NULL DEFAULT 0,   -- 정렬순서
    CONSTRAINT PK_CODE_DETAIL PRIMARY KEY (GROUP_CODE, CODE)
);

COMMENT ON TABLE CODE_DETAIL IS '공통 코드 상세 정보';
COMMENT ON COLUMN CODE_DETAIL.GROUP_CODE IS '그룹코드';
COMMENT ON COLUMN CODE_DETAIL.CODE IS '코드';
COMMENT ON COLUMN CODE_DETAIL.CODE_NAME IS '코드명';
COMMENT ON COLUMN CODE_DETAIL.REFERENCE_VALUE_1 IS '참조값1';
COMMENT ON COLUMN CODE_DETAIL.REFERENCE_VALUE_2 IS '참조값2';
COMMENT ON COLUMN CODE_DETAIL.DISPLAY_SEQUENCE IS '정렬순서';

-- =============================================
-- 9. member_base (회원 정보)
-- =============================================
CREATE TABLE MEMBER_BASE (
    MEMBER_NO           VARCHAR(15)     NOT NULL,   -- 회원번호 (PK, 시퀀스)
    MEMBER_NAME         VARCHAR(50)     NOT NULL,   -- 회원명
    PHONE               VARCHAR(50)     NOT NULL,   -- 전화번호
    EMAIL               VARCHAR(50)     NOT NULL,   -- 이메일
    PASSWORD            VARCHAR(255)    NOT NULL,   -- 비밀번호
    MEMBER_STATUS_CODE  VARCHAR(3)      NOT NULL,   -- 회원상태코드 (MEM001)
    CONSTRAINT PK_MEMBER_BASE PRIMARY KEY (MEMBER_NO)
);

COMMENT ON TABLE MEMBER_BASE IS '회원 정보';
COMMENT ON COLUMN MEMBER_BASE.MEMBER_NO IS '회원번호';
COMMENT ON COLUMN MEMBER_BASE.MEMBER_NAME IS '회원명';
COMMENT ON COLUMN MEMBER_BASE.PHONE IS '전화번호';
COMMENT ON COLUMN MEMBER_BASE.EMAIL IS '이메일';
COMMENT ON COLUMN MEMBER_BASE.PASSWORD IS '비밀번호';
COMMENT ON COLUMN MEMBER_BASE.MEMBER_STATUS_CODE IS '회원상태코드 (MEM001: 001-정상회원/002-탈퇴회원)';

-- =============================================
-- 10. point_history (포인트 기록)
-- =============================================
CREATE TABLE POINT_HISTORY (
    POINT_HISTORY_NO            VARCHAR(15)     NOT NULL,   -- 포인트기록번호 (PK, 시퀀스)
    MEMBER_NO                   VARCHAR(50)     NOT NULL,   -- 회원번호
    AMOUNT                      NUMERIC         NOT NULL,   -- 금액
    POINT_TRANSACTION_CODE      VARCHAR(3)      NOT NULL,   -- 포인트적립사용코드 (MEM002)
    POINT_TRANSACTION_REASON_CODE VARCHAR(3)    NOT NULL,   -- 포인트적립사용사유코드 (MEM003)
    POINT_TRANSACTION_REASON_NO VARCHAR(50),                -- 포인트적립사용번호
    START_DATE_TIME             TIMESTAMP       NOT NULL,   -- 시작일시
    END_DATE_TIME               TIMESTAMP,                  -- 종료일시
    CONSTRAINT PK_POINT_HISTORY PRIMARY KEY (POINT_HISTORY_NO)
);

COMMENT ON TABLE POINT_HISTORY IS '포인트 기록';
COMMENT ON COLUMN POINT_HISTORY.POINT_HISTORY_NO IS '포인트기록번호';
COMMENT ON COLUMN POINT_HISTORY.MEMBER_NO IS '회원번호';
COMMENT ON COLUMN POINT_HISTORY.AMOUNT IS '금액';
COMMENT ON COLUMN POINT_HISTORY.POINT_TRANSACTION_CODE IS '포인트적립사용코드 (MEM002: 001-적립/002-사용)';
COMMENT ON COLUMN POINT_HISTORY.POINT_TRANSACTION_REASON_CODE IS '포인트적립사용사유코드 (MEM003: 001-기타/002-주문)';
COMMENT ON COLUMN POINT_HISTORY.POINT_TRANSACTION_REASON_NO IS '포인트적립사용번호';
COMMENT ON COLUMN POINT_HISTORY.START_DATE_TIME IS '시작일시';
COMMENT ON COLUMN POINT_HISTORY.END_DATE_TIME IS '종료일시';

-- =============================================
-- 11. goods_base (상품 정보)
-- =============================================
CREATE TABLE GOODS_BASE (
    GOODS_NO            VARCHAR(15)     NOT NULL,   -- 상품번호 (PK, G+시퀀스)
    GOODS_NAME          VARCHAR(50)     NOT NULL,   -- 상품명
    GOODS_STATUS_CODE   VARCHAR(3)      NOT NULL,   -- 상품상태코드 (PRD001)
    CONSTRAINT PK_GOODS_BASE PRIMARY KEY (GOODS_NO)
);

COMMENT ON TABLE GOODS_BASE IS '상품 정보';
COMMENT ON COLUMN GOODS_BASE.GOODS_NO IS '상품번호';
COMMENT ON COLUMN GOODS_BASE.GOODS_NAME IS '상품명';
COMMENT ON COLUMN GOODS_BASE.GOODS_STATUS_CODE IS '상품상태코드 (PRD001: 001-판매중/002-판매중단/003-판매중단)';

-- =============================================
-- 12. goods_item (단품 정보)
-- =============================================
CREATE TABLE GOODS_ITEM (
    GOODS_NO            VARCHAR(15)     NOT NULL,   -- 상품번호 (PK)
    ITEM_NO             VARCHAR(3)      NOT NULL,   -- 단품번호 (PK)
    GOODS_NAME          VARCHAR(50)     NOT NULL,   -- 상품명
    ITEM_PRICE          NUMERIC         NOT NULL,   -- 단품금액
    STOCK               NUMERIC         NOT NULL DEFAULT 0,   -- 재고수량
    GOODS_STATUS_CODE   VARCHAR(3)      NOT NULL,   -- 단품상태코드 (PRD001)
    CONSTRAINT PK_GOODS_ITEM PRIMARY KEY (GOODS_NO, ITEM_NO)
);

COMMENT ON TABLE GOODS_ITEM IS '단품 정보';
COMMENT ON COLUMN GOODS_ITEM.GOODS_NO IS '상품번호';
COMMENT ON COLUMN GOODS_ITEM.ITEM_NO IS '단품번호';
COMMENT ON COLUMN GOODS_ITEM.GOODS_NAME IS '상품명';
COMMENT ON COLUMN GOODS_ITEM.ITEM_PRICE IS '단품금액';
COMMENT ON COLUMN GOODS_ITEM.STOCK IS '재고수량';
COMMENT ON COLUMN GOODS_ITEM.GOODS_STATUS_CODE IS '단품상태코드 (PRD001)';

-- =============================================
-- 13. goods_price_hist (상품 가격 정보)
-- =============================================
CREATE TABLE GOODS_PRICE_HIST (
    GOODS_NO            VARCHAR(15)     NOT NULL,   -- 상품번호 (PK)
    START_DATE_TIME     TIMESTAMP       NOT NULL,   -- 시작일 (PK)
    END_DATE_TIME       TIMESTAMP       NOT NULL,   -- 종료일 (PK)
    SALE_PRICE          NUMERIC         NOT NULL,   -- 판매가
    SUPPLY_PRICE        NUMERIC         NOT NULL,   -- 공급원가
    CONSTRAINT PK_GOODS_PRICE_HIST PRIMARY KEY (GOODS_NO, START_DATE_TIME, END_DATE_TIME)
);

COMMENT ON TABLE GOODS_PRICE_HIST IS '상품 가격 정보';
COMMENT ON COLUMN GOODS_PRICE_HIST.GOODS_NO IS '상품번호';
COMMENT ON COLUMN GOODS_PRICE_HIST.START_DATE_TIME IS '시작일';
COMMENT ON COLUMN GOODS_PRICE_HIST.END_DATE_TIME IS '종료일';
COMMENT ON COLUMN GOODS_PRICE_HIST.SALE_PRICE IS '판매가';
COMMENT ON COLUMN GOODS_PRICE_HIST.SUPPLY_PRICE IS '공급원가';

-- =============================================
-- 인덱스 생성
-- =============================================

-- basket_base 인덱스
CREATE INDEX IDX_BASKET_BASE_MEMBER_NO ON BASKET_BASE(MEMBER_NO);
CREATE INDEX IDX_BASKET_BASE_GOODS_NO ON BASKET_BASE(GOODS_NO);

-- order_base 인덱스
CREATE INDEX IDX_ORDER_BASE_MEMBER_NO ON ORDER_BASE(MEMBER_NO);

-- order_detail 인덱스
CREATE INDEX IDX_ORDER_DETAIL_ORDER_NO ON ORDER_DETAIL(ORDER_NO);
CREATE INDEX IDX_ORDER_DETAIL_CLAIM_NO ON ORDER_DETAIL(CLAIM_NO);
CREATE INDEX IDX_ORDER_DETAIL_GOODS_NO ON ORDER_DETAIL(GOODS_NO);
CREATE INDEX IDX_ORDER_DETAIL_ORDER_STATUS ON ORDER_DETAIL(ORDER_STATUS_CODE);

-- order_goods 인덱스
CREATE INDEX IDX_ORDER_GOODS_ORDER_NO ON ORDER_GOODS(ORDER_NO);

-- pay_base 인덱스
CREATE INDEX IDX_PAY_BASE_ORDER_NO ON PAY_BASE(ORDER_NO);
CREATE INDEX IDX_PAY_BASE_MEMBER_NO ON PAY_BASE(MEMBER_NO);
CREATE INDEX IDX_PAY_BASE_APPROVE_NO ON PAY_BASE(APPROVE_NO);
CREATE INDEX IDX_PAY_BASE_PAY_STATUS ON PAY_BASE(PAY_STATUS_CODE);

-- pay_interface_log 인덱스
CREATE INDEX IDX_PAY_INTERFACE_LOG_PAY_NO ON PAY_INTERFACE_LOG(PAY_NO);
CREATE INDEX IDX_PAY_INTERFACE_LOG_MEMBER_NO ON PAY_INTERFACE_LOG(MEMBER_NO);

-- =============================================
-- 시퀀스 사용 예시
-- =============================================

-- 1. basket_no 생성 (단순 시퀀스, 15자리 패딩)
-- 사용 예: LPAD(NEXTVAL('SEQ_BASKET_NO')::TEXT, 15, '0')
-- 결과: 000000000000001, 000000000000002, ...

-- 2. order_no 생성 (날짜 + O + 6자리 시퀀스)
-- 사용 예: TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || 'O' || LPAD(NEXTVAL('SEQ_ORDER_NO')::TEXT, 6, '0')
-- 결과: 20251027O000001, 20251027O000002, ...
-- CYCLE 옵션으로 999999 도달 시 자동으로 1부터 재시작

-- 3. claim_no 생성 (날짜 + C + 6자리 시퀀스)
-- 사용 예: TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || 'C' || LPAD(NEXTVAL('SEQ_CLAIM_NO')::TEXT, 6, '0')
-- 결과: 20251027C000001, 20251027C000002, ...
-- CYCLE 옵션으로 999999 도달 시 자동으로 1부터 재시작

-- 4. pay_no 생성 (단순 시퀀스, 15자리 패딩)
-- 사용 예: LPAD(NEXTVAL('SEQ_PAY_NO')::TEXT, 15, '0')
-- 결과: 000000000000001, 000000000000002, ...

-- 5. pay_interface_no 생성 (단순 시퀀스, 15자리 패딩)
-- 사용 예: LPAD(NEXTVAL('SEQ_PAY_INTERFACE_NO')::TEXT, 15, '0')
-- 결과: 000000000000001, 000000000000002, ...

-- =============================================
-- 시퀀스 조회 및 관리
-- =============================================

-- 현재 시퀀스 값 조회
-- SELECT CURRVAL('SEQ_BASKET_NO');
-- SELECT CURRVAL('SEQ_ORDER_NO');
-- SELECT CURRVAL('SEQ_CLAIM_NO');
-- SELECT CURRVAL('SEQ_PAY_NO');
-- SELECT CURRVAL('SEQ_PAY_INTERFACE_NO');
-- SELECT CURRVAL('SEQ_MEMBER_NO');
-- SELECT CURRVAL('SEQ_POINT_HISTORY_NO');
-- SELECT CURRVAL('SEQ_GOODS_NO');

-- 다음 시퀀스 값 조회
-- SELECT NEXTVAL('SEQ_BASKET_NO');
-- SELECT NEXTVAL('SEQ_ORDER_NO');
-- SELECT NEXTVAL('SEQ_CLAIM_NO');
-- SELECT NEXTVAL('SEQ_PAY_NO');
-- SELECT NEXTVAL('SEQ_PAY_INTERFACE_NO');
-- SELECT NEXTVAL('SEQ_MEMBER_NO');
-- SELECT NEXTVAL('SEQ_POINT_HISTORY_NO');
-- SELECT NEXTVAL('SEQ_GOODS_NO');

-- 시퀀스 수동 초기화 (특별한 경우에만 사용)
-- 일반적으로 초기화 불필요 (CYCLE 옵션으로 자동 순환)
-- ALTER SEQUENCE SEQ_ORDER_NO RESTART WITH 1;
-- ALTER SEQUENCE SEQ_CLAIM_NO RESTART WITH 1;
-- ALTER SEQUENCE SEQ_BASKET_NO RESTART WITH 1;
