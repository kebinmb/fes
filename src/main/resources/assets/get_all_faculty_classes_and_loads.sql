SELECT
    pc.subject_code AS subjectCode,
    pc.faculty_id AS facultyId,
    pc.school_year AS schoolYear,
    pc.semester AS semester,

    pf.college AS college,
    ps.program_code AS programCode,

    pf.firstname AS firstname,
    pf.lastname AS lastname,

    GROUP_CONCAT(DISTINCT pc.class_code ORDER BY pc.class_code) AS classCodes,

    GROUP_CONCAT(DISTINCT psl.yearLevels ORDER BY psl.yearLevels) AS yearLevels,

    GROUP_CONCAT(DISTINCT ps.section_code ORDER BY ps.section_code) AS sectionCodes

FROM primary_class pc

INNER JOIN primary_section ps
    ON pc.section_id = ps.section_id

INNER JOIN primary_faculty pf
    ON pc.faculty_id = pf.faculty_id

LEFT JOIN (
    SELECT
        class_code,
        GROUP_CONCAT(DISTINCT year_level ORDER BY year_level) AS yearLevels
    FROM primary_student_load
    GROUP BY class_code
) psl
    ON pc.class_code = psl.class_code

WHERE pc.faculty_id IN (
    'ALI-ABB031483',
    'ALI-ACB092081',
    'ALI-DFP082787',
    'ALI-DMF090994',
    'ALI-EBV061577',
    'ALI-ESB082283',
    'ALI-FVD123077',
    'ALI-JST100976',
    'ALI-KES082885',
    'ALI-LBL000000',
    'ALI-MAG021889',
    'ALI-MBT102694',
    'ALI-MCD031565',
    'ALI-NPS111287',
    'ALI-RBO101187',
    'ALI-RCS112680',
    'ALI-RGE112772'
)
  AND ps.program_code = 'BS IND TECH'
  AND pc.school_year = 2025
  AND pc.semester = '2nd'

GROUP BY
    pc.subject_code,
    pc.faculty_id,
    pc.school_year,
    pc.semester,
    pf.college,
    ps.program_code,
    pf.firstname,
    pf.lastname

ORDER BY
    pc.faculty_id,
    pc.subject_code,
    ps.program_code;