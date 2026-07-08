CREATE INDEX idx_primary_faculty_coverage
    ON primary_faculty(status, college, faculty_id);

CREATE INDEX idx_faculty_workload_coverage
    ON faculty_workload(faculty_id, school_year, semester);
