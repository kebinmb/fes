CREATE TABLE user_accounts (
                               user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               username VARCHAR(50) UNIQUE NOT NULL,
                               email VARCHAR(100) UNIQUE NOT NULL,
                               password VARCHAR(255) NOT NULL,
                               user_role ENUM('DEAN','FACULTY','PROGRAM_CHAIR','STUDENT') NOT NULL,
                               college ENUM('CAS','CIT','COED','COENG','CCS','FOR_MIGRATION'),
                               is_enabled BIT(1) NOT NULL,
                               is_locked BIT(1) NOT NULL,
                               last_login_at DATETIME(6),
                               created_at DATETIME(6),
                               updated_at DATETIME(6),
                               reset_token VARCHAR(255),
                               reset_token_expiry DATETIME(6),
                               status ENUM('ACTIVE','INACTIVE') NOT NULL
);

CREATE TABLE user_login_otp (
                                otp_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_email VARCHAR(100) NOT NULL,
                                otp_code VARCHAR(10) NOT NULL,
                                attempt_count INT NOT NULL,
                                used BIT NOT NULL,
                                used_at DATETIME,
                                created_at DATETIME NOT NULL,
                                expires_at DATETIME NOT NULL,

                                CONSTRAINT fk_otp_user FOREIGN KEY (user_email)
                                    REFERENCES user_accounts(email)
);

CREATE TABLE student_accounts (
                                  student_account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  student_id VARCHAR(15) UNIQUE NOT NULL,
                                  email VARCHAR(100) UNIQUE NOT NULL,
                                  password VARCHAR(255) NOT NULL,
                                  is_enabled BIT NOT NULL,
                                  is_locked BIT NOT NULL,
                                  created_at DATETIME,
                                  updated_at DATETIME
);

CREATE TABLE student_access_codes (
                                      access_code_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      student_account_id BIGINT,
                                      student_id VARCHAR(15) NOT NULL,
                                      access_code VARCHAR(20) UNIQUE NOT NULL,
                                      is_used BIT NOT NULL,
                                      used_at DATETIME,
                                      is_completed BIT NOT NULL,
                                      completed_at DATETIME,
                                      created_at DATETIME,
                                      expires_at DATETIME NOT NULL,

                                      CONSTRAINT fk_access_student_account FOREIGN KEY (student_account_id)
                                          REFERENCES student_accounts(student_account_id)
);

CREATE TABLE refresh_tokens (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id VARCHAR(255),
                                token_hash VARCHAR(64) UNIQUE NOT NULL,
                                expiry_date DATETIME,
                                revoked BIT,
                                device_info VARCHAR(255)
);