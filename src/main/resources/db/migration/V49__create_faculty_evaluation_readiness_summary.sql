CREATE TABLE IF NOT EXISTS faculty_evaluation_readiness_summary (
    faculty_evaluation_readiness_summary_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id VARCHAR(60) NOT NULL,
    school_year INT NOT NULL,
    semester VARCHAR(10) NOT NULL,
    campus VARCHAR(255),
    subjects TEXT,
    student_evaluation_count BIGINT NOT NULL DEFAULT 0,
    supervisor_evaluation_count BIGINT NOT NULL DEFAULT 0,
    total_score_records BIGINT NOT NULL DEFAULT 0,
    set_average DECIMAL(10, 2),
    sef_average DECIMAL(10, 2),
    overall_average DECIMAL(10, 2),
    last_evaluated_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),

    UNIQUE KEY uq_fers_faculty_term (faculty_id, school_year, semester),
    KEY idx_fers_term_ready (school_year, semester, student_evaluation_count, supervisor_evaluation_count),
    KEY idx_fers_faculty_term (faculty_id, school_year, semester),
    KEY idx_fers_campus (campus)
);

INSERT INTO faculty_evaluation_readiness_summary (
    faculty_id,
    school_year,
    semester,
    campus,
    subjects,
    student_evaluation_count,
    supervisor_evaluation_count,
    total_score_records,
    set_average,
    sef_average,
    overall_average,
    last_evaluated_at,
    created_at,
    updated_at
)
SELECT
    fes.faculty_id,
    fes.school_year,
    CASE
        WHEN UPPER(REPLACE(REPLACE(TRIM(fes.semester), '_', ''), ' ', '')) IN ('1ST', 'FIRST', 'FIRSTSEMESTER') THEN '1st'
        WHEN UPPER(REPLACE(REPLACE(TRIM(fes.semester), '_', ''), ' ', '')) IN ('2ND', 'SECOND', 'SECONDSEMESTER') THEN '2nd'
        WHEN UPPER(REPLACE(REPLACE(TRIM(fes.semester), '_', ''), ' ', '')) IN ('SUMMER', 'SUMMERSEMESTER') THEN 'summer'
        ELSE TRIM(fes.semester)
    END AS semester,
    COALESCE(MAX(pc.campus), MAX(pf.source_campus)) AS campus,
    GROUP_CONCAT(DISTINCT fes.subject_code ORDER BY fes.subject_code SEPARATOR ', ') AS subjects,
    COUNT(DISTINCT CASE
        WHEN fes.evaluation_type IN ('ROLE_STUDENT', 'STUDENT_EVALUATION')
        THEN fes.faculty_evaluation_score_id
    END) AS student_evaluation_count,
    COUNT(DISTINCT CASE
        WHEN fes.evaluation_type IN ('ROLE_PROGRAM_CHAIR', 'SUPERVISOR_EVALUATION')
        THEN fes.faculty_evaluation_score_id
    END) AS supervisor_evaluation_count,
    COUNT(DISTINCT fes.faculty_evaluation_score_id) AS total_score_records,
    ROUND(AVG(CASE
        WHEN fes.evaluation_type IN ('ROLE_STUDENT', 'STUDENT_EVALUATION')
        THEN fes.overall_average_score
    END), 2) AS set_average,
    ROUND(AVG(CASE
        WHEN fes.evaluation_type IN ('ROLE_PROGRAM_CHAIR', 'SUPERVISOR_EVALUATION')
        THEN fes.overall_average_score
    END), 2) AS sef_average,
    ROUND(AVG(fes.overall_average_score), 2) AS overall_average,
    MAX(fes.created_at) AS last_evaluated_at,
    NOW() AS created_at,
    NOW() AS updated_at
FROM faculty_evaluation_score fes
INNER JOIN primary_faculty pf
    ON pf.faculty_id = fes.faculty_id
