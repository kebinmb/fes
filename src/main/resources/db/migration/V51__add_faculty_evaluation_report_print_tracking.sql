ALTER TABLE faculty_evaluation_report
    ADD COLUMN print_tracking_available TINYINT(1) NOT NULL DEFAULT 1 AFTER generated_at,
    ADD COLUMN printed_at DATETIME(6) NULL AFTER print_tracking_available,
    ADD COLUMN printed_by_user_id BIGINT NULL AFTER printed_at,
    ADD COLUMN printed_by_username VARCHAR(255) NULL AFTER printed_by_user_id,
    ADD COLUMN print_count INT NOT NULL DEFAULT 0 AFTER printed_by_username,
    ADD COLUMN annex_d_printed_at DATETIME(6) NULL AFTER print_count,
    ADD COLUMN annex_d_printed_by_user_id BIGINT NULL AFTER annex_d_printed_at,
    ADD COLUMN annex_d_printed_by_username VARCHAR(255) NULL AFTER annex_d_printed_by_user_id,
    ADD COLUMN annex_d_print_count INT NOT NULL DEFAULT 0 AFTER annex_d_printed_by_username,
    ADD INDEX idx_faculty_evaluation_report_latest_valid (
        faculty_id,
        school_year,
        semester,
        status,
        generated_at
    ),
    ADD INDEX idx_faculty_evaluation_report_printed_at (printed_at),
    ADD INDEX idx_faculty_evaluation_report_annex_d_printed_at (annex_d_printed_at);

UPDATE faculty_evaluation_report
SET printed_at = generated_at,
    printed_by_user_id = generated_by_user_id,
    printed_by_username = generated_by_username,
    print_count = 1
WHERE generated_at IS NOT NULL
  AND printed_at IS NULL;
