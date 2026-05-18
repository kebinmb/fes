SELECT 
    pc.faculty_id AS `Faculty ID`,
    MAX(pf.firstname) AS `First Name`,
    MAX(pf.lastname) AS `Last Name`,
    MAX(pc.legacy_database) AS `Source`,

    GROUP_CONCAT(
        DISTINCT pc.subject_code
        ORDER BY pc.subject_code
        SEPARATOR ', '
    ) AS `Subjects`,

    GROUP_CONCAT(
        DISTINCT ps.section_id
        ORDER BY ps.section_id
        SEPARATOR ', '
    ) AS `Section IDs`,

    GROUP_CONCAT(
        DISTINCT ps.program_code
        ORDER BY ps.program_code
        SEPARATOR ', '
    ) AS `Program Codes`,

    COUNT(DISTINCT pc.subject_code) AS `Total Subjects`,
    COUNT(DISTINCT ps.section_id) AS `Total Sections`,
    COUNT(DISTINCT ps.program_code) AS `Total Programs`

FROM primary_class pc

INNER JOIN primary_faculty pf
    ON pc.faculty_id = pf.faculty_id

LEFT JOIN primary_section ps
    ON pc.section_id = ps.section_id

GROUP BY pc.faculty_id

ORDER BY pc.faculty_id;