package com.vibe.pay.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 설정
 *
 * SpringDoc OpenAPI 3.0을 사용한 API 문서화 설정입니다.
 * Swagger UI를 통해 API 명세를 확인하고 테스트할 수 있습니다.
 *
 * 접근 URL:
 * - Swagger UI: http://localhost:8080/api/swagger-ui.html
 * - API Docs: http://localhost:8080/api/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private int serverPort;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * OpenAPI 정보 설정
     *
     * API 문서의 메타데이터를 정의합니다.
     *
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort + contextPath)
                    .description("로컬 개발 서버"),
                new Server()
                    .url("https://dev-api.vibepay.com" + contextPath)
                    .description("개발 서버"),
                new Server()
                    .url("https://api.vibepay.com" + contextPath)
                    .description("운영 서버")
            ));
    }

    /**
     * API 정보
     *
     * @return Info
     */
    private Info apiInfo() {
        return new Info()
            .title("Vibe Pay API")
            .description(
                "Vibe Pay 결제 시스템 API 문서입니다.\n\n" +
                "## 주요 기능\n" +
                "- 회원 관리\n" +
                "- 상품 관리\n" +
                "- 주문 생성 및 관리\n" +
                "- 결제 처리 (Inicis, Nicepay)\n" +
                "- 리워드 포인트 관리\n\n" +
                "## 인증\n" +
                "현재는 인증 없이 모든 API를 사용할 수 있습니다.\n" +
                "향후 JWT 토큰 기반 인증이 추가될 예정입니다.\n\n" +
                "## 응답 형식\n" +
                "모든 API는 다음과 같은 공통 응답 형식을 사용합니다:\n" +
                "```json\n" +
                "{\n" +
                "  \"success\": true,\n" +
                "  \"message\": \"성공\",\n" +
                "  \"data\": { /* 실제 데이터 */ }\n" +
                "}\n" +
                "```"
            )
            .version("1.0.0")
            .contact(
                new Contact()
                    .name("Vibe Pay Development Team")
                    .email("dev@vibepay.com")
                    .url("https://vibepay.com")
            )
            .license(
                new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")
            );
    }
}
