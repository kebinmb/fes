ALTER TABLE faculty_workload
    ADD COLUMN class_code VARCHAR(120) NULL AFTER semester;

ALTER TABLE faculty_workload
    DROP INDEX uk_faculty_workload_term;

ALTER TABLE faculty_workload
    ADD CONSTRAINT uk_faculty_workload_term
        UNIQUE (
            faculty_id(120),
            school_year,
            semester(40),
            class_code,
            course_code,
            program_code,
            year_level,
            section_code
        );

CREATE INDEX idx_faculty_workload_class_code
    ON faculty_workload(class_code);
