-- Align existing primary_class legacy_id to be term-scoped (classCode_schoolYear_semester)
-- so classes can be migrated across different semesters and school years without violating uk_class_legacy.

UPDATE primary_class
SET legacy_id = CONCAT(legacy_id, '_', school_year, '_', semester)
WHERE legacy_id IS NOT NULL
  AND legacy_id NOT LIKE '%\_%'
  AND school_year IS NOT NULL
  AND semester IS NOT NULL;
