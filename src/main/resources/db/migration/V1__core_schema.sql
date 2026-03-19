CREATE TABLE primary_program (
                                 primary_program_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 program_code VARCHAR(255),
                                 program_title VARCHAR(255),
                                 year_granted INT,
                                 college_code VARCHAR(255)
);

CREATE TABLE primary_subject (
                                 primary_subject_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 subject_code VARCHAR(255) UNIQUE,
                                 descriptive_title VARCHAR(255)
);

CREATE TABLE primary_faculty (
                                 primary_faculty_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 faculty_id VARCHAR(255) UNIQUE,
                                 lastname VARCHAR(255),
                                 firstname VARCHAR(255),
                                 middlename VARCHAR(255),
                                 position VARCHAR(255),
                                 load_limit DOUBLE,
                                 college VARCHAR(50),
                                 status VARCHAR(20)
);

CREATE TABLE primary_student (
                                 primary_student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 student_id VARCHAR(255) UNIQUE,
                                 curriculum_major_id INT,
                                 student_lastname VARCHAR(255),
                                 student_firstname VARCHAR(255),
                                 student_middlename VARCHAR(255),
                                 gender VARCHAR(10)
);