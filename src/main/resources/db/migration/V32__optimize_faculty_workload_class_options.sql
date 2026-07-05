CREATE INDEX idx_primary_class_faculty_term_section_subject
    ON primary_class(
        faculty_id(120),
        school_year,
        semester(40),
        section_id,
        subject_code(80),
        class_code(120)
    );
