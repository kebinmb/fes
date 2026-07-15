/*
 * Optional production read indexes.
 *
 * Do not run this through Flyway application startup on MySQL 5.5. These
 * ALTER TABLE statements can lock and copy large tables for several minutes.
 * Run them manually during a maintenance window after checking that each index
 * does not already exist.
 */

ALTER TABLE faculty_evaluation_score
    ADD INDEX idx_fes_term_type_faculty_created (
        school_year,
        semester,
        evaluation_type,
        faculty_id,
        created_at
    );

ALTER TABLE faculty_workload
    ADD INDEX idx_fw_faculty_term_status_class (
        faculty_id(120),
        school_year,
        semester(40),
        load_status,
        class_code
    );

ALTER TABLE audit_logs
    ADD INDEX idx_audit_logs_created_id (created_at, audit_id);
