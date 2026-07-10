CREATE INDEX idx_primary_faculty_admin_list
    ON primary_faculty(
        college,
        legacy_database(64),
        lastname(80),
        firstname(80),
        faculty_id(80)
    );
