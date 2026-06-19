-- Migration V12: Add last login tracking to app_users

ALTER TABLE app_users
    ADD COLUMN last_login DATETIME(6) COMMENT 'Last successful login timestamp';
