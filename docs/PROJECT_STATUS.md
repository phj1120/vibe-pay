# VibePay 프로젝트 현재 상태 (세션 연속성 문서)

## 🎯 프로젝트 개요
- **목적**: PG 결제 프로세스 학습용 데모 시스템
- **기술스택**: Spring Boot 3.5.5 + Nuxt.js 3.12.2 + PostgreSQL + MyBatis
- **PG사**: 이니시스 (INICIS) 연동 완료
- **결제방식**: 팝업 기반 결제 시스템

## 🏗️ 시스템 아키텍처

### Backend (Spring Boot)
```
com.vibe.pay.backend/
├── common/          # 공통 유틸리티
├── member/          # 회원 관리
├── order/           # 주문 관리
├── payment/         # 결제 처리
├── paymentlog/      # 결제 로그
├── product/         # 상품 관리
└── rewardpoints/    # 적립금 관리
```

### Frontend (Nuxt.js)
```
pages/
├── index.vue        # 메인 페이지
├── members/         # 회원 관리 페이지
├── order/           # 주문 관리 페이지
│   ├── index.vue    # 주문서 작성
│   ├── popup.vue    # 결제 팝업창
│   ├── progress-popup.vue  # 결제 진행 팝업
│   └── complete.vue # 주문 완료 페이지
└── products/        # 상품 관리 페이지
```

## 📊 데이터베이스 스키마

### 핵심 테이블 구조
```sql
-- 주문 관리 (17자리 VARCHAR ID)
CREATE TABLE "order" (
    id VARCHAR(17) PRIMARY KEY,           -- 주문번호: YYYYMMDD + 8자리 시퀀스
    member_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    used_reward_points DOUBLE PRECISION DEFAULT 0.0,
    final_payment_amount DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_id VARCHAR(17)                -- 결제ID (외래키)
);

-- 결제 관리 (17자리 VARCHAR ID)
CREATE TABLE payment (
    id VARCHAR(17) PRIMARY KEY,           -- 결제번호: YYYYMMDDP + 8자리 시퀀스
    member_id BIGINT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    pg_company VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP NOT NULL
);

-- 결제 인터페이스 로그
CREATE TABLE payment_interface_request_log (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(17),               -- 주문번호/결제번호 저장
    request_type VARCHAR(50) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    timestamp TIMESTAMP NOT NULL
);
```

### ID 생성 규칙
- **주문번호**: `YYYYMMDD + 8자리시퀀스` (예: `2025091800000001`)
- **결제번호**: `YYYYMMDDP + 8자리시퀀스` (예: `20250918P0000001`)

## 🔄 결제 프로세스 플로우

### 1. 팝업 기반 결제 시스템
```
주문서 작성 → 결제하기 클릭 → 팝업 열기 → 이니시스 결제창
    ↓
팝업에서 결제 완료 → postMessage로 부모창에 결과 전달 → 팝업 닫기
    ↓
부모창에서 주문 생성 API 호출 → 주문 완료 페이지 이동
```

### 2. API 호출 순서
```javascript
// 1. 결제 파라미터 생성
POST /api/payments/initiate
→ InicisPaymentParameters 반환

// 2. 이니시스 결제창 호출 (팝업)
INIStdPay.pay('inicisForm')

// 3. 결제 완료 후 주문 생성
POST /api/orders
→ Order 생성 및 Payment 승인 처리

// 4. 주문 완료 페이지 이동
/order/complete?orderNumber=${orderNumber}
```

## 🚀 최근 주요 개발 완료 사항

### ✅ 완료된 작업들
1. **순환참조 해결**: PaymentService ↔ OrderService 의존성 제거
2. **ID 체계 통일**: Long → VARCHAR(17) 변경 (주문번호, 결제번호)
3. **팝업 결제 시스템**: SSR 쿠키 공유 이슈 해결
4. **결제 승인 로직 개선**: 로그 파싱 → request 직접 사용
5. **주문 완료 페이지**: 알럿 제거 후 전용 페이지 구현
6. **인코딩 통일**: 모든 파일 UTF-8 적용

### 🔧 핵심 변경사항
- **PaymentService.generatePaymentId()**: 시퀀스 기반 17자리 ID 생성
- **주문번호 생성**: OrderService.generateOrderNumber() 사용
- **결제 확인**: PaymentConfirmRequest에 memberId, paymentMethod 추가
- **DB 스키마**: 모든 ID 필드 VARCHAR(17)로 변경

## 🛠️ 개발 환경 설정

### Backend 실행
```bash
cd vibe-pay-backend
./mvnw spring-boot:run
```

### Frontend 실행
```bash
cd vibe-pay-frontend
npm run dev
```

### 환경변수 (application.properties)
```properties
# 이니시스 설정
inicis.mid=${INICIS_MID}
inicis.signKey=${INICIS_SIGN_KEY}
inicis.returnUrl=http://localhost:3000/order/progress-popup
inicis.closeUrl=http://localhost:3000/order/popup
```

## 🎯 현재 상태 및 다음 작업 가능 항목

### ✅ 정상 동작하는 기능
- 회원/상품 CRUD
- 주문서 작성 (회원선택, 상품선택, 적립금 사용)
- 팝업 기반 이니시스 결제 연동
- 결제 승인 및 주문 생성
- 주문 완료 페이지 표시

### 🔍 개선 가능한 영역
1. **에러 처리**: 현재 console.error → 토스트 알림 또는 에러 페이지
2. **주문 구조화**: process.md의 3번 구조화 요구사항 적용
3. **결제 취소**: PG 승인취소 API 구현
4. **나이스페이 연동**: 이니시스 외 추가 PG사 지원
5. **주문 내역**: 주문 조회 및 관리 페이지
6. **적립금 정책**: 결제 완료 시 적립금 지급 로직

## 📝 개발 스타일 및 협업 방식

### 선호하는 작업 방식
- **간결하고 직접적인 소통**: 불필요한 설명 없이 핵심만
- **실용적인 문제 해결**: 실제 동작하는 코드 우선
- **TodoWrite 활용**: 작업 진행상황 체계적 관리
- **중요 작업 전 확인**: 구조 변경이나 새 기능 개발 시 질문 후 진행

### 코딩 컨벤션
- **인코딩**: UTF-8 통일
- **주석**: 요청 시에만 추가
- **에러 로깅**: console.error 사용 (향후 토스트 알림 고려)
- **타입 안정성**: Long → String 변환 등 타입 일치성 중시

## 🔗 핵심 API 엔드포인트

| 메소드 | 엔드포인트 | 설명 |
|--------|------------|------|
| POST | `/api/payments/initiate` | 결제 파라미터 생성 |
| POST | `/api/payments/confirm` | 결제 승인 처리 |
| POST | `/api/orders` | 주문 생성 |
| GET | `/api/orders/{id}` | 주문 조회 |
| GET | `/api/members` | 회원 목록 |
| GET | `/api/products` | 상품 목록 |

## 🏁 세션 시작 시 체크리스트

1. **환경 확인**: Backend/Frontend 서버 실행 상태
2. **DB 상태**: PostgreSQL 연결 및 스키마 적용 확인
3. **최근 작업**: git log로 최신 커밋 확인
4. **현재 이슈**: 콘솔 에러나 동작 이상 여부 확인
5. **개발 방향**: docs/process.md 재검토