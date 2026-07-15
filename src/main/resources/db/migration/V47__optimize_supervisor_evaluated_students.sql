SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'primary_student'
      AND index_name = 'idx_ps_legacy_student_lookup'
);

SET @statement := IF(
    @index_exists = 0,
    'CREATE INDEX idx_ps_legacy_student_lookup ON primary_student(legacy_database(64), student_id(64))',
    'SELECT 1'
);

PREPARE stmt FROM @statement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'faculty_evaluation_score'
      AND index_name = 'idx_fes_supervisor_evaluated_lookup'
);

SET @statement := IF(
    @index_exists = 0,
    'CREATE INDEX idx_fes_supervisor_evaluated_lookup ON faculty_evaluation_score(school_year, semester, evaluator_id, evaluation_type, created_at DESC, faculty_id)',
    'SELECT 1'
);

PREPARE stmt FROM @statement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
