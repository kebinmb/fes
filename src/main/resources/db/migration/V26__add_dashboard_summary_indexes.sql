CREATE INDEX idx_pc_term_subject
    ON primary_class(school_year, semester, subject_code);

CREATE INDEX idx_fes_term_score
    ON faculty_evaluation_score(school_year, semester, overall_average_score);

CREATE INDEX idx_fes_term_evaluator
    ON faculty_evaluation_score(school_year, semester, evaluator_id);

CREATE INDEX idx_ps_program_section
    ON primary_section(program_code, section_id);
