package com.faculty_evaluation_backend.fes.config.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            log.info("Executing Flyway repair before migration to align checksums...");
            flyway.repair();
            log.info("Executing Flyway migration...");
            flyway.migrate();
        };
    }
}
