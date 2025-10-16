package com.vibe.pay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정
 *
 * 현재는 결제 기능 구현에 집중하기 위해 인증/인가 기능을 비활성화합니다.
 * 모든 API 엔드포인트에 대한 접근을 허용합니다.
 *
 * 향후 요구사항:
 * - JWT 기반 인증 구현
 * - 역할 기반 권한 관리 (ROLE_USER, ROLE_ADMIN)
 * - API 별 접근 권한 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Spring Security 필터 체인 설정
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호 비활성화 (REST API이므로)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS 설정 활성화
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 세션 사용 안 함 (Stateless)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 인증/인가 설정 - 현재는 모든 요청 허용
            .authorizeHttpRequests(auth -> auth
                // Swagger UI 접근 허용
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // 모든 API 엔드포인트 접근 허용 (인증 불필요)
                .anyRequest().permitAll()
            )

            // Form 로그인 비활성화
            .formLogin(AbstractHttpConfigurer::disable)

            // HTTP Basic 인증 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)

            // 로그아웃 비활성화
            .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * CORS 설정
     *
     * application.yml의 cors 설정을 참조하여 동적으로 구성할 수도 있지만,
     * 명시적인 설정을 위해 코드로 관리합니다.
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 설정
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*"
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));

        // 자격증명 허용 (쿠키, 인증 헤더 등)
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
