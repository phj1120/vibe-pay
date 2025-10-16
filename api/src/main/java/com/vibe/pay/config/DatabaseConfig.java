package com.vibe.pay.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 데이터베이스 설정
 *
 * 데이터 소스와 트랜잭션 매니저를 설정합니다.
 * HikariCP를 사용하여 커넥션 풀을 관리합니다.
 *
 * 현재는 단일 데이터베이스를 사용하지만,
 * 향후 Read/Write 분리 또는 다중 데이터베이스 환경으로 확장할 수 있습니다.
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    /**
     * HikariCP 설정
     *
     * application.yml의 spring.datasource.hikari 설정을 바인딩합니다.
     *
     * @return HikariConfig
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    /**
     * 데이터 소스 설정
     *
     * HikariCP를 사용한 커넥션 풀을 설정합니다.
     * application.yml의 spring.datasource 설정을 사용합니다.
     *
     * HikariCP 주요 설정:
     * - maximum-pool-size: 최대 커넥션 수
     * - minimum-idle: 최소 유휴 커넥션 수
     * - connection-timeout: 커넥션 획득 타임아웃 (ms)
     * - idle-timeout: 유휴 커넥션 유지 시간 (ms)
     * - max-lifetime: 커넥션 최대 수명 (ms)
     * - auto-commit: 자동 커밋 여부
     *
     * @return DataSource
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig());
    }

    /**
     * 트랜잭션 매니저 설정
     *
     * Spring의 @Transactional 어노테이션이 동작하기 위해 필요합니다.
     * MyBatis와 함께 사용됩니다.
     *
     * 트랜잭션 전파 레벨:
     * - REQUIRED: 진행 중인 트랜잭션이 있으면 참여, 없으면 새로 시작 (기본값)
     * - REQUIRES_NEW: 항상 새 트랜잭션 시작
     * - SUPPORTS: 진행 중인 트랜잭션이 있으면 참여, 없어도 무관
     * - NOT_SUPPORTED: 트랜잭션 없이 실행
     * - MANDATORY: 진행 중인 트랜잭션 필수
     * - NEVER: 트랜잭션 없이만 실행 가능
     * - NESTED: 중첩 트랜잭션 (savepoint 사용)
     *
     * @param dataSource 데이터 소스
     * @return PlatformTransactionManager
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 향후 Read/Write 분리 시 추가할 설정:
     *
     * 1. Read 전용 데이터 소스
     * @Bean
     * @ConfigurationProperties(prefix = "spring.datasource.read")
     * public DataSource readDataSource() {
     *     return new HikariDataSource(readHikariConfig());
     * }
     *
     * 2. Write 전용 데이터 소스
     * @Bean
     * @ConfigurationProperties(prefix = "spring.datasource.write")
     * public DataSource writeDataSource() {
     *     return new HikariDataSource(writeHikariConfig());
     * }
     *
     * 3. 라우팅 데이터 소스
     * @Bean
     * public DataSource routingDataSource(
     *     @Qualifier("writeDataSource") DataSource writeDataSource,
     *     @Qualifier("readDataSource") DataSource readDataSource
     * ) {
     *     RoutingDataSource routingDataSource = new RoutingDataSource();
     *     Map<Object, Object> dataSourceMap = new HashMap<>();
     *     dataSourceMap.put("write", writeDataSource);
     *     dataSourceMap.put("read", readDataSource);
     *     routingDataSource.setTargetDataSources(dataSourceMap);
     *     routingDataSource.setDefaultTargetDataSource(writeDataSource);
     *     return routingDataSource;
     * }
     *
     * 4. Read 전용 트랜잭션 매니저
     * @Bean
     * public PlatformTransactionManager readTransactionManager(
     *     @Qualifier("readDataSource") DataSource dataSource
     * ) {
     *     return new DataSourceTransactionManager(dataSource);
     * }
     */
}
