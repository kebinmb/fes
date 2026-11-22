package com.faculty_evaluation_backend.fes.migration.orchestrator;

import com.faculty_evaluation_backend.fes.migration.services.*;
import com.faculty_evaluation_backend.fes.services.data.admin.AdministratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationOrchestrator {
    private final FacultyMigration facultyMigration;
    private final ClassMigration classMigration;
    private final StudentMigration studentMigration;
    private final ProgramMigration programMigration;
    private final SectionMigration sectionMigration;
    private final StudentLoadMigration studentLoadMigration;
    private final SubjectMigrationService subjectMigration;
    private final AdministratorService administratorService;

    public void migrateAll(){
        log.info("===START MIGRATION PIPELINE===");
        programMigration.migrate();
        sectionMigration.migrate();
        subjectMigration.migrate();
        facultyMigration.migrate();
        classMigration.migrate();
        studentMigration.migrate();
        studentLoadMigration.migrate();

        try {
            log.info("===AUTO-POPULATING FACULTY WORKLOADS POST-MIGRATION===");
            administratorService.autoPopulateAndSyncFacultyWorkloads(null, null);
        } catch (Exception e) {
            log.error("Post-migration workload auto-population failed: {}", e.getMessage(), e);
        }

        log.info("===END MIGRATION PIPELINE===");
    }
}
