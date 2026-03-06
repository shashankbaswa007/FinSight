-- ═══════════════════════════════════════════════════════════════
-- V1: Initial schema for FinSight
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    UNIQUE KEY uk_category_name_type (name, type)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(500),
    date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_transaction_user_date (user_id, date),
    INDEX idx_transaction_user_category (user_id, category_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    monthly_limit DECIMAL(15,2) NOT NULL,
    budget_month INT NOT NULL,
    budget_year INT NOT NULL,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS recurring_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(500),
    frequency ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    next_occurrence DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_recurring_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_recurring_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

-- ═══════════════════════════════════════════════════════════════
-- Seed default categories
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO categories (name, type) VALUES ('Food', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Transport', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Shopping', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Entertainment', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Healthcare', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Utilities', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Rent', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Education', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Salary', 'INCOME');
INSERT IGNORE INTO categories (name, type) VALUES ('Freelance', 'INCOME');
INSERT IGNORE INTO categories (name, type) VALUES ('Investments', 'INCOME');
INSERT IGNORE INTO categories (name, type) VALUES ('Other Income', 'INCOME');
