-- Migration V12: Add last login tracking to users

ALTER TABLE users
    ADD COLUMN last_login DATETIME(6) COMMENT 'Last successful login timestamp';
