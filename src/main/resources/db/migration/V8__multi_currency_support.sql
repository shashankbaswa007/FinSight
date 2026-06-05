-- ═══════════════════════════════════════════════════════════════
-- V8: Multi-Currency Support
-- ═══════════════════════════════════════════════════════════════
-- Multi-currency wallets, transactions, and exchange rates

CREATE TABLE IF NOT EXISTS currencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exchange_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_currency_id BIGINT NOT NULL,
    to_currency_id BIGINT NOT NULL,
    rate DECIMAL(18,8) NOT NULL,
    source VARCHAR(100) NOT NULL,
    effective_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_exchange_from FOREIGN KEY (from_currency_id) REFERENCES currencies(id),
    CONSTRAINT fk_exchange_to FOREIGN KEY (to_currency_id) REFERENCES currencies(id),
    INDEX idx_exchange_from_to (from_currency_id, to_currency_id),
    INDEX idx_exchange_effective_date (effective_date),
    UNIQUE KEY uk_exchange_rate (from_currency_id, to_currency_id, effective_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    currency_id BIGINT NOT NULL,
    balance DECIMAL(20,8) NOT NULL DEFAULT 0.00,
    primary_wallet BOOLEAN NOT NULL DEFAULT FALSE,
    wallet_name VARCHAR(100),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_wallet_currency FOREIGN KEY (currency_id) REFERENCES currencies(id),
    INDEX idx_wallet_user (user_id),
    UNIQUE KEY uk_wallet_user_currency (user_id, currency_id)
) ENGINE=InnoDB;

ALTER TABLE transactions ADD COLUMN currency_id BIGINT AFTER amount;
ALTER TABLE transactions ADD COLUMN base_currency_id BIGINT AFTER currency_id;
ALTER TABLE transactions ADD COLUMN exchange_rate DECIMAL(18,8) DEFAULT 1.00000000 AFTER base_currency_id;
ALTER TABLE transactions ADD CONSTRAINT fk_transaction_currency FOREIGN KEY (currency_id) REFERENCES currencies(id);
ALTER TABLE transactions ADD CONSTRAINT fk_transaction_base_currency FOREIGN KEY (base_currency_id) REFERENCES currencies(id);

ALTER TABLE budgets ADD COLUMN currency_id BIGINT AFTER monthly_limit;
ALTER TABLE budgets ADD CONSTRAINT fk_budget_currency FOREIGN KEY (currency_id) REFERENCES currencies(id);

ALTER TABLE recurring_transactions ADD COLUMN currency_id BIGINT AFTER amount;
ALTER TABLE recurring_transactions ADD CONSTRAINT fk_recurring_currency FOREIGN KEY (currency_id) REFERENCES currencies(id);

CREATE TABLE IF NOT EXISTS currency_conversion_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    from_currency_id BIGINT NOT NULL,
    to_currency_id BIGINT NOT NULL,
    from_amount DECIMAL(20,8) NOT NULL,
    to_amount DECIMAL(20,8) NOT NULL,
    rate_used DECIMAL(18,8) NOT NULL,
    rate_source VARCHAR(100),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_conversion_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_conversion_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT fk_conversion_from FOREIGN KEY (from_currency_id) REFERENCES currencies(id),
    CONSTRAINT fk_conversion_to FOREIGN KEY (to_currency_id) REFERENCES currencies(id),
    INDEX idx_conversion_user_date (user_id, created_at)
) ENGINE=InnoDB;

-- Seed common currencies
INSERT IGNORE INTO currencies (code, name, symbol, active) VALUES 
('USD', 'United States Dollar', '$', TRUE),
('EUR', 'Euro', '€', TRUE),
('GBP', 'British Pound', '£', TRUE),
('JPY', 'Japanese Yen', '¥', TRUE),
('INR', 'Indian Rupee', '₹', TRUE),
('CAD', 'Canadian Dollar', '$', TRUE),
('AUD', 'Australian Dollar', '$', TRUE),
('CHF', 'Swiss Franc', 'CHF', TRUE),
('CNY', 'Chinese Yuan', '¥', TRUE),
('SGD', 'Singapore Dollar', '$', TRUE);
