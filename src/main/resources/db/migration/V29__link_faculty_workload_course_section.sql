ALTER TABLE faculty_workload
    DROP INDEX uk_faculty_workload_term;

ALTER TABLE faculty_workload
    ADD COLUMN course_code VARCHAR(80) NOT NULL DEFAULT '',
    ADD COLUMN program_code VARCHAR(80) NOT NULL DEFAULT '',
    ADD COLUMN year_level VARCHAR(50) NOT NULL DEFAULT '',
    ADD COLUMN section_code VARCHAR(80) NOT NULL DEFAULT '';

ALTER TABLE faculty_workload
    ADD CONSTRAINT uk_faculty_workload_term
        UNIQUE (
            faculty_id(120),
            school_year,
            semester(40),
            course_code,
            program_code,
            year_level,
            section_code
        );

CREATE INDEX idx_faculty_workload_course_section
    ON faculty_workload(course_code, program_code, year_level, section_code);
