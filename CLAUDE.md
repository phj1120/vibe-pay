# 프로젝트 개발 가이드

## 기술 스택 및 버전

- **Frontend**: Next.js 14.2.x, React 18.3.x
- **Backend**: Spring Boot 3.3.1, Java 21
- **Database**: PostgreSQL
- **ORM**: MyBatis

---

## 프로젝트 구조

### Frontend (FO)
```
fo/
├── src/
│   ├── app/              # Next.js App Router 페이지
│   ├── components/       # React 컴포넌트
│   ├── lib/             # API 클라이언트, 유틸리티
│   └── types/           # TypeScript 타입 정의
```

### Backend (API)
```
api/src/main/java/com/api/app/
├── controller/          # REST API 컨트롤러
├── service/            # 비즈니스 로직
├── repository/         # MyBatis Mapper 인터페이스
├── entity/             # 데이터베이스 엔티티
├── dto/                # 요청/응답 DTO
│   ├── request/
│   └── response/
├── emum/               # 공통 코드 Enum (중요!)
├── common/             # 공통 유틸리티, 보안, 예외 처리
└── aop/                # AOP (시스템 컬럼 자동 세팅 등)
```

### MyBatis Mapper XML
```
api/src/main/resources/mapper/
├── member/             # 회원 관련 쿼리
├── goods/              # 상품 관련 쿼리
├── order/              # 주문 관련 쿼리
├── basket/             # 장바구니 관련 쿼리
└── point/              # 포인트 관련 쿼리
```

---

## 개발 가이드 문서

각 영역별 상세 가이드는 아래 파일을 참조하세요.
**해당 영역의 작업이 처음 언급될 때 한 번만 읽고, 이후에는 처음 읽은 내용을 기반으로 작업합니다.**

### Frontend (FO)
- 경로: `/docs/fo-guide.md`
- Next.js, React 개발 시 반드시 준수

### Backend (API)
- 경로: `/docs/api-guide.md`
- Spring Boot API 개발 시 반드시 준수
- **MyBatis XML 매퍼에서 SQL 작성 시에는 Database 가이드도 함께 참조**

### Database (SQL)
- 경로: `/docs/sql-guide.md`
- PostgreSQL 쿼리 작성 시 반드시 준수
- **MyBatis XML 매퍼의 SQL 작성 시에도 반드시 준수**

---

## 도메인별 상세 스펙 문서 (중요!)

**각 도메인 작업 시 반드시 해당 문서를 먼저 읽고 작업을 시작하세요.**

API 스펙, 비즈니스 로직, 데이터 흐름 등이 상세하게 정의되어 있습니다.

### 회원/포인트
- **회원**: `/docs/domain/member/member.md`
- **포인트**: `/docs/domain/member/point.md`
  - 포인트 적립/사용/취소 API
  - 포인트 내역 조회
  - 포인트 만료 정책

### 상품
- **상품**: `/docs/domain/goods/goods.md`
  - 상품 등록/수정/조회
  - 상품 단품(옵션) 관리
  - 가격 이력 관리

### 주문/결제
- **장바구니**: `/docs/domain/order/basket.md`
  - 장바구니 추가/수정/삭제/조회
- **주문서**: `/docs/domain/order/orderSheet.md`
  - 주문서 조회
- **주문**: `/docs/domain/order/order.md` ⭐ **가장 복잡하고 중요**
  - 주문 프로세스 전체 흐름
  - 결제 연동 (이니시스, 나이스페이)
  - PG사별 요청/응답 스펙
  - 복합 결제 처리 (카드 + 포인트)
  - 주문 검증 로직
  - 망취소 처리
  - 결제 승인 요청/응답

### 공통
- **화면**: `/docs/domain/common/screen.md`
  - 화면별 요구사항 및 기능 명세

---

## 데이터베이스 스키마

### DDL 및 초기화 스크립트
- **DDL**: `/docs/database/ddl.sql`
  - 전체 테이블 스키마 정의
  - 컬럼 설명, 제약조건 포함
- **Drop**: `/docs/database/drop.sql`
  - 테이블 삭제 스크립트

### 주요 테이블 구조
- **회원**: `member_base`, `member_login_history`
- **포인트**: `point_history`
- **상품**: `goods_base`, `goods_item`, `goods_price_hist`
- **장바구니**: `basket_base`
- **주문**: `order_base`, `order_detail`, `order_goods`
- **결제**: `pay_base`, `pay_interface_log`

---

## 공통 코드 (Enum)

**위치**: `@api/src/main/java/com/api/app/emum/`

모든 공통 코드는 Enum으로 관리됩니다. **절대 하드코딩하지 마세요!**

### 주요 공통 코드 Enum

