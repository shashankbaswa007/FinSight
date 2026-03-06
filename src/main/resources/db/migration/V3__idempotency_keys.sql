CREATE TABLE IF NOT EXISTS idempotency_keys (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    user_id       BIGINT       NOT NULL,
    endpoint      VARCHAR(255) NOT NULL,
    response_body TEXT,
    status_code   INT          NOT NULL DEFAULT 200,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_key_user_endpoint (idempotency_key, user_id, endpoint),
    INDEX idx_idempotency_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
