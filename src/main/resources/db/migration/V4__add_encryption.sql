-- Migration V4: Add Encryption-Ready Columns
-- Purpose: Add encrypted columns for sensitive financial data
-- Strategy: Add new encrypted columns, keep old ones during migration period for data safety
-- After data migration and verification (typically 1-2 weeks), old columns can be dropped

-- ──── Add Encrypted Columns to transactions ────
ALTER TABLE transactions ADD COLUMN amount_encrypted LONGBLOB COMMENT 'AES-256 encrypted transaction amount';
ALTER TABLE transactions ADD COLUMN description_encrypted LONGBLOB COMMENT 'AES-256 encrypted transaction description';

-- Create indexes on encrypted columns for new queries
CREATE INDEX idx_transaction_user_date_encrypted ON transactions(user_id, date) WHERE amount_encrypted IS NOT NULL;

-- ──── Add Encrypted Columns to recurring_transactions ────
ALTER TABLE recurring_transactions ADD COLUMN amount_encrypted LONGBLOB COMMENT 'AES-256 encrypted recurring amount';

-- ──── Add Encryption Metadata Table ────
CREATE TABLE IF NOT EXISTS encryption_migrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL,
    column_name VARCHAR(255) NOT NULL,
    total_rows BIGINT,
    migrated_rows BIGINT,
    migration_status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    started_at DATETIME(6),
    completed_at DATETIME(6),
    error_message TEXT,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_migration_column (table_name, column_name)
) COMMENT 'Tracks encryption migration progress for audit trail';

-- ──── Add PII Masking Table ----
CREATE TABLE IF NOT EXISTS pii_access_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL,
    column_name VARCHAR(255) NOT NULL,
    access_type ENUM('READ', 'WRITE', 'DELETE') NOT NULL,
    accessed_by_user_id BIGINT,
    accessed_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    ipv4_address VARCHAR(45),
    is_masked BOOLEAN DEFAULT FALSE,
    purpose VARCHAR(255),
    INDEX idx_pii_user_date (accessed_by_user_id, accessed_at),
    INDEX idx_pii_table_date (table_name, accessed_at)
) COMMENT 'Audit log for sensitive PII access (compliance requirement)';

-- ──── Add Audit Log Masking Indicators ----
-- Update transaction_audit_log to include encryption status
ALTER TABLE transaction_audit_log ADD COLUMN is_pii_masked BOOLEAN DEFAULT FALSE COMMENT 'Whether sensitive values were masked in this audit log';
ALTER TABLE transaction_audit_log ADD COLUMN pii_mask_reason VARCHAR(255) COMMENT 'Reason for masking (e.g., GDPR_REQUEST, SECURITY_REVIEW)';

-- Create migration status tracking view
CREATE OR REPLACE VIEW encryption_migration_status AS
SELECT 
    'transactions' AS table_name,
    'amount' AS column_name,
    COUNT(*) AS total_rows,
    SUM(CASE WHEN amount_encrypted IS NOT NULL THEN 1 ELSE 0 END) AS migrated_rows,
    ROUND(100.0 * SUM(CASE WHEN amount_encrypted IS NOT NULL THEN 1 ELSE 0 END) / COUNT(*), 2) AS migration_percentage,
    CASE 
        WHEN SUM(CASE WHEN amount_encrypted IS NOT NULL THEN 1 ELSE 0 END) = COUNT(*) THEN 'COMPLETED'
        WHEN SUM(CASE WHEN amount_encrypted IS NOT NULL THEN 1 ELSE 0 END) > 0 THEN 'IN_PROGRESS'
        ELSE 'PENDING'
    END AS status
FROM transactions
UNION ALL
SELECT 
    'recurring_transactions' AS table_name,
    'amount' AS column_name,
    COUNT(*) AS total_rows,
    SUM(CASE WHEN amount_encrypted IS NOT NULL THEN 1 ELSE 0 END) AS migrated_rows,
    ROUND(100.0 * SUM(CASE WHEN amount_encrypted IS NOT NULL THEN 1 ELSE 0 END) / COUNT(*), 2) AS migration_percentage,
    CASE 
        WHEN SUM(CASE WHEN amount_encrypted IS NOT NULL THEN 1 ELSE 0 END) = COUNT(*) THEN 'COMPLETED'
        WHEN SUM(CASE WHEN amount_encrypted IS NOT NULL THEN 1 ELSE 0 END) > 0 THEN 'IN_PROGRESS'
        ELSE 'PENDING'
    END AS status
FROM recurring_transactions;
