-- ═══════════════════════════════════════════════════════════════
-- V2: Transaction audit trail for regulatory compliance
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS transaction_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT,
    user_id BIGINT NOT NULL,
    action ENUM('CREATE', 'UPDATE', 'DELETE') NOT NULL,
    old_amount DECIMAL(15,2),
    new_amount DECIMAL(15,2),
    old_type VARCHAR(10),
    new_type VARCHAR(10),
    old_category VARCHAR(100),
    new_category VARCHAR(100),
    old_description VARCHAR(500),
    new_description VARCHAR(500),
    performed_by VARCHAR(255) NOT NULL,
    performed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_audit_transaction (transaction_id),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_performed_at (performed_at)
) ENGINE=InnoDB;
