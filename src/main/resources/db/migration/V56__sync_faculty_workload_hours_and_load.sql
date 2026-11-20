-- ============================================================================
-- V56: Integrate primary_subject lec/lab units and hours into faculty_workload
-- Basis: primary_class mapped to primary_subject by subject_code and faculty_id
-- ============================================================================

-- 1. Update total_hours_per_week on faculty_workload from primary_class + primary_subject
UPDATE faculty_workload fw
INNER JOIN primary_class pc
    ON pc.faculty_id = fw.faculty_id
   AND (
       (fw.class_code IS NOT NULL AND pc.class_code = fw.class_code)
       OR (fw.class_code IS NULL AND pc.subject_code = fw.course_code)
   )
INNER JOIN primary_subject ps
    ON ps.subject_code = pc.subject_code
SET fw.total_hours_per_week = COALESCE(ps.lec_hours, 0.00) + COALESCE(ps.lab_hours, 0.00)
WHERE ps.lec_hours IS NOT NULL OR ps.lab_hours IS NOT NULL;

-- 2. Update total_teaching_load and total_workload from sum of (lec_units + lab_units)
UPDATE faculty_workload fw
INNER JOIN (
    SELECT
        pc.faculty_id,
        pc.school_year,
        pc.semester,
        SUM(COALESCE(ps.lec_units, 0.00) + COALESCE(ps.lab_units, 0.00)) AS total_teaching_units
    FROM primary_class pc
    INNER JOIN primary_subject ps
        ON ps.subject_code = pc.subject_code
    GROUP BY pc.faculty_id, pc.school_year, pc.semester
) t ON t.faculty_id = fw.faculty_id
   AND t.school_year = fw.school_year
   AND (
       LOWER(TRIM(t.semester)) = LOWER(TRIM(fw.semester))
       OR (UPPER(TRIM(fw.semester)) = 'FIRST_SEMESTER' AND LOWER(TRIM(t.semester)) = '1st')
       OR (UPPER(TRIM(fw.semester)) = 'SECOND_SEMESTER' AND LOWER(TRIM(t.semester)) = '2nd')
       OR (UPPER(TRIM(fw.semester)) = 'SUMMER_SEMESTER' AND LOWER(TRIM(t.semester)) = 'summer')
   )
SET fw.total_teaching_load = t.total_teaching_units,
    fw.total_workload = t.total_teaching_units + COALESCE(fw.designation_etu, 0.00),
    fw.overload_hours = GREATEST(0.00, (t.total_teaching_units + COALESCE(fw.designation_etu, 0.00)) - 18.00);
