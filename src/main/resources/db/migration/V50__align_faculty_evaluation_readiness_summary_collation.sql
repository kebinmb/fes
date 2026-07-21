ALTER TABLE faculty_evaluation_readiness_summary
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE faculty_evaluation_readiness_summary
    MODIFY faculty_id VARCHAR(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY semester VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY campus VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
    MODIFY subjects TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;