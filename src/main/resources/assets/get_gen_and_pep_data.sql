SELECT * FROM primary_faculty WHERE lastname = "BASCAR";

SELECT * FROM primary_class WHERE faculty_id = "TAL-CAS009";

SELECT * FROM primary_subject WHERE subject_code LIKE "%GEN%";
SELECT * FROM primary_subject WHERE subject_code LIKE "%PEP%";

SELECT *
FROM primary_subject
WHERE 
    UPPER(subject_code) LIKE '%GEN%'
    OR UPPER(subject_code) REGEXP '(^|[- ])PEP([A-Z0-9]|$)';