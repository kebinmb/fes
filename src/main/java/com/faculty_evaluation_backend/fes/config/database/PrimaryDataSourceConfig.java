package com.faculty_evaluation_backend.fes.config.database;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
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
@EnableJpaRepositories(
        basePackages = {
                "com.faculty_evaluation_backend.fes.repositories.primary",
                "com.faculty_evaluation_backend.fes.repositories.evaluation",
                "com.faculty_evaluation_backend.fes.repositories.tokens",
                "com.faculty_evaluation_backend.fes.repositories.authentication",
                "com.faculty_evaluation_backend.fes.repositories.data",
                "com.faculty_evaluation_backend.fes.audit"
        },
        entityManagerFactoryRef = "primaryEntityManagerFactory",
        transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDataSourceConfig {
    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource(){
        return DataSourceBuilder.create().build();
    }
    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
      EntityManagerFactoryBuilder entityManagerFactoryBuilder,
      @Qualifier("primaryDataSource") DataSource dataSource,
      Environment environment
    ){
        Map<String, Object> properties = hibernateProperties(environment);
        return entityManagerFactoryBuilder
                .dataSource(dataSource)
                .packages(
                        "com.faculty_evaluation_backend.fes.entities.primary",
                        "com.faculty_evaluation_backend.fes.entities.authentication",
                        "com.faculty_evaluation_backend.fes.entities.evaluation",
                        "com.faculty_evaluation_backend.fes.entities.tokens",
                        "com.faculty_evaluation_backend.fes.entities.data",
                        "com.faculty_evaluation_backend.fes.audit"
                )
                .persistenceUnit("primary")
                .properties(properties)
                .build();
    }

    private Map<String, Object> hibernateProperties(Environment environment) {
        Map<String, Object> properties = new HashMap<>();

        putIfPresent(properties, "hibernate.hbm2ddl.auto", environment, "spring.jpa.hibernate.ddl-auto");
        putIfPresent(properties, "hibernate.dialect", environment, "spring.jpa.properties.hibernate.dialect");
        putIfPresent(properties, "hibernate.format_sql", environment, "spring.jpa.properties.hibernate.format_sql");
        putIfPresent(properties, "hibernate.show_sql", environment, "spring.jpa.show-sql");
        putIfPresent(properties, "hibernate.jdbc.batch_size", environment, "spring.jpa.properties.hibernate.jdbc.batch_size");
        putIfPresent(properties, "hibernate.jdbc.fetch_size", environment, "spring.jpa.properties.hibernate.jdbc.fetch_size");
        putIfPresent(properties, "hibernate.jdbc.batch_versioned_data", environment, "spring.jpa.properties.hibernate.jdbc.batch_versioned_data");
        putIfPresent(properties, "hibernate.order_inserts", environment, "spring.jpa.properties.hibernate.order_inserts");
        putIfPresent(properties, "hibernate.order_updates", environment, "spring.jpa.properties.hibernate.order_updates");
        putIfPresent(properties, "hibernate.generate_statistics", environment, "spring.jpa.properties.hibernate.generate_statistics");
        putIfPresent(properties, "hibernate.boot.allow_jdbc_metadata_access", environment, "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access");
        putIfPresent(properties, "hibernate.temp.use_jdbc_metadata_defaults", environment, "spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults");

        properties.putIfAbsent("hibernate.hbm2ddl.auto", "none");
        properties.putIfAbsent("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.putIfAbsent("hibernate.format_sql", false);

        return properties;
    }

    private void putIfPresent(
            Map<String, Object> properties,
            String hibernateKey,
            Environment environment,
            String propertyKey
    ) {
        String value = environment.getProperty(propertyKey);

        if (value != null) {
            properties.put(hibernateKey, value);
        }
    }

    @Primary
    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory")EntityManagerFactory entityManagerFactory
            ){
        return new JpaTransactionManager(entityManagerFactory);
    }
}
