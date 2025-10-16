# Vibe Pay System Design Document

## 1. Executive Summary
Vibe Pay 프로젝트는 온라인 결제 및 주문 관리를 위한 시스템입니다. 사용자는 상품을 주문하고 다양한 결제 수단을 통해 결제할 수 있으며, 관리자는 주문 및 결제 내역을 관리할 수 있습니다.
핵심 기능은 사용자 및 상품 관리, 주문 생성 및 관리, 결제 처리, 그리고 리워드 포인트 관리입니다.
기술 스택은 프론트엔드에 Nuxt.js (Vue.js), 백엔드에 Spring Boot (Java), 데이터베이스로 PostgreSQL을 사용합니다.
배포 환경은 명시되지 않았으나, 웹 기반 서비스로 구성됩니다.

## 2. 시스템 아키텍처
### High-Level 아키텍처 다이어그램
```mermaid
graph TD
    User --> Frontend
    Frontend --> Backend API
    Backend API --> Database
    Backend API --> PaymentGateway(Payment Gateway Services)
    PaymentGateway --> Bank
```

### 주요 컴포넌트 역할
*   **Frontend**:
    *   **기술스택**: Nuxt.js (Vue.js 3), TypeScript, Pinia (상태 관리), Axios (API 통신), `@nuxt/ui` (UI 컴포넌트).
    *   **역할**: 사용자 인터페이스 제공, 백엔드 API 호출, 결제 연동 (Inicis, Nicepay 클라이언트 플러그인).
    *   **주요 화면**:
        *   `pages/index.vue`: 메인 페이지
        *   `pages/members/index.vue`, `pages/members/[id].vue`: 회원 목록 및 상세 페이지
        *   `pages/products/index.vue`, `pages/products/[id].vue`: 상품 목록 및 상세 페이지
        *   `pages/order/index.vue`, `pages/order/complete.vue`, `pages/order/failed.vue`, `pages/order/return.vue`, `pages/order/close.vue`, `pages/order/popup.vue`, `pages/order/progress-popup.vue`: 주문 관련 페이지 및 팝업
*   **Backend**:
    *   **기술스택**: Spring Boot (Java), MyBatis (ORM), PostgreSQL.
    *   **역할**: 비즈니스 로직 처리, 데이터베이스 연동, 외부 결제 PG사 연동, API 엔드포인트 제공.
    *   **계층구조**:
        *   `Controller`: HTTP 요청 처리 및 응답 반환.
        *   `Service`: 비즈니스 로직 구현.
        *   `Mapper` (DAO): 데이터베이스 접근 및 ORM 매핑.
        *   `Config`: 애플리케이션 설정 (CORS, WebClient 등).
        *   `Exception`: 커스텀 예외 처리.
        *   `Enums`: 도메인 관련 열거형 정의.
*   **Database**:
    *   **기술스택**: PostgreSQL.
    *   **주요 테이블**: `member`, `product`, `orders`, `order_item`, `payment`, `reward_points`, `point_history`, `payment_interface_request_log`.
*   **External Services**:
    *   **PG사**: Inicis, Nicepay (결제 승인 및 환불 처리).
    *   **인증**: 현재 코드베이스에서 명확한 인증 서비스는 확인되지 않음.

### 컴포넌트 간 상호작용 흐름
1.  사용자가 Frontend를 통해 요청을 보냅니다.
2.  Frontend는 Axios를 사용하여 Backend API에 HTTP 요청을 보냅니다.
3.  Backend API는 요청을 받아 비즈니스 로직을 처리하고, 필요한 경우 Database에 접근하거나 외부 PG사에 요청을 보냅니다.
4.  PG사는 결제 처리 후 Backend에 결과를 통보합니다.
5.  Backend는 처리 결과를 Frontend에 반환하고, Frontend는 사용자에게 결과를 표시합니다.

## 3. 데이터 아키텍처
### 엔티티 관계도 (텍스트)
```
Member (1) -- (1) RewardPoints
Member (1) -- (N) Orders
Member (1) -- (N) Payment
Member (1) -- (N) PointHistory
Orders (1) -- (N) OrderItem
Orders (1) -- (N) Payment (via order_id)
Product (1) -- (N) OrderItem
Payment (1) -- (1) PaymentInterfaceRequestLog (via payment_id)
```

