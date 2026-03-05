-- ═══════════════════════════════════════════════════════════════
-- FinSight – Seed Data
-- Inserts default categories for new installations.
-- Uses INSERT IGNORE to skip duplicates on the (name, type) unique key.
-- ═══════════════════════════════════════════════════════════════

-- Expense categories
INSERT IGNORE INTO categories (name, type) VALUES ('Food', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Transport', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Shopping', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Entertainment', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Healthcare', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Utilities', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Rent', 'EXPENSE');
INSERT IGNORE INTO categories (name, type) VALUES ('Education', 'EXPENSE');

-- Income categories
INSERT IGNORE INTO categories (name, type) VALUES ('Salary', 'INCOME');
INSERT IGNORE INTO categories (name, type) VALUES ('Freelance', 'INCOME');
INSERT IGNORE INTO categories (name, type) VALUES ('Investments', 'INCOME');
INSERT IGNORE INTO categories (name, type) VALUES ('Other Income', 'INCOME');
