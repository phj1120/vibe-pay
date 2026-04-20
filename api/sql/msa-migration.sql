-- MSA 분리 마이그레이션 DDL
-- Phase 4~6: outbox, order_status_code, claim_base, claim_saga_state

-- 1. Outbox 테이블 (모든 서비스 공유 - 단일 DB 학습 환경)
CREATE TABLE IF NOT EXISTS outbox (
    id              BIGSERIAL PRIMARY KEY,
    aggregate_type  VARCHAR(50)  NOT NULL,
    aggregate_id    VARCHAR(50)  NOT NULL,
    event_type      VARCHAR(80)  NOT NULL,
    topic           VARCHAR(80)  NOT NULL,
    payload         JSONB        NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count     INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    sent_at         TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_outbox_pending ON outbox (status, id) WHERE status = 'PENDING';

-- 2. order_base 에 주문상태코드 컬럼 추가
ALTER TABLE order_base ADD COLUMN IF NOT EXISTS order_status_code VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- 3. Claim 기본 테이블
CREATE TABLE IF NOT EXISTS claim_base (
    claim_no            VARCHAR(15)  NOT NULL,
    order_no            VARCHAR(15),
    member_no           VARCHAR(15),
    claim_status_code   VARCHAR(20),
    claim_reason        VARCHAR(500),
    complete_date_time  TIMESTAMP,
    regist_id           VARCHAR(15),
    regist_date_time    TIMESTAMP,
    modify_id           VARCHAR(15),
    modify_date_time    TIMESTAMP,
    CONSTRAINT pk_claim_base PRIMARY KEY (claim_no)
);

-- 4. Claim Saga 상태 추적 테이블
CREATE TABLE IF NOT EXISTS claim_saga_state (
    claim_no        VARCHAR(15)  NOT NULL,
    payment_done    BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT pk_claim_saga_state PRIMARY KEY (claim_no)
);
