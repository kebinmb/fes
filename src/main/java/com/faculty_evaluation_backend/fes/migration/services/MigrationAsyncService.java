package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.migration.orchestrator.MigrationOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationAsyncService {

    private final MigrationOrchestrator migrationOrchestrator;

    @Async
    public void startMigration() {

        try {

            log.info("=== MIGRATION STARTED ===");

            migrationOrchestrator.migrateAll();

            log.info("=== MIGRATION FINISHED ===");

        } catch (Exception e) {

            log.error(
                    "Migration failed : {}",
                    e.getMessage(),
                    e
            );
        }
    }
}