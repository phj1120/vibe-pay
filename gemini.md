# Gemini 프로젝트 개발 가이드

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
| `PAY001.java` | 결제 구분 코드 | 001:인증요청, 002:승인요청, etc.            |
| `PAY002.java` | 결제 방식 코드 | 001:카드, 002:포인트                     |
| `PAY003.java` | 결제 상태 코드 | 001:결제대기, 002:결제완료, etc.            |
| `PAY004.java` | 결제 로그 코드 | 001: 결제, 002: 승인, 003: 망취소, 004: 취소 |
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

## 주의사항 (Gemini)

Gemini는 다음 사항을 엄격히 준수하여 작업을 수행합니다.

### 필수 준수 사항

1.  **도메인 문서가 최우선**
    -   코드와 문서가 다르면 도메인 문서가 정답임을 인지하고, 도메인 문서에 정의된 API 스펙, 프로세스, 비즈니스 규칙을 반드시 준수합니다.

2.  **공통 코드 Enum 사용**
    -   "001", "002" 같은 코드값 하드코딩을 절대 금지하며, 반드시 `emum/` 패키지의 Enum을 사용합니다.

3.  **가이드 준수**
    -   Frontend: `/docs/fo-guide.md`
    -   Backend: `/docs/api-guide.md`
    -   Database: `/docs/sql-guide.md`
    -   **MyBatis XML 매퍼 작성 시**: Java 코드는 API 가이드, SQL 쿼리는 Database 가이드를 준수합니다.

4.  **보안**
    -   민감한 정보(회원번호, 이메일 등)는 토큰에서 추출하며, 클라이언트가 보낸 값을 그대로 신뢰하지 않고 서버에서 검증합니다.

5.  **예외 처리**
    -   비즈니스 예외는 명확한 메시지와 함께 처리하며, 도메인 문서에 정의된 검증 로직을 반드시 구현합니다.

### 우선순위

Gemini는 정보의 우선순위를 다음과 같이 따릅니다.

1.  도메인 문서 (`/docs/domain/*/`)
2.  개발 가이드 (`/docs/*-guide.md`)
3.  기존 코드 패턴
4.  일반적인 Best Practice

