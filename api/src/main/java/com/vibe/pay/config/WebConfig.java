package com.vibe.pay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 웹 설정
 *
 * CORS, Interceptor, Formatter 등 웹 관련 설정을 담당합니다.
 * 현재는 CORS 설정만 포함되어 있습니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CorsConfigProperties corsConfigProperties;

    public WebConfig(CorsConfigProperties corsConfigProperties) {
        this.corsConfigProperties = corsConfigProperties;
    }

    /**
     * CORS 설정
     *
     * Cross-Origin Resource Sharing 정책을 설정합니다.
     * application.yml의 cors 설정 값을 CorsConfigProperties를 통해 주입받아 사용합니다.
     *
     * SecurityConfig의 CORS 설정과 함께 동작하며,
     * Spring Security를 거치지 않는 요청에 대해서도 CORS를 적용합니다.
     *
     * @param registry CORS 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(corsConfigProperties.getAllowedOrigins().toArray(new String[0]))
            .allowedMethods(corsConfigProperties.getAllowedMethods().toArray(new String[0]))
            .allowedHeaders(corsConfigProperties.getAllowedHeaders().toArray(new String[0]))
            .exposedHeaders(corsConfigProperties.getExposedHeaders().toArray(new String[0]))
            .allowCredentials(corsConfigProperties.isAllowCredentials())
            .maxAge(corsConfigProperties.getMaxAge());
    }

    /**
     * 향후 추가 가능한 설정:
     *
     * 1. Interceptor 등록
     * @Override
     * public void addInterceptors(InterceptorRegistry registry) {
     *     registry.addInterceptor(new LoggingInterceptor())
     *         .addPathPatterns("/**")
     *         .excludePathPatterns("/swagger-ui/**", "/api-docs/**");
     * }
     *
     * 2. Formatter 등록
     * @Override
     * public void addFormatters(FormatterRegistry registry) {
     *     registry.addFormatter(new DateFormatter("yyyy-MM-dd"));
     * }
     *
     * 3. Argument Resolver 등록
     * @Override
     * public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
     *     resolvers.add(new CustomArgumentResolver());
     * }
     *
     * 4. Resource Handler 등록 (정적 리소스)
     * @Override
     * public void addResourceHandlers(ResourceHandlerRegistry registry) {
     *     registry.addResourceHandler("/static/**")
     *         .addResourceLocations("classpath:/static/");
     * }
     */
}
