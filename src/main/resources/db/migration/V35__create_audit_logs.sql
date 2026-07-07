CREATE TABLE IF NOT EXISTS audit_logs (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    username VARCHAR(255) NULL,
    entity_type VARCHAR(255) NULL,
    entity_id INT NULL,
    action VARCHAR(255) NULL,
    ip_address VARCHAR(100) NULL,
    user_agent VARCHAR(512) NULL,
    request_method VARCHAR(20) NULL,
    request_path VARCHAR(512) NULL,
    execution_time_ms BIGINT NULL,
    old_value LONGTEXT NULL,
    new_value LONGTEXT NULL,
    created_at DATETIME(6) NOT NULL
);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs (created_at);

CREATE INDEX idx_audit_logs_user_id_created_at
    ON audit_logs (user_id, created_at);

CREATE INDEX idx_audit_logs_action_created_at
    ON audit_logs (action, created_at);

CREATE INDEX idx_audit_logs_entity_created_at
    ON audit_logs (entity_type, created_at);
