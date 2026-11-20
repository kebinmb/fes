ALTER TABLE primary_subject
    ADD COLUMN lec_units DOUBLE NULL AFTER descriptive_title,
    ADD COLUMN lab_units DOUBLE NULL AFTER lec_units,
    ADD COLUMN lec_hours DOUBLE NULL AFTER lab_units,
    ADD COLUMN lab_hours DOUBLE NULL AFTER lec_hours;
