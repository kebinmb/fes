CREATE TEMPORARY TABLE fers_annex_print_latest AS
SELECT
    faculty_id,
    school_year,
    semester,
    MAX(annex_d_printed_at) AS annex_d_printed_at,
    SUM(COALESCE(annex_d_print_count, 0)) AS annex_d_print_count
FROM faculty_evaluation_report
WHERE annex_d_printed_at IS NOT NULL
GROUP BY faculty_id, school_year, semester;

CREATE TEMPORARY TABLE fers_annex_print_repair AS
SELECT
    latest.faculty_id,
    latest.school_year,
    latest.semester,
    latest.annex_d_printed_at,
    latest.annex_d_print_count,
    source.annex_d_printed_by_user_id,
    source.annex_d_printed_by_username
FROM fers_annex_print_latest latest
INNER JOIN faculty_evaluation_report source
    ON source.faculty_id = latest.faculty_id
   AND source.school_year = latest.school_year
   AND source.semester = latest.semester
   AND source.annex_d_printed_at = latest.annex_d_printed_at;

UPDATE faculty_evaluation_report valid_report
INNER JOIN fers_annex_print_repair repair
    ON repair.faculty_id = valid_report.faculty_id
   AND repair.school_year = valid_report.school_year
   AND repair.semester = valid_report.semester
SET valid_report.annex_d_printed_at = repair.annex_d_printed_at,
    valid_report.annex_d_printed_by_user_id = repair.annex_d_printed_by_user_id,
    valid_report.annex_d_printed_by_username = repair.annex_d_printed_by_username,
    valid_report.annex_d_print_count = GREATEST(
        COALESCE(valid_report.annex_d_print_count, 0),
        COALESCE(repair.annex_d_print_count, 1)
    )
WHERE valid_report.status = 'VALID'
  AND valid_report.annex_d_printed_at IS NULL;

DROP TEMPORARY TABLE fers_annex_print_repair;

DROP TEMPORARY TABLE fers_annex_print_latest;