### 주요 엔티티별 역할
*   **Member**: 사용자 정보 (`member_id`, `name`, `shipping_address`, `phone_number`, `email`, `created_at`) 관리.
*   **Product**: 상품 정보 (`product_id`, `name`, `price`) 관리.
*   **Orders**: 주문 정보 (`order_id`, `ord_seq`, `ord_proc_seq`, `claim_id`, `member_id`, `order_date`, `total_amount`, `status`) 관리. `(order_id, ord_seq, ord_proc_seq)`가 복합 기본 키.
*   **OrderItem**: 주문에 포함된 개별 상품 정보 (`order_item_id`, `order_id`, `ord_seq`, `ord_proc_seq`, `product_id`, `quantity`, `price_at_order`) 관리.
*   **Payment**: 결제 정보 (`payment_id`, `member_id`, `order_id`, `claim_id`, `amount`, `payment_method`, `pay_type`, `pg_company`, `status`, `order_status`, `transaction_id`, `payment_date`) 관리. `(payment_id, payment_method, order_id, pay_type)`가 복합 기본 키.
*   **RewardPoints**: 사용자별 리워드 포인트 잔액 (`reward_points_id`, `member_id`, `points`, `last_updated`) 관리.
*   **PointHistory**: 리워드 포인트 적립/사용 내역 (`point_history_id`, `member_id`, `point_amount`, `balance_after`, `transaction_type`, `reference_type`, `reference_id`, `description`, `created_at`) 관리.
*   **PaymentInterfaceRequestLog**: PG사와의 통신 로그 (`log_id`, `payment_id`, `request_type`, `request_payload`, `response_payload`, `timestamp`) 기록.

### 데이터 흐름 (쓰기/읽기)
*   **쓰기**: Frontend 요청 -> Backend Service -> Backend Mapper -> Database (INSERT/UPDATE/DELETE).
*   **읽기**: Frontend 요청 -> Backend Service -> Backend Mapper -> Database (SELECT) -> Backend Service -> Frontend.

## 4. 핵심 프로세스
### 주문 생성 및 결제 처리 흐름
1.  **주문 요청**: 사용자가 Frontend에서 상품을 선택하고 주문 정보를 입력한 후, Backend의 Order API에 `OrderRequest`를 전송합니다. `OrderRequest`에는 주문할 상품 목록과 결제 수단 정보가 포함됩니다.
2.  **결제 승인 요청**: `OrderService`는 `OrderRequest`에 포함된 각 결제 수단에 대해 `PaymentService.confirmPayment()`를 호출합니다. `PaymentService`는 `PaymentProcessorFactory`를 통해 해당 결제 수단에 맞는 `PaymentProcessor`를 선택하고, PG사에 결제 승인을 요청합니다.
3.  **PG사 결제 처리**: `PaymentProcessor`는 `PaymentGatewayAdapter`를 사용하여 실제 PG사 (Inicis, Nicepay 등)와 통신하여 결제를 처리합니다. 이 과정에서 `PaymentInterfaceRequestLog`에 통신 내역이 기록될 수 있습니다.
4.  **결제 성공 시 주문 저장**: 모든 결제 수단에 대한 승인이 성공하면, `OrderService`는 `OrderRequest`의 상품 목록을 기반으로 `orders` 테이블과 `order_item` 테이블에 주문 및 주문 상품 정보를 저장합니다. 이때 `order_id`, `ord_seq`, `ord_proc_seq`를 사용하여 각 상품별 주문과 처리 단계를 관리합니다.
5.  **결제 실패 시 망취소 (Net Cancel)**: 만약 결제 승인 후 주문 정보 저장 과정에서 오류가 발생하면, `OrderService`는 `@Transactional` 어노테이션에 의해 트랜잭션을 롤백하고, 이미 승인된 결제에 대해 `PaymentProcessor.netCancel()`을 호출하여 PG사에 망취소 요청을 보냅니다. 이는 결제는 되었으나 주문이 생성되지 않는 불일치 상태를 방지합니다.
6.  **결과 반환**: 주문 및 결제 처리 결과를 Frontend에 반환하고, Frontend는 사용자에게 결제 완료 또는 실패 화면을 보여줍니다.

