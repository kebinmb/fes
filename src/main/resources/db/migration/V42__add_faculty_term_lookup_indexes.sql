CREATE INDEX idx_pc_faculty_term_assignments
    ON primary_class(
        faculty_id(80),
        school_year,
        semester(16),
        subject_code(64),
        section_id,
        class_code(96)
    );

CREATE INDEX idx_fes_faculty_term_report
    ON faculty_evaluation_score(
        faculty_id,
        school_year,
        semester,
        class_code,
        evaluator_id
    );
