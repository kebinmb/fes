ALTER TABLE faculty_evaluation_score
    DROP INDEX uq_evaluation;

ALTER TABLE faculty_evaluation_score
    ADD CONSTRAINT uq_evaluation
        UNIQUE (
            faculty_id,
            evaluator_id,
            class_code,
            subject_code,
            year_level,
            semester,
            school_year
        );
