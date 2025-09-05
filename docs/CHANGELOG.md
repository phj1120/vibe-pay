# VibePay 프로젝트 변경 이력

## [Unreleased] - 2024-12-19

### Added
- 프로젝트 문서화 시스템 구축
- `docs/` 폴더 구조 생성
- 요구사항 명세서 (`REQUIREMENTS.md`)
- API 명세서 (`API.md`)
- 데이터베이스 스키마 문서 (`SCHEMA.md`)
- 프론트엔드 명세서 (`FRONTEND.md`)
- PG사 연동 명세서 (`PG_INTEGRATION.md`)
- 프로젝트 상태 문서 (`STATUS.md`)
- 변경 이력 문서 (`CHANGELOG.md`)

### Changed
- 프로젝트 관리 방식 개선
- 문서 기반 개발 프로세스 도입

### Fixed
- 결제 승인 처리 로직 완전 구현
- 이니시스 API 연동 개선
- 결제-주문 연결 프로세스 수정
- 프론트엔드 결제 콜백 처리 개선
- **CORS 문제 해결 (returnUrl 도메인 불일치)**
- **이니시스 결제 프로세스 완전 구현**
- **이니시스 파라미터 한글 사용 불가 문제 해결**

### Technical Details
- `PaymentConfirmRequest`에 이니시스 승인 필드 추가
- `PaymentService.confirmPayment()` 메서드 개선
- `OrderRequest`에 `paymentId` 필드 추가
- `OrderService.createOrder()` 메서드 수정
- 프론트엔드 결제 프로세스 순서 개선
- **returnUrl을 프론트엔드 도메인으로 변경**
- **Return 페이지 (`/order/return`) 구현**
- **Close 페이지 (`/order/close`) 구현**
- **CORS 설정 추가 (`CorsConfig.java`)**
- **로컬 스토리지를 통한 주문 정보 전달**
- **이니시스 파라미터 한글 허용, 특수문자만 제거**
- **실제 상품명과 회원명 사용**
- **회원 엔티티에 이메일 필드 추가**
- **`sanitizeForInicis()` 메서드 개선**
- **전화번호 전용 정리 메서드 `sanitizeForPhoneNumber()` 추가**
- **이니시스 파라미터 디버깅 로그 추가**

## [0.1.0] - 2024-12-19 (이전)

### Added
- 기본 프로젝트 구조 생성
- 데이터베이스 스키마 설계 및 구현
- 백엔드 API 기본 구조 구현
- 프론트엔드 기본 페이지 구현
- 이니시스 PG사 연동 기본 구현

### Backend
- Spring Boot 프로젝트 초기 설정
- PostgreSQL 데이터베이스 연동
- MyBatis ORM 설정
- 기본 CRUD API 구현
  - MemberController
  - ProductController
  - OrderController
  - PaymentController
  - RewardPointsController
- 이니시스 결제 연동 기본 구현
- 글로벌 예외 처리기 구현

### Frontend
- Nuxt.js 3 프로젝트 초기 설정
- Vuetify 3 UI 라이브러리 적용
- 기본 페이지 구현
  - 홈페이지
  - 회원 관리 페이지
  - 상품 관리 페이지
  - 주문서 페이지
- 이니시스 SDK 연동
- API 프록시 설정

### Database
- PostgreSQL 스키마 생성
- 7개 테이블 구조 설계
- 외래키 관계 설정
- 기본 인덱스 생성

## 변경 이력 작성 규칙

### 형식
```
## [버전] - YYYY-MM-DD

### Added
- 새로운 기능 추가

### Changed
- 기존 기능 변경

### Deprecated
- 곧 제거될 기능

### Removed
- 제거된 기능

### Fixed
- 버그 수정

### Security
- 보안 관련 변경
```

### 버전 규칙
- **Major**: 호환성 없는 API 변경
- **Minor**: 하위 호환성을 유지하는 새 기능
- **Patch**: 하위 호환성을 유지하는 버그 수정

### 작성 시점
- 새로운 기능 구현 완료 시
- 중요한 버그 수정 시
- API 변경 시
- 문서 업데이트 시

### 예시
```markdown
## [0.2.0] - 2024-12-20

### Added
- 나이스페이 PG사 연동 구현
- PG사 비율 기반 선택 로직
- 결제 취소 기능

### Changed
- 결제 프로세스 개선
- 에러 처리 강화

### Fixed
- 이니시스 결제창 호출 오류 수정
- 적립금 계산 로직 버그 수정
```
