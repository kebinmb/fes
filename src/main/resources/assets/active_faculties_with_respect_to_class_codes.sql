SELECT 
    pf.faculty_id,
    pf.firstname,
    pf.lastname,
    GROUP_CONCAT(DISTINCT pc.subject_code ORDER BY pc.subject_code SEPARATOR ', ') AS subject_codes
FROM primary_class pc
INNER JOIN primary_faculty pf
    ON pc.faculty_id = pf.faculty_id
GROUP BY 
    pf.faculty_id,
    pf.firstname,
    pf.lastname;
    
SELECT COUNT(DISTINCT pc.faculty_id) AS total_active_faculty
FROM primary_class pc
INNER JOIN primary_faculty pf
    ON pc.faculty_id = pf.faculty_id
WHERE pf.status = 'ACTIVE';