SET @evaluation_type_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'faculty_evaluation_score'
      AND column_name = 'evaluation_type'
);

SET @evaluation_type_column_sql := IF(
    @evaluation_type_column_exists = 0,
    'ALTER TABLE faculty_evaluation_score ADD COLUMN evaluation_type VARCHAR(255) NULL AFTER overall_interpretation',
    'SELECT 1'
);

PREPARE evaluation_type_column_stmt FROM @evaluation_type_column_sql;
EXECUTE evaluation_type_column_stmt;
DEALLOCATE PREPARE evaluation_type_column_stmt;
