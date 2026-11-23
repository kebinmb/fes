-- ============================================================================
-- V59: Add subject_type column to primary_subject
-- Stores classification for "LECTURE" and "LABORATORY" (or mixed).
-- Safe / Idempotent: checks information_schema before adding column.
-- ============================================================================

SET @subject_type_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'primary_subject'
      AND column_name = 'subject_type'
);

SET @subject_type_column_sql := IF(
    @subject_type_column_exists = 0,
    'ALTER TABLE primary_subject ADD COLUMN subject_type VARCHAR(50) NULL DEFAULT ''LECTURE'' AFTER lab_hours',
    'SELECT 1'
);

PREPARE subject_type_column_stmt FROM @subject_type_column_sql;
EXECUTE subject_type_column_stmt;
DEALLOCATE PREPARE subject_type_column_stmt;

UPDATE primary_subject
SET subject_type = CASE
    WHEN COALESCE(lab_hours, 0.0) > 0.0 OR COALESCE(lab_units, 0.0) > 0.0 THEN 'LABORATORY'
    ELSE 'LECTURE'
END
WHERE subject_type IS NULL OR subject_type = '';
