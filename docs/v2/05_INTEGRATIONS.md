# Vibe Pay 개발 사양서: 5. 통합 및 외부 의존성

이 문서는 Vibe Pay 시스템이 외부 서비스 및 라이브러리와 어떻게 통합되고 의존성을 관리하는지 설명한다.

## 1. 외부 API 연동 스펙

Vibe Pay는 다양한 결제 게이트웨이(PG)와의 연동을 통해 결제 기능을 제공한다. PG사별 연동은 `PaymentGatewayAdapter` 인터페이스를 구현하는 어댑터 패턴을 사용하여 추상화되어 있다.

### 1.1. 이니시스 (INICIS)

-   **연동 방식**: 클라이언트(브라우저)에서 PG사 결제창을 팝업으로 호출하고, 서버 간 통신(S2S)으로 최종 승인 및 취소 처리를 수행한다.
-   **주요 API**: `initiate` (클라이언트 파라미터 생성), `confirm` (S2S 승인), `refund` (S2S 취소).
-   **인증/인가**: `signKey`를 이용한 `signature` 생성 (클라이언트 파라미터), `apiKey`를 이용한 S2S 요청 인증.
-   **설정 파라미터 (application.yml)**:
    -   `inicis.mid`: 상점 아이디
    -   `inicis.signKey`: 서명 생성에 사용되는 키 (민감 정보)
    -   `inicis.apiKey`: S2S API 호출에 사용되는 키 (민감 정보)
    -   `inicis.returnUrl`: 결제 완료 후 클라이언트가 리디렉션될 URL
    -   `inicis.closeUrl`: 결제창 닫기 시 클라이언트가 리디렉션될 URL
    -   `inicis.refundUrl`: 환불 요청 API 엔드포인트

### 1.2. 나이스페이 (NICEPAY)

-   **연동 방식**: 이니시스와 유사하게 클라이언트에서 PG사 결제창을 팝업으로 호출하고, 서버 간 통신(S2S)으로 최종 승인 및 취소 처리를 수행한다.
-   **주요 API**: `initiate` (클라이언트 파라미터 생성), `confirm` (S2S 승인), `refund` (S2S 취소).
-   **인증/인가**: `merchantKey`를 이용한 `SignData` 생성 (클라이언트 파라미터).
-   **설정 파라미터 (application.yml)**:
    -   `nicepay.mid`: 상점 아이디
    -   `nicepay.merchantKey`: 서명 생성 및 S2S API 호출에 사용되는 키 (민감 정보)
    -   `nicepay.returnUrl`: 결제 완료 후 클라이언트가 리디렉션될 URL
    -   `nicepay.cancelUrl`: 결제 취소 시 클라이언트가 리디렉션될 URL

### 1.3. 토스페이먼츠 (TOSS Payments)

-   **연동 방식**: `application.yml`에 설정 정보는 존재하나, 현재 코드베이스에서는 명시적인 연동 로직이 구현되어 있지 않다. 향후 확장을 위해 미리 설정만 정의된 상태이다.
-   **설정 파라미터 (application.yml)**:
    -   `toss.clientKey`: 클라이언트 키
    -   `toss.secretKey`: 시크릿 키 (민감 정보)
    -   `toss.successUrl`: 결제 성공 후 리디렉션 URL
    -   `toss.failUrl`: 결제 실패 후 리디렉션 URL

## 2. 인증/인가 메커니즘 (외부 API 연동)

-   **PG사 연동**: 각 PG사에서 제공하는 상점 아이디(MID)와 비밀 키(SignKey, MerchantKey, ApiKey)를 사용하여 API 요청의 유효성을 검증한다.
    -   클라이언트에서 PG 결제창을 호출할 때는 서버에서 생성한 `signature` 또는 `SignData`를 통해 요청의 무결성을 보장한다.
    -   서버 간 통신(S2S) 시에는 PG사에서 제공하는 API 키를 사용하여 서버 자체를 인증한다.
-   **보안**: 모든 민감한 키 정보는 환경변수를 통해 주입받아 소스 코드에 노출되지 않도록 관리한다.

## 3. 서드파티 라이브러리 사용 목적

### 3.1. 백엔드 (Java/Spring Boot)

-   **`org.springframework.boot:spring-boot-starter-web`**: RESTful API 개발을 위한 웹 애플리케이션 프레임워크.
-   **`org.springframework.boot:spring-boot-starter-webflux`**: 비동기, 논블로킹 I/O를 위한 리액티브 스택 웹 프레임워크. 주로 PG사와의 S2S 통신 시 외부 API 호출의 효율성을 높이는 데 사용된다.
-   **`org.mybatis.spring.boot:mybatis-spring-boot-starter`**: SQL Mapper 프레임워크인 MyBatis를 Spring Boot에서 쉽게 사용할 수 있도록 지원한다. 복잡한 SQL 쿼리 제어에 용이하다.
-   **`org.postgresql:postgresql`**: PostgreSQL 데이터베이스 드라이버.
-   **`org.projectlombok:lombok`**: Getter, Setter, 생성자 등 반복적인 자바 코드 작성을 줄여 개발 생산성을 높인다.

### 3.2. 프론트엔드 (Nuxt.js/Vue.js)

-   **`nuxt`**: Vue.js 기반의 범용 애플리케이션 프레임워크. 서버 사이드 렌더링(SSR), 정적 사이트 생성(SSG) 등 다양한 렌더링 모드를 지원하며, 파일 기반 라우팅 등 개발 편의성을 제공한다.
-   **`vuetify-nuxt-module`**: Vue.js를 위한 Material Design 컴포넌트 프레임워크인 Vuetify를 Nuxt.js에서 쉽게 사용할 수 있도록 통합한다. 일관되고 현대적인 UI/UX를 제공한다.
-   **`@mdi/font`**: Material Design Icons 폰트. Vuetify와 함께 사용되어 다양한 아이콘을 제공한다.

## 4. 환경변수 및 설정 관리

-   **Spring Profile**: `application.yml`을 통해 `dev`, `prod`와 같은 Spring Profile을 정의하여 환경별 설정을 분리한다. 이는 개발 환경과 운영 환경에서 다른 데이터베이스 연결 정보나 PG사 키를 사용할 수 있게 한다.
-   **환경변수 주입**: PG사 `mid`, `signKey`, `apiKey`, `merchantKey` 등 민감한 정보는 `application.yml`에서 `${ENV_VAR_NAME}` 형식으로 환경변수로부터 값을 주입받도록 설정되어 있다.
    -   **예시**: `inicis.mid: ${INICIS_MID}`
-   **목적**: 민감 정보를 소스 코드에서 분리하여 보안을 강화하고, 빌드 과정 없이 환경 설정만으로 배포 환경을 변경할 수 있도록 유연성을 제공한다.
