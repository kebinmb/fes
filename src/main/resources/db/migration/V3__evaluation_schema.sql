CREATE TABLE management_of_teaching_and_learning (
                                                     management_of_teaching_and_learning_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     faculty_id VARCHAR(60),
                                                     punctuality VARCHAR(50),
                                                     course_clarity VARCHAR(50),
                                                     time_management VARCHAR(50),
                                                     critical_thinking_facilitation VARCHAR(50),
                                                     independent_learning_guidance VARCHAR(50),
                                                     feedback_communication VARCHAR(50)
);

CREATE TABLE content_knowledge_pedagogy_and_technology (
                                                           content_knowledge_pedagogy_and_technology_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                           faculty_id VARCHAR(60),
                                                           subject_knowledge VARCHAR(50),
                                                           content_simplification VARCHAR(50),
                                                           real_world_application VARCHAR(50),
                                                           technology_integration VARCHAR(50),
                                                           assessment_alignment VARCHAR(50)
);

CREATE TABLE commitment_and_transparency (
                                             commitment_and_transparency_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             faculty_id VARCHAR(60),
                                             diversity_recognition VARCHAR(50),
                                             consultation_support VARCHAR(50),
                                             immediate_feedback VARCHAR(50),
                                             transparent_grading VARCHAR(50)
);