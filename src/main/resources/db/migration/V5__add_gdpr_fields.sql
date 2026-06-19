-- Migration V5: Add GDPR Support - Data Export and Deletion
-- Purpose: Add support for GDPR Article 17 (right to erasure) and Article 20 (data portability)
-- Strategy: Soft-delete with 90-day retention before hard-delete

-- ──── Add GDPR Fields to app_users ────
ALTER TABLE app_users ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE COMMENT 'Soft-delete flag for GDPR compliance';
ALTER TABLE app_users ADD COLUMN deletion_requested_at DATETIME(6) COMMENT 'Timestamp when user requested deletion';
ALTER TABLE app_users ADD COLUMN deletion_reason VARCHAR(255) COMMENT 'User-provided reason for deletion';
ALTER TABLE app_users ADD COLUMN hard_delete_scheduled_at DATETIME(6) COMMENT 'Scheduled hard-delete date (90 days after soft-delete)';

-- Indexes for GDPR queries
CREATE INDEX idx_users_deleted_at ON app_users(deletion_requested_at);
CREATE INDEX idx_users_hard_delete_scheduled ON app_users(hard_delete_scheduled_at);
CREATE INDEX idx_users_is_deleted ON app_users(is_deleted);

-- ──── Create GDPR Audit Table ────
CREATE TABLE IF NOT EXISTS gdpr_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_type ENUM('EXPORT', 'DELETE') NOT NULL COMMENT 'Type of GDPR request',
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    data_exported_at DATETIME(6) COMMENT 'When data was exported (for EXPORT requests)',
    soft_deleted_at DATETIME(6) COMMENT 'When soft-delete was applied (for DELETE requests)',
    hard_deleted_at DATETIME(6) COMMENT 'When hard-delete will occur',
    export_file_path VARCHAR(255) COMMENT 'Path to exported data file',
    reason VARCHAR(255),
    requested_by_ip VARCHAR(45),
    requested_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    processed_at DATETIME(6),
    FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE RESTRICT,
    INDEX idx_gdpr_user_type (user_id, request_type),
    INDEX idx_gdpr_status_date (status, requested_at),
    INDEX idx_gdpr_hard_delete (hard_deleted_at)
) COMMENT 'Audit trail for GDPR data export and deletion requests';

-- ──── Create GDPR Data Retention Policy Table ----
CREATE TABLE IF NOT EXISTS gdpr_retention_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL COMMENT 'Entity type (e.g., User, Transaction, Audit)',
    retention_days INT NOT NULL DEFAULT 90 COMMENT 'Days to retain after deletion request',
    hard_delete_enabled BOOLEAN DEFAULT TRUE COMMENT 'Whether to perform hard-delete after retention period',
    archival_enabled BOOLEAN DEFAULT FALSE COMMENT 'Whether to archive before deletion',
    archive_location VARCHAR(255) COMMENT 'Where archived data is stored',
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_entity_type (entity_type),
    INDEX idx_retention_entity (entity_type)
) COMMENT 'GDPR data retention policies for different entity types';

-- Insert default retention policies
INSERT INTO gdpr_retention_policies (entity_type, retention_days, hard_delete_enabled, archival_enabled)
VALUES 
    ('USER', 90, TRUE, FALSE),
    ('TRANSACTION', 90, TRUE, TRUE),
    ('AUDIT_LOG', 365, TRUE, TRUE),
    ('BUDGET', 90, TRUE, FALSE),
    ('RECURRING_TRANSACTION', 90, TRUE, FALSE)
ON DUPLICATE KEY UPDATE retention_days=VALUES(retention_days);

-- ──── Create Anonymization Template Table ----
CREATE TABLE IF NOT EXISTS anonymization_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    anonymization_method ENUM('HASH', 'MASK', 'NULL', 'RANDOM') NOT NULL DEFAULT 'MASK',
    replacement_value VARCHAR(255),
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_entity_field (entity_type, field_name),
    INDEX idx_anonymization_entity (entity_type)
) COMMENT 'Defines how to anonymize fields during soft-delete';

-- Insert default anonymization templates
INSERT INTO anonymization_templates (entity_type, field_name, anonymization_method, replacement_value)
VALUES
    ('USER', 'email', 'MASK', 'deleted-user@anonymized.local'),
    ('USER', 'name', 'MASK', 'Anonymous User'),
    ('TRANSACTION', 'description', 'MASK', '[DELETED]'),
    ('AUDIT_LOG', 'user_name', 'MASK', '[ANONYMIZED]')
ON DUPLICATE KEY UPDATE anonymization_method=VALUES(anonymization_method);

-- ──── Create View for GDPR Status ----
CREATE OR REPLACE VIEW gdpr_deletion_schedule AS
SELECT 
    u.id AS user_id,
    u.email,
    u.deletion_requested_at,
    u.hard_delete_scheduled_at,
    DATEDIFF(DAY, NOW(), u.hard_delete_scheduled_at) AS days_until_hard_delete,
    CASE 
        WHEN u.hard_delete_scheduled_at <= NOW() THEN 'READY_FOR_HARD_DELETE'
        WHEN u.hard_delete_scheduled_at IS NOT NULL THEN 'SCHEDULED_FOR_DELETION'
        ELSE 'ACTIVE'
    END AS deletion_status,
    grp.retention_days
FROM app_users u
LEFT JOIN gdpr_retention_policies grp ON grp.entity_type = 'USER'
WHERE u.is_deleted = TRUE
ORDER BY u.hard_delete_scheduled_at ASC;

-- ──── Create Stored Procedure for Hard Delete ----
