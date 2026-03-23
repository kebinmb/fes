CREATE TABLE management_of_teaching_and_learning (
                                                     management_of_teaching_and_learning_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     faculty_id VARCHAR(60),

                                                     punctuality ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                                     course_clarity ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                                     time_management ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                                     critical_thinking_facilitation ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                                     independent_learning_guidance ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                                     feedback_communication ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL
);

CREATE TABLE content_knowledge_pedagogy_and_technology (
                                                           content_knowledge_pedagogy_and_technology_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                           faculty_id VARCHAR(60),

                                                           subject_knowledge ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                                           content_simplification ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                                           real_world_application ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                                           technology_integration ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                                           assessment_alignment ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL
);

CREATE TABLE commitment_and_transparency (
                                             commitment_and_transparency_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             faculty_id VARCHAR(60),

                                             diversity_recognition ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                             consultation_support ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                             immediate_feedback ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
                                             transparent_grading ENUM('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL
);