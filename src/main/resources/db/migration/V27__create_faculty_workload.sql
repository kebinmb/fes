CREATE TABLE faculty_workload (
                                  faculty_workload_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  faculty_id VARCHAR(255) NOT NULL,
                                  school_year INT NOT NULL,
                                  semester VARCHAR(255) NOT NULL,
                                  total_teaching_load DECIMAL(8, 2),
                                  number_of_preparations INT,
                                  designation_etu DECIMAL(8, 2),
                                  total_workload DECIMAL(8, 2),
                                  overload_hours DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
                                  load_status VARCHAR(30) NOT NULL DEFAULT 'REGULAR_LOAD',
                                  source VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
                                  remarks VARCHAR(1000),
                                  created_at DATETIME(6) NOT NULL,
                                  updated_at DATETIME(6),
                                  created_by VARCHAR(255),
                                  updated_by VARCHAR(255),

                                  CONSTRAINT uk_faculty_workload_term
                                      UNIQUE (faculty_id, school_year, semester)
);

CREATE INDEX idx_faculty_workload_faculty
    ON faculty_workload(faculty_id);

CREATE INDEX idx_faculty_workload_term
    ON faculty_workload(school_year, semester);

CREATE INDEX idx_faculty_workload_status
    ON faculty_workload(load_status);
