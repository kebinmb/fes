CREATE INDEX idx_psl_student_year
ON primary_student_load(student_id, year_level);

CREATE INDEX idx_psl_class_code
ON primary_student_load(class_code);

CREATE INDEX idx_pc_class_year
ON primary_class(class_code, school_year);