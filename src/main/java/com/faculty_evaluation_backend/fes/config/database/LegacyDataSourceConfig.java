package com.faculty_evaluation_backend.fes.config.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.Query;
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
        basePackages = "com.faculty_evaluation_backend.fes.repositories.legacy",
        entityManagerFactoryRef = "legacyEntityManagerFactory",
        transactionManagerRef = "legacyTransactionManager"
)
public class LegacyDataSourceConfig {
    @Bean(name = "legacyDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.legacy")
    public DataSource legactDataSource(){
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "legacyEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean legacyEntityManagerFactory(
            EntityManagerFactoryBuilder entityManagerFactoryBuilder,
            @Qualifier("legacyDataSource") DataSource dataSource
    ){
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        return entityManagerFactoryBuilder
                .dataSource(dataSource)
                .packages("com.faculty_evaluation_backend.fes.entities.legacy")
                .persistenceUnit("legacy")
                .properties(properties)
                .build();
    }
    @Bean(name = "legacyTransactionManager")
    public PlatformTransactionManager legacyTransactionManager(
            @Qualifier("legacyEntityManagerFactory")EntityManagerFactory entityManagerFactory){
                return new JpaTransactionManager(entityManagerFactory);
    }

}
