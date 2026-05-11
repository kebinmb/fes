CREATE TABLE school_year_and_semester
(

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    school_year INT NOT NULL,

    semester VARCHAR(30) NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_school_year_semester
        UNIQUE (
                school_year,
                semester
            ),

    CONSTRAINT chk_semester
        CHECK (
            semester IN (
                         '1st',
                         '2nd',
                         'summer'
                )
            ),

    CONSTRAINT chk_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'INACTIVE'
                )
            )
);

CREATE INDEX idx_school_year_semester_status
    ON school_year_and_semester(status);

CREATE INDEX idx_school_year_semester
    ON school_year_and_semester(
                                school_year,
                                semester
        );

INSERT INTO school_year_and_semester
(
    school_year,
    semester,
    status
)
VALUES
    (
        2023,
        '1st',
        'ACTIVE'
    ),
    (
        2023,
        '2nd',
        'INACTIVE'
    );