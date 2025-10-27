-- 데이터베이스 생성
CREATE DATABASE model_claude_v6;

-- 사용자 생성 (비밀번호를 원하는 값으로 입력)
CREATE USER model_claude_v6 PASSWORD 'password';

-- 데이터베이스에 CONNECT 권한 부여
GRANT CONNECT ON DATABASE model_claude_v6 TO model_claude_v6;

-- 스키마 생성 및 소유권 설정
CREATE SCHEMA model_claude_v6 AUTHORIZATION model_claude_v6;

-- 스키마 권한 부여
GRANT ALL ON SCHEMA model_claude_v6 TO model_claude_v6;

-- 앞으로 생성할 모든 테이블에 model 사용자에게 모든 권한 부여
ALTER DEFAULT PRIVILEGES IN SCHEMA model_claude_v6
    GRANT ALL ON TABLES TO model_claude_v6;

-- 앞으로 생성할 모든 시퀀스에 model 사용자에게 모든 권한 부여
ALTER DEFAULT PRIVILEGES IN SCHEMA model_claude_v6
    GRANT ALL ON SEQUENCES TO model_claude_v6;

-- 앞으로 생성할 모든 함수에 model 사용자에게 모든 권한 부여
ALTER DEFAULT PRIVILEGES IN SCHEMA model_claude_v6
    GRANT ALL ON FUNCTIONS TO model_claude_v6;
