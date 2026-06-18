package com.api.app.common.config

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.core.env.Environment
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.SharedEntityManagerCreator
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["com.api.app.repository.rwdb"],
    entityManagerFactoryRef = "rwEntityManagerFactory",
    transactionManagerRef = "rwTransactionManager"
)
class RwDataSourceConfig(
    private val environment: Environment
) {

    @Primary
    @Bean(name = ["rwDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.rw")
    fun rwDataSource(): DataSource = DataSourceBuilder.create().build()

    @Primary
    @Bean(name = ["rwEntityManagerFactory"])
    fun rwEntityManagerFactory(
        @Qualifier("rwDataSource") dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.dataSource = dataSource
        factory.setPackagesToScan("com.api.app.entity")
        factory.jpaVendorAdapter = HibernateJpaVendorAdapter()
        factory.setJpaProperties(hibernateProperties())
        return factory
    }

    @Primary
    @Bean(name = ["rwTransactionManager"])
    fun rwTransactionManager(
        @Qualifier("rwEntityManagerFactory") emf: EntityManagerFactory
    ): PlatformTransactionManager = JpaTransactionManager(emf)

    @Primary
    @Bean(name = ["rwJpaQueryFactory"])
    fun rwJpaQueryFactory(
        @Qualifier("rwEntityManagerFactory") emf: EntityManagerFactory
    ): JPAQueryFactory = JPAQueryFactory(SharedEntityManagerCreator.createSharedEntityManager(emf))

    private fun hibernateProperties() = java.util.Properties().apply {
        setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        setProperty("hibernate.format_sql", "true")
        setProperty(
            "hibernate.hbm2ddl.auto",
            environment.getProperty("spring.jpa.hibernate.ddl-auto", "validate")
        )
    }
}

@Configuration
@EnableJpaRepositories(
    basePackages = ["com.api.app.repository.rodb"],
    entityManagerFactoryRef = "roEntityManagerFactory",
    transactionManagerRef = "roTransactionManager"
)
class RoDataSourceConfig {

    @Bean(name = ["roDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.ro")
    fun roDataSource(): DataSource = DataSourceBuilder.create().build()

    @Bean(name = ["roEntityManagerFactory"])
    fun roEntityManagerFactory(
        @Qualifier("roDataSource") dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.dataSource = dataSource
        factory.setPackagesToScan("com.api.app.entity")
        factory.jpaVendorAdapter = HibernateJpaVendorAdapter()
        factory.setJpaProperties(hibernateProperties())
        return factory
    }

    @Bean(name = ["roTransactionManager"])
    fun roTransactionManager(
        @Qualifier("roEntityManagerFactory") emf: EntityManagerFactory
    ): PlatformTransactionManager = JpaTransactionManager(emf)

    @Bean(name = ["roJpaQueryFactory"])
    fun roJpaQueryFactory(
        @Qualifier("roEntityManagerFactory") emf: EntityManagerFactory
    ): JPAQueryFactory = JPAQueryFactory(SharedEntityManagerCreator.createSharedEntityManager(emf))

    private fun hibernateProperties() = java.util.Properties().apply {
        setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        setProperty("hibernate.format_sql", "true")
        setProperty("hibernate.hbm2ddl.auto", "none")
    }
}
