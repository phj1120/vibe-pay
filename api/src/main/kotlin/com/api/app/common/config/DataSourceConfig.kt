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
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["com.api.app.repository.rwdb"],
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
class PrimaryDataSourceConfig(
    private val environment: Environment
) {

    @Primary
    @Bean(name = ["primaryDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    fun primaryDataSource(): DataSource = DataSourceBuilder.create().build()

    @Primary
    @Bean(name = ["primaryEntityManagerFactory"])
    fun primaryEntityManagerFactory(
        @Qualifier("primaryDataSource") dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.dataSource = dataSource
        factory.setPackagesToScan("com.api.app.entity")
        factory.jpaVendorAdapter = HibernateJpaVendorAdapter()
        factory.setJpaProperties(hibernateProperties())
        return factory
    }

    @Primary
    @Bean(name = ["primaryTransactionManager"])
    fun primaryTransactionManager(
        @Qualifier("primaryEntityManagerFactory") emf: EntityManagerFactory
    ): PlatformTransactionManager = JpaTransactionManager(emf)

    @Bean(name = ["primaryJpaQueryFactory"])
    fun primaryJpaQueryFactory(
        @Qualifier("primaryEntityManagerFactory") emf: EntityManagerFactory
    ): JPAQueryFactory = JPAQueryFactory(emf.createEntityManager())

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
    entityManagerFactoryRef = "secondaryEntityManagerFactory",
    transactionManagerRef = "secondaryTransactionManager"
)
class SecondaryDataSourceConfig(
    private val environment: Environment
) {

    @Bean(name = ["secondaryDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    fun secondaryDataSource(): DataSource = DataSourceBuilder.create().build()

    @Bean(name = ["secondaryEntityManagerFactory"])
    fun secondaryEntityManagerFactory(
        @Qualifier("secondaryDataSource") dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.dataSource = dataSource
        factory.setPackagesToScan("com.api.app.entity")
        factory.jpaVendorAdapter = HibernateJpaVendorAdapter()
        factory.setJpaProperties(hibernateProperties())
        return factory
    }

    @Bean(name = ["secondaryTransactionManager"])
    fun secondaryTransactionManager(
        @Qualifier("secondaryEntityManagerFactory") emf: EntityManagerFactory
    ): PlatformTransactionManager = JpaTransactionManager(emf)

    @Bean(name = ["secondaryJpaQueryFactory"])
    fun secondaryJpaQueryFactory(
        @Qualifier("secondaryEntityManagerFactory") emf: EntityManagerFactory
    ): JPAQueryFactory = JPAQueryFactory(emf.createEntityManager())

    private fun hibernateProperties() = java.util.Properties().apply {
        setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        setProperty("hibernate.format_sql", "true")
        setProperty(
            "hibernate.hbm2ddl.auto",
            environment.getProperty("spring.jpa.hibernate.ddl-auto", "validate")
        )
    }
}
