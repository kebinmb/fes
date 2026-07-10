ALTER TABLE refresh_tokens
    ADD COLUMN last_activity_at DATETIME(6) NULL AFTER expiry_date;

UPDATE refresh_tokens
SET last_activity_at = CURRENT_TIMESTAMP(6)
WHERE last_activity_at IS NULL;

ALTER TABLE refresh_tokens
    MODIFY last_activity_at DATETIME(6) NOT NULL;

CREATE INDEX idx_refresh_tokens_last_activity
    ON refresh_tokens(last_activity_at);
