SELECT DISTINCT subject_code FROM primary_class;

SELECT 
    pf.faculty_id,
    pf.firstname AS `First Name`,
    pf.lastname AS `Last Name`,
    pf.source_campus,

    CASE
        WHEN LOWER(pf.firstname) LIKE '%dummy%'
          OR LOWER(pf.lastname) LIKE '%dummy%'
        THEN 'DUMMY NAME'

        WHEN EXISTS (
            SELECT 1
            FROM primary_faculty pf2
            WHERE pf2.faculty_id <> pf.faculty_id
              AND LOWER(TRIM(pf2.firstname)) = LOWER(TRIM(pf.firstname))
              AND LOWER(TRIM(pf2.lastname)) = LOWER(TRIM(pf.lastname))
        )
        THEN 'DUPLICATE NAME'

        ELSE 'OK'
    END AS remarks

FROM primary_class pc
INNER JOIN primary_faculty pf
    ON pc.faculty_id = pf.faculty_id

GROUP BY 
    pf.faculty_id,
    pf.firstname,
    pf.lastname,
    pf.source_campus;