### 주문 취소 처리 흐름
1.  **주문 취소 요청**: 사용자가 Frontend에서 특정 `orderId`에 대한 취소를 요청하면, Backend의 Order API에 `cancelOrder()`가 호출됩니다.
2.  **기존 주문 확인**: `OrderService`는 해당 `orderId`의 원본 주문 (`ord_proc_seq = 1`)을 조회하고, 이미 취소된 주문인지 확인합니다.
3.  **클레임 번호 생성**: 새로운 클레임 번호 (`claimId`)를 생성합니다.
4.  **취소 주문 생성**: 원본 주문의 각 상품에 대해 `ord_proc_seq`를 1 증가시키고, `total_amount`와 `quantity`를 음수로 설정하며, `status`를 'CANCELLED'로 하는 새로운 `orders` 및 `order_item` 레코드를 생성하여 저장합니다. 이는 취소 이력을 별도의 레코드로 관리하는 방식입니다.
5.  **결제 환불 처리**: 해당 `orderId`와 관련된 모든 `Payment` 정보를 조회하여 `paymentService.processRefund()`를 호출합니다. `PaymentService`는 `PaymentProcessorFactory`를 통해 해당 결제 수단에 맞는 `PaymentProcessor`를 선택하고, PG사에 환불 요청을 보냅니다.
6.  **포인트 처리**: 환불 과정에서 사용되었던 리워드 포인트가 있다면 `RewardPoints` 및 `PointHistory` 테이블에 적절한 환불 내역이 기록될 수 있습니다.
7.  **결과 반환**: 취소 처리 결과를 Frontend에 반환합니다.

## 5. 기술 스택 및 선택 이유
*   **Frontend**:
    *   **Nuxt.js (Vue.js)**: SSR (Server-Side Rendering) 지원으로 SEO에 유리하며, Vue.js의 반응형 UI 개발 이점을 활용합니다. 구조화된 프로젝트 구성과 개발 편의성을 제공합니다.
    *   **TypeScript**: 코드의 안정성과 유지보수성을 높이고, 개발 과정에서 타입 관련 오류를 줄입니다.
*   **Backend**:
    *   **Spring Boot (Java)**: 빠르고 쉬운 애플리케이션 개발을 가능하게 하며, 강력한 생태계와 높은 확장성을 제공합니다. 안정적이고 성능이 우수하여 대규모 트랜잭션 처리에 적합합니다.
    *   **MyBatis**: SQL을 직접 작성하여 데이터베이스 접근을 세밀하게 제어할 수 있으며, 복잡한 쿼리나 성능 최적화에 유용합니다.
    *   **PostgreSQL**: 강력한 관계형 데이터베이스로, 안정성, 데이터 무결성, 고급 기능 (시퀀스, JSONB 등) 지원이 강점입니다. (참고: `application.yml`에는 MySQL 설정이 되어 있어 `schema.sql`과의 불일치가 존재합니다. `schema.sql`을 기준으로 PostgreSQL로 판단합니다.)
    *   **Factory/Adapter Pattern**: `PaymentGatewayFactory`, `PaymentGatewayAdapter`, `PaymentProcessorFactory`, `PaymentProcessor`를 활용하여 다양한 PG사 및 결제 수단에 대한 유연하고 확장 가능한 아키텍처를 구현했습니다. 이는 새로운 PG사 연동 시 코드 변경을 최소화하는 데 기여합니다.
    *   **PG Weight Selector**: `PgWeightSelector`를 통해 여러 PG사 중 가중치 기반으로 동적으로 PG사를 선택할 수 있는 기능을 제공하여, PG사 장애 대응, 비용 최적화, A/B 테스트 등에 활용될 수 있습니다.
*   **Infrastructure**: (추론) 웹 서비스이므로 웹 서버 (Nginx/Apache), 애플리케이션 서버 (Tomcat 내장), 로드 밸런서 등이 사용될 수 있습니다.
*   **주요 라이브러리/프레임워크**:
    *   Frontend: Pinia (Vue.js 상태 관리), Axios (HTTP 클라이언트).
    *   Backend: Lombok (코드 간결화), Spring Web (RESTful API), Spring JDBC.

## 6. 보안 및 인증
*   **인증 방식**: 현재 프로젝트는 결제 기능에 집중하기 위해 사용자 인증 및 권한 관리 로직을 의도적으로 제외했습니다.
*   **권한 관리**: 위와 동일한 이유로 현재 프로젝트 범위에 포함되지 않습니다.
*   **보안 고려사항**:
    *   `CorsConfig.java`를 통해 CORS (Cross-Origin Resource Sharing) 설정이 되어 있어, 웹 보안의 한 측면을 다루고 있습니다.
    *   PG사 연동을 위한 API 키는 테스트 목적으로 `application.yml`에 직접 관리되고 있습니다. 실제 운영 환경에서는 환경 변수, Secret Management 서비스 등을 통해 안전하게 관리되어야 합니다.
    *   결제 정보와 같은 민감한 데이터는 PG사와의 연동 과정에서 안전하게 처리되어야 합니다. PG사 연동 시 암호화된 통신 (HTTPS) 및 민감 정보의 안전한 저장이 필수적입니다.

