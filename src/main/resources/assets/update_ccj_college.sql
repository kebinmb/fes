UPDATE syndicateadmin_faculty_evaluation_db.primary_faculty pf
INNER JOIN primary_class pc
    ON pf.faculty_id = pc.faculty_id
SET pf.college = 'CCJ'
WHERE pf.lastname IN (
    'Dequilato',
    'Domingo',
    'Egca',
    'Francisco',
    'Ga',
    'Genova',
    'Lamasan',
    'Progoso',
    'Rebucas',
    'Rojo',
    'Romero',
    'Sarimong',
    'Taloring',
    'Tolitol'
);