| Enum 파일 | 설명 | 예시                                  |
|-----------|------|-------------------------------------|
| `MEM001.java` | 회원 상태 코드 | 001:활성, 002:휴면, 003:탈퇴              |
| `MEM002.java` | 포인트 거래 구분 코드 | 001:사용, 002:취소                      |
| `MEM003.java` | 포인트 거래 사유 코드 | 001:구매적립, 002:구매사용, etc.            |
| `PRD001.java` | 상품 상태 코드 | 001:판매중, 002:판매중단, 003:품절           |
| `ORD001.java` | 주문 유형 코드 | 001:주문, 002:취소                      |
| `ORD002.java` | 주문 상태 코드 | 001:주문접수, 007:취소완료, etc.            |
| `DLV001.java` | 배송 유형 코드 | 001:일반배송, 002:반품배송                  |
| `PAY001.java` | 결제 로그 코드 | 001:인증요청, 002:승인요청, etc.            |
| `PAY002.java` | 결제 방식 코드 | 001:카드, 002:포인트                     |
| `PAY003.java` | 결제 상태 코드 | 001:결제대기, 002:결제완료, etc.            |
| `PAY004.java` | 결제 구분 코드 | 001: 결제, 002: 승인, 003: 망취소, 004: 취소 |
| `PAY005.java` | PG사 코드 | 001:이니시스, 002:나이스                   |

### Enum 사용 방법
```java
// 코드 값으로 찾기
PAY002 payWay = PAY002.findByCode("001");  // 카드

// 코드 값 가져오기
String code = PAY002.CREDIT_CARD.getCode();  // "001"

// 설명 가져오기
String desc = PAY002.CREDIT_CARD.getDescription();  // "신용카드"

// 참조 값 가져오기 (가중치, 만료일 등 추가 정보)
Integer weight = PAY005.INICIS.getReferenceValue1();  // PG 가중치
```

---

## 작업 방식

### 작업 시작 전 체크리스트

1. **도메인 문서 확인** (가장 중요!)
   - 작업할 도메인의 `/docs/domain/*/` 문서를 먼저 읽기
   - API 스펙, 프로세스, 비즈니스 규칙 파악

2. **공통 코드 확인**
   - 필요한 상태값, 구분값이 `emum/` 에 정의되어 있는지 확인
   - 하드코딩 금지, 반드시 Enum 사용

3. **스키마 확인**
   - `/docs/database/ddl.sql` 에서 관련 테이블 구조 확인
   - 컬럼명, 데이터 타입, 제약조건 파악

4. **가이드 문서 읽기**
   - Frontend 작업: `/docs/fo-guide.md` 읽기 (최초 1회)
   - Backend 작업: `/docs/api-guide.md` 읽기 (최초 1회)
   - Database 작업: `/docs/sql-guide.md` 읽기 (최초 1회)

5. **기존 코드 참조**
   - 유사한 기능이 이미 구현되어 있다면 해당 코드 패턴 참조
   - 예: 주문 기능 → 기존 OrderService 참조

### 작업 순서

1. **Backend 작업**
   - Entity → Mapper Interface → Mapper XML → Service → Controller
   - DTO는 필요한 시점에 작성

2. **Frontend 작업**
   - Types 정의 → API 클라이언트 함수 → 컴포넌트

3. **통합 테스트**
   - Backend: Service 레이어 단위 테스트 작성
   - Frontend: 빌드 확인 (`npm run build`)
   - Backend: 컴파일 확인 (`./gradlew compileJava`)

---

## 주의사항

### 필수 준수 사항

1. **도메인 문서가 최우선**
   - 코드와 문서가 다르면 도메인 문서가 정답
   - 도메인 문서에 정의된 API 스펙, 프로세스 반드시 준수

2. **공통 코드 Enum 사용**
   - "001", "002" 같은 코드값 하드코딩 절대 금지
   - 반드시 `emum/` 패키지의 Enum 사용

3. **가이드 준수**
   - Frontend: `/docs/fo-guide.md`
   - Backend: `/docs/api-guide.md`
   - Database: `/docs/sql-guide.md`
   - **MyBatis XML 매퍼 작성 시**: Java 코드는 API 가이드, SQL 쿼리는 Database 가이드 준수

4. **보안**
   - 민감한 정보(회원번호, 이메일 등)는 토큰에서 추출
   - 클라이언트가 보낸 값을 그대로 신뢰하지 말고 서버에서 검증

5. **예외 처리**
   - 비즈니스 예외는 명확한 메시지와 함께 던지기
   - 도메인 문서에 정의된 검증 로직 반드시 구현

### 우선순위

1. 도메인 문서 (`/docs/domain/*/`)
2. 개발 가이드 (`/docs/*-guide.md`)
3. 기존 코드 패턴
4. 일반적인 Best Practice
