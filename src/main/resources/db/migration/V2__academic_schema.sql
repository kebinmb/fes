CREATE TABLE primary_section (
                                 primary_section_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 section_id INT UNIQUE,
                                 year_level VARCHAR(255),
                                 program_code VARCHAR(255),
                                 section_code VARCHAR(255)
);

CREATE TABLE primary_class (
                               primary_class_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               class_code VARCHAR(255),
                               faculty_id VARCHAR(255),
                               subject_code VARCHAR(255),
                               section_id INT,
                               semester VARCHAR(50),
                               school_year INT,
                               schedule_day VARCHAR(50),
                               schedule_time VARCHAR(50),
                               room VARCHAR(50),
                               created_at DATETIME,

                               CONSTRAINT fk_class_faculty FOREIGN KEY (faculty_id)
                                   REFERENCES primary_faculty(faculty_id),

                               CONSTRAINT fk_class_subject FOREIGN KEY (subject_code)
                                   REFERENCES primary_subject(subject_code),

                               CONSTRAINT fk_class_section FOREIGN KEY (section_id)
                                   REFERENCES primary_section(section_id)
);

CREATE TABLE primary_student_load (
                                      primay_student_load_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      load_id INT,
                                      student_id VARCHAR(255),
                                      year_level VARCHAR(50),
                                      class_code INT,
                                      grade VARCHAR(50),

                                      CONSTRAINT fk_student_load FOREIGN KEY (student_id)
                                          REFERENCES primary_student(student_id)
);