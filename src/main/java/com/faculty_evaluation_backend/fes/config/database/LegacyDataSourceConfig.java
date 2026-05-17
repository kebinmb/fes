package com.faculty_evaluation_backend.fes.config.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.faculty_evaluation_backend.fes.repositories.legacy", entityManagerFactoryRef = "legacyEntityManagerFactory", transactionManagerRef = "legacyTransactionManager")
public class LegacyDataSourceConfig {

    @Bean(name = "legacyTalisayDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.legacy-talisay")
    public DataSource legacyTalisayDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "legacyAlijisDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.legacy-alijis")
    public DataSource legacyAlijisDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "legacyBinalbaganDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.legacy-binalbagan")
    public DataSource legacyBinalbaganDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "legacyFtDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.legacy-ft")
    public DataSource legacyFtDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "legacyDataSource")
    public DataSource legacyRoutingDataSource() {

        Map<Object, Object> targetDataSources = new HashMap<>();

        targetDataSources.put(LegacyDatabase.LEGACY_TALISAY, legacyTalisayDataSource());

        targetDataSources.put(LegacyDatabase.LEGACY_ALIJIS, legacyAlijisDataSource());

        targetDataSources.put(LegacyDatabase.LEGACY_BINALBAGAN, legacyBinalbaganDataSource());

        targetDataSources.put(LegacyDatabase.LEGACY_FT, legacyFtDataSource());

        LegacyRoutingDataSource routingDataSource = new LegacyRoutingDataSource();

        routingDataSource.setTargetDataSources(targetDataSources);

        routingDataSource.setDefaultTargetDataSource(legacyTalisayDataSource());

        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    @Bean(name = "legacyEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean legacyEntityManagerFactory(EntityManagerFactoryBuilder entityManagerFactoryBuilder, @Qualifier("legacyDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();

        properties.put("hibernate.hbm2ddl.auto", "none");

        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        properties.put("hibernate.boot.allow_jdbc_metadata_access", false);

        properties.put("hibernate.temp.use_jdbc_metadata_defaults", false);

        properties.put("hibernate.format_sql", true);
        return entityManagerFactoryBuilder.dataSource(dataSource).packages("com.faculty_evaluation_backend.fes.entities.legacy").persistenceUnit("legacy").properties(properties).build();
    }

    @Bean(name = "legacyTransactionManager")
    public PlatformTransactionManager legacyTransactionManager(@Qualifier("legacyEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}