package com.vibe.pay.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 설정
 *
 * MyBatis와 Spring Boot를 연동하고 Mapper 인터페이스를 스캔합니다.
 * Mapper XML 파일의 위치와 타입 별칭을 설정합니다.
 */
@Configuration
@MapperScan(
    basePackages = {
        "com.vibe.pay.domain.*.repository"
    },
    sqlSessionFactoryRef = "sqlSessionFactory"
)
public class MyBatisConfig {

    /**
     * SqlSessionFactory 빈 생성
     *
     * MyBatis의 핵심 컴포넌트로, SQL 세션을 생성하고 관리합니다.
     * Mapper XML 파일의 위치와 설정을 지정합니다.
     *
     * @param dataSource 데이터 소스
     * @return SqlSessionFactory
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // Mapper XML 파일 위치 설정
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(
            resolver.getResources("classpath:mapper/**/*.xml")
        );

        // MyBatis 설정
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();

        // 언더스코어를 카멜 케이스로 자동 매핑 (user_name -> userName)
        configuration.setMapUnderscoreToCamelCase(true);

        // NULL 값을 처리할 때 JDBC 타입 지정
        configuration.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);

        // NULL 값에 대해서도 setter 호출
        configuration.setCallSettersOnNulls(true);

        // 캐시 비활성화 (개발 단계에서는 비활성화 권장)
        configuration.setCacheEnabled(false);

        // 기본 Fetch 크기
        configuration.setDefaultFetchSize(100);

        // 기본 Statement 타임아웃 (초)
        configuration.setDefaultStatementTimeout(30);

        sessionFactory.setConfiguration(configuration);

        // 타입 별칭 패키지 설정
        sessionFactory.setTypeAliasesPackage(
            "com.vibe.pay.domain.*.entity," +
            "com.vibe.pay.domain.*.dto"
        );

        return sessionFactory.getObject();
    }
}
