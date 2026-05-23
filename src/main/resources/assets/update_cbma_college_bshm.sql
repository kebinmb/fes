UPDATE syndicateadmin_faculty_evaluation_db_prod.primary_faculty pf

INNER JOIN primary_class pc
    ON pf.faculty_id = pc.faculty_id

SET pf.college = 'CBMA'

WHERE pf.status = 'ACTIVE'

AND (pf.lastname, pf.firstname) IN (
('ABETO','MARNYL JOHN'),
('BARATO','NIEVES'),
('MANA-AY','ANNIELOU'),
('TAGUINES','GILBERT'),
('TIPON', 'TRICIA ANNE'),
('TORRES','GRACIEL'),
('VALLADAREZ','JOHN ROLAND')
);