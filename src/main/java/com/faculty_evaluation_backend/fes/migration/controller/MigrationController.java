package com.faculty_evaluation_backend.fes.migration.controller;

import com.faculty_evaluation_backend.fes.dto.migration.MigrationErrorResponse;
import com.faculty_evaluation_backend.fes.dto.migration.MigrationResponse;
import com.faculty_evaluation_backend.fes.dto.migration.MigrationStatistics;
import com.faculty_evaluation_backend.fes.migration.orchestrator.MigrationOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/migration")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {

    private final MigrationOrchestrator migrationOrchestrator;

    private final AtomicBoolean running =
            new AtomicBoolean(false);

    @PostMapping("/all")
    public MigrationResponse<Void> migrateAll() {

        Instant start = Instant.now();

        if (!running.compareAndSet(false, true)) {

            return buildResponse(
                    "RUNNING",
                    "Migration is already running",
                    start,
                    null,
                    Collections.emptyList()
            );
        }

        startMigration();

        return buildResponse(
                "STARTED",
                "Migration started asynchronously",
                start,
                null,
                Collections.emptyList()
        );
    }

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

        } finally {

            running.set(false);
        }
    }

    private MigrationResponse<Void> buildResponse(
            String status,
            String message,
            Instant start,
            MigrationStatistics statistics,
            List<MigrationErrorResponse> errors
    ) {

        Instant end = Instant.now();

        return MigrationResponse.<Void>builder()
                .status(status)
                .message(message)
                .startTime(start)
                .endTime(end)
                .durationMs(
                        end.toEpochMilli()
                                - start.toEpochMilli()
                )
                .stats(statistics)
                .errors(errors)
                .build();
    }
}