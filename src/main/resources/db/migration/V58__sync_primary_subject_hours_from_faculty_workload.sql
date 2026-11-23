-- ============================================================================
-- V58: Synchronize primary_subject hours and units from recent faculty_workload
-- Populates missing/null lec_units, lab_units, lec_hours, and lab_hours in primary_subject
-- using the actively encoded total_hours_per_week from faculty_workload.
-- ============================================================================

UPDATE primary_subject ps
INNER JOIN (
    SELECT 
        fw.course_code,
        MAX(fw.total_hours_per_week) AS best_hours
    FROM faculty_workload fw
    WHERE fw.course_code IS NOT NULL 
      AND TRIM(fw.course_code) != '' 
      AND fw.total_hours_per_week > 0
    GROUP BY fw.course_code
) t ON t.course_code = ps.subject_code
SET 
    ps.lec_units = CASE 
        WHEN t.best_hours = 3.0 THEN 3.0
        WHEN t.best_hours = 2.0 THEN 2.0
        WHEN t.best_hours = 1.0 THEN 1.0
        WHEN t.best_hours = 4.0 THEN 4.0
        WHEN t.best_hours IN (4.25, 4.5) THEN 2.0
        WHEN t.best_hours = 5.0 THEN 2.0
        WHEN t.best_hours IN (3.25, 3.5) THEN 1.0
        WHEN t.best_hours IN (5.25, 5.5) THEN 3.0
        WHEN t.best_hours >= 6.0 AND t.best_hours < 7.0 THEN 6.0
        WHEN t.best_hours >= 7.0 AND t.best_hours <= 8.5 THEN 3.0
        ELSE t.best_hours
    END,
    ps.lab_units = CASE 
        WHEN t.best_hours IN (4.25, 4.5, 5.0, 3.25, 3.5, 5.25, 5.5) THEN 1.0
        WHEN t.best_hours >= 7.0 AND t.best_hours <= 8.5 THEN 2.0
        ELSE 0.0
    END,
    ps.lec_hours = CASE 
        WHEN t.best_hours = 3.0 THEN 3.0
        WHEN t.best_hours = 2.0 THEN 2.0
        WHEN t.best_hours = 1.0 THEN 1.0
        WHEN t.best_hours = 4.0 THEN 4.0
        WHEN t.best_hours IN (4.25, 4.5) THEN ROUND(t.best_hours - 3.0, 2)
        WHEN t.best_hours = 5.0 THEN 2.0
        WHEN t.best_hours IN (3.25, 3.5) THEN ROUND(t.best_hours - 2.0, 2)
        WHEN t.best_hours IN (5.25, 5.5) THEN ROUND(t.best_hours - 3.0, 2)
        WHEN t.best_hours >= 6.0 AND t.best_hours < 7.0 THEN t.best_hours
        WHEN t.best_hours >= 7.0 AND t.best_hours <= 8.5 THEN ROUND(t.best_hours - 4.5, 2)
        ELSE t.best_hours
    END,
    ps.lab_hours = CASE 
        WHEN t.best_hours IN (4.25, 4.5, 5.0, 5.25, 5.5) THEN 3.0
        WHEN t.best_hours IN (3.25, 3.5) THEN 2.0
        WHEN t.best_hours >= 7.0 AND t.best_hours <= 8.5 THEN 4.5
        ELSE 0.0
    END
WHERE (ps.lec_hours IS NULL OR ps.lec_hours = 0) 
  AND (ps.lab_hours IS NULL OR ps.lab_hours = 0);
