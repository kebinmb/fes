CREATE INDEX idx_fes_term_type_faculty_created
    ON faculty_evaluation_score(
        school_year,
        semester,
        evaluation_type,
        faculty_id,
        created_at
    );

CREATE INDEX idx_fw_faculty_term_status_class
    ON faculty_workload(
        faculty_id(120),
        school_year,
        semester(40),
        load_status,
        class_code
    );

CREATE INDEX idx_audit_logs_created_id
    ON audit_logs(created_at, audit_id);
