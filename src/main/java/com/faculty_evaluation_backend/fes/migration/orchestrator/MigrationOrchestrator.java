package com.faculty_evaluation_backend.fes.migration.orchestrator;

import com.faculty_evaluation_backend.fes.migration.services.*;
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
    private final SubjectMigrationService subjectMigrationService;
    public void migrateAll(){
        log.info("===START MIGRATION PIPELINE===");
        facultyMigration.migrate();
        classMigration.migrate();
        studentMigration.migrate();
        programMigration.migrate();
        sectionMigration.migrate();
        studentLoadMigration.migrate();
        studentMigration.migrate();
        log.info("===END MIGRATION PIPELINE===");
    }
}
