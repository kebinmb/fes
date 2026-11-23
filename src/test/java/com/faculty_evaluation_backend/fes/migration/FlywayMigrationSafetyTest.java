package com.faculty_evaluation_backend.fes.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMigrationSafetyTest {

    @Test
    void supervisorEvaluatedStudentsIndexAvoidsInvalidPrefixOnEvaluationType() throws IOException {
        String migration = readClasspathResource(
                "/db/migration/V48__optimize_supervisor_evaluated_students.sql"
        ).toLowerCase();

        assertTrue(migration.contains("idx_fes_supervisor_evaluated_lookup"));
        assertTrue(migration.contains("evaluation_type, created_at desc"));
        assertFalse(migration.contains("evaluation_type("));
    }

    @Test
    void supervisorEvaluatedStudentsMigrationIsDefensive() throws IOException {
        String migration = readClasspathResource(
                "/db/migration/V48__optimize_supervisor_evaluated_students.sql"
        ).toLowerCase();

        assertTrue(migration.contains("information_schema.statistics"));
        assertTrue(migration.contains("index_name = 'idx_ps_legacy_student_lookup'"));
        assertTrue(migration.contains("index_name = 'idx_fes_supervisor_evaluated_lookup'"));
        assertTrue(migration.contains("prepare stmt from @statement"));
    }

    @Test
    void subjectTypeMigrationIsDefensive() throws IOException {
        String migration = readClasspathResource(
                "/db/migration/V59__add_subject_type_to_primary_subject.sql"
        ).toLowerCase();

        assertTrue(migration.contains("information_schema.columns"));
        assertTrue(migration.contains("column_name = 'subject_type'"));
        assertTrue(migration.contains("prepare subject_type_column_stmt from @subject_type_column_sql"));
    }

    private String readClasspathResource(String path) throws IOException {
        try (var inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("Missing classpath resource: " + path);
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
