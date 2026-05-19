UPDATE syndicateadmin_faculty_evaluation_db.primary_faculty pf

INNER JOIN primary_class pc
    ON pf.faculty_id = pc.faculty_id

SET pf.college = 'COENG'

WHERE pf.status = 'ACTIVE'

AND (pf.lastname, pf.firstname) IN (
('Casibua', 'Apolonio Jr.'),
('Forton', 'Michael'),
('Gerona', 'Grace'),
('Gerona', 'Roy'),
('Gotera', 'Geronemo III'),
('Marquez', 'Jun-Jun'),
('Ramos', 'Rey'),
('Ringor', 'Maria Rizalina'),
('Ta-ala', 'Kaj Neil')
);