## 7. 배포 및 운영
*   **빌드 프로세스**:
    *   **Frontend**: `npm run build` (또는 `yarn build`) 명령어를 통해 Nuxt.js 애플리케이션을 빌드합니다.
    *   **Backend**: Maven (`mvn clean install`)을 사용하여 Spring Boot 애플리케이션을 빌드하고 실행 가능한 JAR 파일을 생성합니다.
*   **배포 구조**: (추론) Frontend는 정적 파일로 빌드되어 웹 서버에 배포되거나, Nuxt.js의 SSR 기능을 활용하여 Node.js 서버에서 실행될 수 있습니다. Backend는 JAR 파일 형태로 서버에 배포되어 JVM 위에서 실행됩니다.
*   **필수 환경 변수**:
    *   `application.yml`에 정의된 데이터베이스 연결 정보 (URL, username, password).
    *   PG사 연동을 위한 API 키, 시크릿 키 등 민감 정보.

## 8. 개선 포인트
*   **아키텍처 개선점**:
    *   **API Gateway 도입**: Frontend와 Backend 사이에 API Gateway를 두어 인증/인가, 로깅, 라우팅, 속도 제한 등의 기능을 중앙에서 관리할 수 있습니다.
    *   **MSA 전환 고려**: 서비스 규모가 커질 경우, 주문, 결제, 회원 등 도메인별로 마이크로서비스 아키텍처로 전환을 고려할 수 있습니다.
    *   **비동기 처리 강화**: 결제와 같이 시간이 오래 걸리거나 외부 서비스에 의존하는 작업은 메시지 큐 (Kafka, RabbitMQ 등)를 활용한 비동기 처리로 시스템 응답성을 높일 수 있습니다.
*   **코드 품질 이슈**:
    *   **데이터베이스 설정 불일치**: `application.yml`에는 MySQL 설정이, `schema.sql`에는 PostgreSQL 문법이 사용되어 데이터베이스 종류에 대한 혼란이 있습니다. 이를 일관성 있게 통일해야 합니다.
    *   **테스트 코드 부족**: 현재 `VibePayBackendApplicationTests.java` 외에 구체적인 단위/통합 테스트 코드가 보이지 않습니다. 핵심 비즈니스 로직에 대한 테스트 코드 작성이 필요합니다.
    *   **DTO/Entity 분리**: `order` 패키지 내 `Order.java`와 `OrderDetailDto.java`, `OrderItemDto.java` 등이 혼재되어 있습니다. 계층별 DTO 분리 및 사용 규칙을 명확히 하여 코드의 응집도를 높이고 결합도를 낮출 수 있습니다.
    *   **복합 기본 키의 복잡성**: `orders` 및 `payment` 테이블의 복합 기본 키는 쿼리 및 유지보수 복잡성을 증가시킬 수 있습니다. 단일 컬럼 기본 키 (예: UUID) 사용을 고려하여 단순화할 수 있습니다.
    *   **주문 취소 로직의 명확성**: 주문 취소를 새로운 레코드 생성 (`ordProcSeq` 증가, 음수 값)으로 처리하는 방식은 이력 관리에 유리하지만, 현재 주문 상태를 파악하거나 집계 쿼리를 작성할 때 복잡성을 증가시킬 수 있습니다. 이 방식의 장단점을 명확히 하고, 필요시 다른 취소 처리 방식 (예: 상태 업데이트 및 취소 이력 테이블 분리)을 고려할 수 있습니다.
    *   **로깅 전략**: `PaymentInterfaceRequestLogMapper.xml`이 존재하지만, 애플리케이션 전반에 걸친 체계적인 로깅 전략 (로그 레벨, 포맷, 저장 위치 등) 수립이 필요합니다.

## 9. 제약사항 및 전제조건
*   **기술적 제약사항**:
    *   Java 17 (pom.xml 기준).
    *   PostgreSQL 데이터베이스 사용 (schema.sql 기준).
    *   Inicis 및 Nicepay PG사 연동에 대한 의존성.
*   **비즈니스 제약사항**:
    *   한국 시장에 특화된 PG사 (Inicis, Nicepay) 사용.
    *   주문, 결제, 회원, 상품, 포인트 등 핵심 도메인에 대한 비즈니스 규칙 준수.
*   **코드에서 발견된 가정들**:
    *   데이터베이스 스키마는 `schema.sql` 및 MyBatis Mapper XML 파일에 정의된 구조를 따릅니다.
    *   PG사와의 통신은 특정 API 규약을 따릅니다.
    *   Frontend와 Backend 간의 API 통신은 RESTful 원칙을 따릅니다.