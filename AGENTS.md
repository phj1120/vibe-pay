# AGENTS.md

Shared context for all AI coding agents (Claude Code, OpenAI Codex, Cursor, Copilot).

## Project

VibePay — 결제 모듈 구현 프로젝트 (Vibe Coding 학습용).
주문, 장바구니, 결제(Inicis/Nice PG), 포인트 적립/사용 도메인을 구현한다.

## Modules

| 경로 | 역할 | 기술 |
|------|------|------|
| `api/` | REST API 서버 | Kotlin 1.9, Spring Boot 3.5, JPA + QueryDSL, PostgreSQL 16 |
| `fo/` | 웹 프론트엔드 | Next.js 16, React 19, TypeScript, Zustand, Zod, Tailwind CSS 4 |
| `docs/` | 도메인/DB 문서 | — |

Spring Batch 모듈은 추후 추가 예정.

## Domain Concepts

| 개념 | 설명 |
|------|------|
| 회원 (Member) | 이메일/비밀번호 로그인, JWT 인증 |
| 상품 (Goods) | 상품(`goods_base`) + 단품(`goods_item`) + 가격이력(`goods_price_hist`) |
| 장바구니 (Basket) | 단품 단위로 담기, 주문 전환 시 `is_order = true` |
| 주문 (Order) | `order_base` + `order_detail` (복합 PK 3개) + `order_goods` |
| 결제 (Payment) | 팝업 PG 연동 (Inicis / Nice), `pay_base` + `pay_interface_log` |
| 포인트 (Point) | `point_history` 누적 방식, 주문 시 사용/적립 |
| 클레임 (Claim) | 취소·반품·교환, `order_detail`의 `upper_order_process_sequence`로 원주문 추적 |

DB 스키마 상세 → `docs/database/schema.md`
도메인 흐름 상세 → `docs/domain/`

## Architecture Rules

### Backend (api/)

- 레이어: `Controller → Service (interface + Impl) → Repository`
- 쓰기 레포지토리는 `repository/rwdb/`, 읽기는 `repository/rodb/` — 절대 교차 사용 금지
- 새 엔티티는 반드시 `SystemEntity` 상속 (`registId`, `modifyId` 자동 처리됨 — AOP로 주입)
- 에러는 `ApiError` enum 코드 체계를 따르고 `ApiException`으로 throw
- 새 public 엔드포인트 추가 시 `SecurityConfig`의 `permitAll()` 목록에 명시적으로 추가

### Frontend (fo/)

- 모든 API 호출은 `lib/api-client.ts`를 통해서만 수행 — `fetch` 직접 사용 금지
- 도메인별 API 함수는 `lib/{domain}-api.ts`에 분리
- 전역 상태는 Zustand store (`store/`), 폼 유효성 검사는 Zod 사용
- 결제는 팝업 방식 — `postMessage` 수신 후 부모 창에서 승인 API 호출

### Database

- 모든 테이블은 `regist_id`, `regist_date_time`, `modify_id`, `modify_date_time` 컬럼 포함
- 컬럼명·테이블명은 snake_case, 도메인 접두사 사용 (예: `basket_base`, `order_detail`)
- DDL 변경 시 `docs/database/ddl.sql` 반드시 동기화

## Testing

- 서비스 레이어는 Mockito 기반 단위 테스트 작성 (`@ExtendWith(MockitoExtension)`)
- 테스트는 Kotlin으로 작성 (`api/src/test/kotlin/`)
- 도메인별 테스트 케이스 시나리오 → `docs/domain/test-case.md` 참고
