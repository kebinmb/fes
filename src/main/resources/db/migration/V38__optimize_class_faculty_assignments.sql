CREATE INDEX idx_primary_class_admin_assignments
    ON primary_class(
        school_year,
        semester(16),
        legacy_database(64),
        subject_code(64),
        class_code(96)
    );

CREATE INDEX idx_primary_faculty_assignment_options
    ON primary_faculty(
        status,
        legacy_database(64),
        lastname(80),
        firstname(80),
        faculty_id(80)
    );
