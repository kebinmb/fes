-- =========================
-- PRIMARY DOMAIN INDEXES
-- =========================

CREATE INDEX idx_student_id ON primary_student(student_id);

CREATE INDEX idx_faculty_id ON primary_faculty(faculty_id);

CREATE INDEX idx_subject_code ON primary_subject(subject_code);

CREATE INDEX idx_section_id ON primary_section(section_id);

-- =========================
-- CLASS + LOAD (HIGH JOIN USAGE)
-- =========================

CREATE INDEX idx_class_faculty ON primary_class(faculty_id);
CREATE INDEX idx_class_subject ON primary_class(subject_code);
CREATE INDEX idx_class_section ON primary_class(section_id);
CREATE INDEX idx_class_school_year ON primary_class(school_year);

CREATE INDEX idx_student_load_student ON primary_student_load(student_id);
CREATE INDEX idx_student_load_class ON primary_student_load(class_code);

-- =========================
-- EVALUATION TABLES (CRITICAL)
-- =========================

CREATE INDEX idx_eval_faculty ON faculty_evaluation_score(faculty_id);
CREATE INDEX idx_eval_evaluator ON faculty_evaluation_score(evaluator_id);
CREATE INDEX idx_eval_class ON faculty_evaluation_score(class_code);
CREATE INDEX idx_eval_subject ON faculty_evaluation_score(subject_code);
CREATE INDEX idx_eval_school_year ON faculty_evaluation_score(school_year);

-- 🔥 COMPOSITE INDEX (MOST IMPORTANT QUERY)
CREATE INDEX idx_eval_lookup ON faculty_evaluation_score(
                                                         faculty_id,
                                                         class_code,
                                                         semester,
                                                         school_year
    );
-- =========================
-- EVALUATION CATEGORY TABLES
-- =========================

CREATE INDEX idx_ct_faculty ON commitment_and_transparency(faculty_id);
CREATE INDEX idx_ckpt_faculty ON content_knowledge_pedagogy_and_technology(faculty_id);
CREATE INDEX idx_mtl_faculty ON management_of_teaching_and_learning(faculty_id);

-- =========================
-- AUTHENTICATION (HIGH FREQUENCY)
-- =========================

-- user_accounts
CREATE INDEX idx_user_email ON user_accounts(email);
CREATE INDEX idx_user_username ON user_accounts(username);
CREATE INDEX idx_user_role ON user_accounts(user_role);

-- student_accounts
CREATE INDEX idx_student_accounts_student_id ON student_accounts(student_id);
CREATE INDEX idx_student_accounts_email ON student_accounts(email);

-- refresh_tokens (VERY IMPORTANT)
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_expiry ON refresh_tokens(expiry_date);

-- OTP
CREATE INDEX idx_otp_email ON user_login_otp(user_email);
CREATE INDEX idx_otp_code ON user_login_otp(otp_code);
CREATE INDEX idx_otp_expiry ON user_login_otp(expires_at);

-- student access codes
CREATE INDEX idx_access_student_id ON student_access_codes(student_id);
CREATE INDEX idx_access_code ON student_access_codes(access_code);
CREATE INDEX idx_access_expiry ON student_access_codes(expires_at);