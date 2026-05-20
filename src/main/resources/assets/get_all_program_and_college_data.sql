SELECT 
    pf.faculty_id,
    pf.firstname,
    pf.lastname,
    pf.college,
    GROUP_CONCAT(DISTINCT pc.subject_code ORDER BY pc.subject_code) AS subject_codes
FROM primary_class pc
INNER JOIN primary_section ps 
    ON pc.section_id = ps.section_id
INNER JOIN primary_faculty pf 
    ON pc.faculty_id = pf.faculty_id
WHERE ps.program_code LIKE '%BS IND TECH%'
  AND pf.college = 'CCS'
GROUP BY
    pf.faculty_id,
    pf.firstname,
    pf.lastname,
    pf.college;