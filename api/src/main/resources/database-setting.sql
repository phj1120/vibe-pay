-- drop database model_claude_v6;
-- drop schema model_claude_v6;
-- drop user model_claude_v6;


-- 2. 사용자 생성 및 비밀번호 설정
CREATE USER model_claude_v6 PASSWORD 'password';

-- 1. 데이터베이스 생성 (슈퍼유저 권한 필요)
CREATE DATABASE model_claude_v6
    WITH
    OWNER = model_claude_v6   ;
-- 소유자 지정 (필요하면 변경)

-- 3. 데이터베이스 연결 권한 부여
GRANT CONNECT ON DATABASE model_claude_v6 TO model_claude_v6;

-- 4. 데이터베이스 접속 후(예: \c model_claude_v6)
-- 스키마 생성 및 소유권 부여
CREATE SCHEMA model_claude_v6 AUTHORIZATION model_claude_v6;

-- 5. 스키마 내 오브젝트 생성 권한 부여
GRANT USAGE ON SCHEMA model_claude_v6 TO model_claude_v6;
GRANT CREATE ON SCHEMA model_claude_v6 TO model_claude_v6;

-- 6. 기본 권한 부여 (향후 생성되는 테이블, 시퀀스, 함수 권한 자동 부여)
ALTER DEFAULT PRIVILEGES IN SCHEMA model_claude_v6
    GRANT ALL ON TABLES TO model_claude_v6;

ALTER DEFAULT PRIVILEGES IN SCHEMA model_claude_v6
    GRANT ALL ON SEQUENCES TO model_claude_v6;

ALTER DEFAULT PRIVILEGES IN SCHEMA model_claude_v6
    GRANT ALL ON FUNCTIONS TO model_claude_v6;

CREATE SEQUENCE model_claude_v6.SEQ_BASKET_NO
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 999999999999999
    CACHE 20;
