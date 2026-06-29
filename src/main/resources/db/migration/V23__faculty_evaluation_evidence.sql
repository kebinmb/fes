CREATE TABLE faculty_evaluation_evidence (
    faculty_evaluation_evidence_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id VARCHAR(60) NOT NULL,
    class_code VARCHAR(255),
    subject_code VARCHAR(255),
    year_level VARCHAR(50),
    semester VARCHAR(30),
    school_year INT,
    criterion VARCHAR(80) NOT NULL,
    description VARCHAR(1000),
    uploaded_by VARCHAR(100) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(150),
    file_size BIGINT NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_evidence_faculty_context
    ON faculty_evaluation_evidence(faculty_id, school_year, semester);

CREATE INDEX idx_evidence_criterion
    ON faculty_evaluation_evidence(criterion);

CREATE INDEX idx_evidence_uploaded_by
    ON faculty_evaluation_evidence(uploaded_by);
