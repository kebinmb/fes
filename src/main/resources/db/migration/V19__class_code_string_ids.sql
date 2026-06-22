ALTER TABLE primary_class
    MODIFY COLUMN class_code VARCHAR(255);

ALTER TABLE primary_student_load
    MODIFY COLUMN class_code VARCHAR(255);
