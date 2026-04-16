CREATE TABLE school_year_and_semester (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_year INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT uk_school_year_semester UNIQUE (school_year, semester)
);

CREATE INDEX idx_status ON school_year_and_semester(status);

INSERT INTO school_year_and_semester (school_year, semester, status)
VALUES
(2023, '1st', 'ACTIVE'),
(2023, '2nd', 'INACTIVE');