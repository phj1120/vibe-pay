## 회원

필요 기능: 회원 가입, 로그인, 회원정보조회

불필요한 기능: 회원정보수정, 비밀번호 찾기

회원 테이블: member_base

- 회원 가입
    - Request

        | member_name | 회원명 | varchar(50) |  |  |
        | --- | --- | --- | --- | --- |
        | phone | 전화번호 | varchar(50) |  |  |
        | email | 이메일 | varchar(50) |  |  |
        | password | 비밀번호 | varchar(255) |  |  |
    - Response
        - 없음.
        - HTTP 상태 값으로 성공 실패 처리.
    - 프로세스
        - Request 를 기반으로 Entity 를 만들고, 
        member_no 채번, member_status_code: 001(정상회원) 으로 세팅
        - 비밀번호 암호화 하여 저장
        - 회원가입이 불가능할 경우 불가능 사유를 예외로 던지고, 화면에서 해당 정보 노출.
        ex) 아이디 중복 체크.
- 로그인: POST `/api/members/login`
    - Request
  
        | 필드 | 설명 | 타입 |
        | --- | --- | --- |
        | email | 이메일 | varchar(50) |
        | password | 비밀번호 | varchar(255) |
    - Response
  
        | 필드 | 설명 | 타입 |
        | --- | --- | --- |
        | accessToken | 액세스 토큰 | string |
        | refreshToken | 리프레시 토큰 | string |
    - 프로세스
        - Spring Security 기반 JWT 인증 구현
        - 이메일/비밀번호 검증 후 Access Token과 Refresh Token 발급
        - Access Token: 짧은 만료 시간 (30초~1시간)
        - Refresh Token: 긴 만료 시간 (2시간)
        - 토큰은 프론트엔드 localStorage에 저장

- 토큰 갱신: POST `/api/members/refresh`
    - Request
  
        | 필드 | 설명 | 타입 |
        | --- | --- | --- |
        | refreshToken | 리프레시 토큰 | string |
  
    - Response
  
        | 필드 | 설명 | 타입 |
        | --- | --- | --- |
        | accessToken | 새로운 액세스 토큰 | string |
        | refreshToken | 새로운 리프레시 토큰 | string |
    
    - 프로세스
        - Refresh Token 검증 (만료 여부, 유효성)
        - 토큰에서 이메일 추출 및 회원 존재 확인
        - 새로운 Access Token과 Refresh Token 발급
        - Refresh Token도 함께 갱신 (Refresh Token Rotation)

- JWT 인증 처리 (JwtAuthenticationFilter)
    - 모든 API 요청에 대해 Authorization 헤더의 Bearer 토큰 검증
    - 토큰 검증 순서:
        1. 토큰 존재 여부 확인
        2. 토큰 만료 여부 확인 → 만료 시 401 에러 (code: 2003)
        3. 토큰 유효성 검증 → 실패 시 401 에러 (code: 2002)
        4. 검증 성공 시 SecurityContext에 인증 정보 설정
    - 인증 예외 처리:
        - 만료된 토큰: `{"code": "2003", "message": "만료된 토큰입니다"}`
        - 유효하지 않은 토큰: `{"code": "2002", "message": "유효하지 않은 토큰입니다"}`

- 프론트엔드 자동 토큰 갱신 (api-client.ts)
    - 모든 API 요청을 intercept하여 토큰 만료 자동 처리
    - 401 에러 발생 시 자동 갱신 프로세스:
        1. 401 에러 감지 (code: 2003 또는 2002)
        2. `/api/members/refresh` 호출하여 새 토큰 발급
        3. localStorage의 토큰 자동 업데이트
        4. 실패했던 원래 요청을 새 토큰으로 자동 재시도
        5. 사용자는 토큰 만료를 인지하지 못함
    - 동시 요청 처리:
        - 여러 API가 동시에 실패해도 토큰 갱신은 한 번만 수행
        - 나머지 요청들은 큐에서 대기 후 새 토큰으로 재시도
    - 갱신 실패 처리:
        - Refresh Token도 만료된 경우 자동 로그아웃
        - localStorage 토큰 삭제 및 로그인 페이지로 리다이렉트

- Security 설정 (SecurityConfig)
    -- 모든 회원이 이용 가능한 api 를 제외하고는 모두 인증 필요(ex. 로그인, 회원가입, 상품 조회)

- 화면
    - 마이페이지
        - 회원정보조회, 포인트, 주문내역 탭으로 구성
        - 쿼리스트링으로 탭을 받아 해당 탭으로 열릴 수 있도록.
        - 주문 내역 탭 / 주문 구현 시 구현. 요구사항도 주문 쪽에 기입.