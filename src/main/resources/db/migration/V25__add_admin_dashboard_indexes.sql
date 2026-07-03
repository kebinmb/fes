CREATE INDEX idx_pc_term_class
    ON primary_class(school_year, semester, class_code);

CREATE INDEX idx_pc_term_faculty
    ON primary_class(school_year, semester, faculty_id);

CREATE INDEX idx_pc_term_section
    ON primary_class(school_year, semester, section_id);

CREATE INDEX idx_psl_class_student
    ON primary_student_load(class_code, student_id);

CREATE INDEX idx_fes_term_evaluator_class
    ON faculty_evaluation_score(school_year, semester, evaluator_id, class_code);

CREATE INDEX idx_fes_term_faculty_class
    ON faculty_evaluation_score(school_year, semester, faculty_id, class_code);
