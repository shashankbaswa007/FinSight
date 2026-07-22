ALTER TABLE app_users
ADD COLUMN telegram_chat_id BIGINT UNIQUE,
ADD COLUMN telegram_linking_code VARCHAR(6);
