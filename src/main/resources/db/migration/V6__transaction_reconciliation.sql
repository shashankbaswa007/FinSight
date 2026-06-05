-- ═══════════════════════════════════════════════════════════════
-- V6: Transaction Reconciliation System
-- ═══════════════════════════════════════════════════════════════
-- Enables daily settlement verification and bank reconciliation

CREATE TABLE IF NOT EXISTS reconciliation_batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    batch_date DATE NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    total_transactions INT NOT NULL DEFAULT 0,
    matched_transactions INT NOT NULL DEFAULT 0,
    unmatched_transactions INT NOT NULL DEFAULT 0,
    discrepancy_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_reconciliation_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_reconciliation_user_date (user_id, batch_date),
    INDEX idx_reconciliation_status (status),
    UNIQUE KEY uk_reconciliation_user_date (user_id, batch_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transaction_matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reconciliation_batch_id BIGINT NOT NULL,
    internal_transaction_id BIGINT NOT NULL,
    external_transaction_id VARCHAR(255),
    external_amount DECIMAL(15,2) NOT NULL,
    internal_amount DECIMAL(15,2) NOT NULL,
    match_confidence DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    match_status ENUM('EXACT_MATCH', 'PARTIAL_MATCH', 'NO_MATCH') NOT NULL,
    variance_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    matched_at DATETIME(6),
    notes VARCHAR(500),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_match_batch FOREIGN KEY (reconciliation_batch_id) REFERENCES reconciliation_batches(id),
    CONSTRAINT fk_match_transaction FOREIGN KEY (internal_transaction_id) REFERENCES transactions(id),
    INDEX idx_match_batch (reconciliation_batch_id),
    INDEX idx_match_status (match_status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS external_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    source VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description VARCHAR(500),
    transaction_date DATE NOT NULL,
    imported_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_external_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_external_user_date (user_id, transaction_date),
    INDEX idx_external_source (source),
    UNIQUE KEY uk_external_id (external_id, source, user_id)
) ENGINE=InnoDB;