LEFT JOIN (
    SELECT
        faculty_id,
        class_code,
        subject_code,
        school_year,
        CASE
            WHEN UPPER(REPLACE(REPLACE(TRIM(semester), '_', ''), ' ', '')) IN ('1ST', 'FIRST', 'FIRSTSEMESTER') THEN '1st'
            WHEN UPPER(REPLACE(REPLACE(TRIM(semester), '_', ''), ' ', '')) IN ('2ND', 'SECOND', 'SECONDSEMESTER') THEN '2nd'
            WHEN UPPER(REPLACE(REPLACE(TRIM(semester), '_', ''), ' ', '')) IN ('SUMMER', 'SUMMERSEMESTER') THEN 'summer'
            ELSE TRIM(semester)
        END AS semester,
        MAX(source_campus) AS campus
    FROM primary_class
    GROUP BY
        faculty_id,
        class_code,
        subject_code,
        school_year,
        CASE
            WHEN UPPER(REPLACE(REPLACE(TRIM(semester), '_', ''), ' ', '')) IN ('1ST', 'FIRST', 'FIRSTSEMESTER') THEN '1st'
            WHEN UPPER(REPLACE(REPLACE(TRIM(semester), '_', ''), ' ', '')) IN ('2ND', 'SECOND', 'SECONDSEMESTER') THEN '2nd'
            WHEN UPPER(REPLACE(REPLACE(TRIM(semester), '_', ''), ' ', '')) IN ('SUMMER', 'SUMMERSEMESTER') THEN 'summer'
            ELSE TRIM(semester)
        END
) pc
    ON pc.faculty_id = fes.faculty_id
   AND pc.class_code = fes.class_code
   AND pc.subject_code = fes.subject_code
   AND pc.school_year = fes.school_year
   AND pc.semester = CASE
        WHEN UPPER(REPLACE(REPLACE(TRIM(fes.semester), '_', ''), ' ', '')) IN ('1ST', 'FIRST', 'FIRSTSEMESTER') THEN '1st'
        WHEN UPPER(REPLACE(REPLACE(TRIM(fes.semester), '_', ''), ' ', '')) IN ('2ND', 'SECOND', 'SECONDSEMESTER') THEN '2nd'
        WHEN UPPER(REPLACE(REPLACE(TRIM(fes.semester), '_', ''), ' ', '')) IN ('SUMMER', 'SUMMERSEMESTER') THEN 'summer'
        ELSE TRIM(fes.semester)
   END
WHERE fes.evaluation_type IN ('ROLE_STUDENT', 'STUDENT_EVALUATION', 'ROLE_PROGRAM_CHAIR', 'SUPERVISOR_EVALUATION')
GROUP BY
    fes.faculty_id,
    fes.school_year,
    CASE
        WHEN UPPER(REPLACE(REPLACE(TRIM(fes.semester), '_', ''), ' ', '')) IN ('1ST', 'FIRST', 'FIRSTSEMESTER') THEN '1st'
        WHEN UPPER(REPLACE(REPLACE(TRIM(fes.semester), '_', ''), ' ', '')) IN ('2ND', 'SECOND', 'SECONDSEMESTER') THEN '2nd'
        WHEN UPPER(REPLACE(REPLACE(TRIM(fes.semester), '_', ''), ' ', '')) IN ('SUMMER', 'SUMMERSEMESTER') THEN 'summer'
        ELSE TRIM(fes.semester)
    END
ON DUPLICATE KEY UPDATE
    campus = VALUES(campus),
    subjects = VALUES(subjects),
    student_evaluation_count = VALUES(student_evaluation_count),
    supervisor_evaluation_count = VALUES(supervisor_evaluation_count),
    total_score_records = VALUES(total_score_records),
    set_average = VALUES(set_average),
    sef_average = VALUES(sef_average),
    overall_average = VALUES(overall_average),
    last_evaluated_at = VALUES(last_evaluated_at),
    updated_at = NOW();