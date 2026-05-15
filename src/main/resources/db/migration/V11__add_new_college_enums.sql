ALTER TABLE primary_faculty
MODIFY COLUMN college ENUM(
    'CAS',
    'CIT',
    'COED',
    'COENG',
    'CCJ',
    'CCS',
    'COF',
    'FOR_MIGRATION'
);