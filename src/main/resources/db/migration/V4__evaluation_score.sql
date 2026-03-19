CREATE TABLE faculty_evaluation_score (
                                          faculty_evaluation_score_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                          faculty_id VARCHAR(60) NOT NULL,
                                          evaluator_id VARCHAR(15) NOT NULL,
                                          student_id VARCHAR(15),
                                          class_code VARCHAR(20) NOT NULL,
                                          semester VARCHAR(10) NOT NULL,
                                          school_year INT NOT NULL,
                                          subject_code VARCHAR(255) NOT NULL,

                                          comments_or_feedbacks VARCHAR(255),

                                          management_of_teaching_and_learning_id BIGINT,
                                          content_knowledge_pedagogy_and_technology_id BIGINT,
                                          commitment_and_transparency_id BIGINT,

                                          overall_average_score DOUBLE,
                                          overall_interpretation VARCHAR(20),

                                          created_at DATETIME,
                                          updated_at DATETIME,

                                          CONSTRAINT uq_evaluation UNIQUE (
                                                                           faculty_id, student_id, class_code, semester, school_year
                                              ),

                                          CONSTRAINT fk_eval_mtl FOREIGN KEY (management_of_teaching_and_learning_id)
                                              REFERENCES management_of_teaching_and_learning(management_of_teaching_and_learning_id),

                                          CONSTRAINT fk_eval_ckpt FOREIGN KEY (content_knowledge_pedagogy_and_technology_id)
                                              REFERENCES content_knowledge_pedagogy_and_technology(content_knowledge_pedagogy_and_technology_id),

                                          CONSTRAINT fk_eval_ct FOREIGN KEY (commitment_and_transparency_id)
                                              REFERENCES commitment_and_transparency(commitment_and_transparency_id)
);