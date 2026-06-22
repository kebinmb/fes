package com.faculty_evaluation_backend.fes.config.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        basePackages = "com.faculty_evaluation_backend.fes.repositories.sis_authentication",
        entityManagerFactoryRef = "authenticationEntityManagerFactory",
        transactionManagerRef = "authenticationTransactionManager"
)
public class AuthenticationDataSourceConfig {
    @Bean(name = "authenticationDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.authentication")
    public DataSource authenticationDataSource(){
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "authenticationEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean authenticationManagerFactory(
            EntityManagerFactoryBuilder entityManagerFactoryBuilder,
            @Qualifier("authenticationDataSource") DataSource dataSource,
            Environment environment
    ){
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put(
                "hibernate.dialect",
                environment.getProperty(
                        "spring.jpa.properties.hibernate.dialect",
                        "org.hibernate.dialect.MySQLDialect"
                )
        );
        return entityManagerFactoryBuilder
                .dataSource(dataSource)
                .packages("com.faculty_evaluation_backend.fes.entities.sis_authentication")
                .persistenceUnit("authentication")
                .properties(properties)
                .build();
    }
    @Bean(name = "authenticationTransactionManager")
    public PlatformTransactionManager authenticationTransactionManager(
            @Qualifier("authenticationEntityManagerFactory") EntityManagerFactory entityManagerFactory){
        return new JpaTransactionManager(entityManagerFactory);
    }
}
