-- ═══════════════════════════════════════════════════════════════
-- FinSight – Seed Data
-- Inserts default categories and currencies for new installations.
-- ═══════════════════════════════════════════════════════════════

-- Expense categories (H2 compatible: MERGE statement)
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Food', 'EXPENSE');
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Transport', 'EXPENSE');
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Shopping', 'EXPENSE');
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Entertainment', 'EXPENSE');
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Healthcare', 'EXPENSE');
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Utilities', 'EXPENSE');
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Rent', 'EXPENSE');
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Education', 'EXPENSE');

-- Income categories
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Salary', 'INCOME');
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Freelance', 'INCOME');
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Investments', 'INCOME');
MERGE INTO categories (name, type) KEY(name, type) VALUES ('Other Income', 'INCOME');

-- Default currencies
MERGE INTO currencies (code, name, symbol, active, created_at) KEY(code) VALUES ('USD', 'US Dollar', '$', true, NOW());
MERGE INTO currencies (code, name, symbol, active, created_at) KEY(code) VALUES ('EUR', 'Euro', '€', true, NOW());
MERGE INTO currencies (code, name, symbol, active, created_at) KEY(code) VALUES ('GBP', 'British Pound', '£', true, NOW());
