-- ═══════════════════════════════════════════════════════════════
-- V7: Webhook & Notification System
-- ═══════════════════════════════════════════════════════════════
-- Real-time budget alerts and webhook infrastructure

CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    budget_alerts_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    budget_alert_threshold INT NOT NULL DEFAULT 80,
    alert_email BOOLEAN NOT NULL DEFAULT TRUE,
    alert_in_app BOOLEAN NOT NULL DEFAULT TRUE,
    alert_frequency ENUM('REAL_TIME', 'DAILY', 'WEEKLY') NOT NULL DEFAULT 'REAL_TIME',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_notif_pref_user FOREIGN KEY (user_id) REFERENCES app_users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('BUDGET_ALERT', 'ANOMALY_ALERT', 'RECONCILIATION_ALERT', 'SYSTEM') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    metadata JSON,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES app_users(id),
    INDEX idx_notification_user_read (user_id, is_read),
    INDEX idx_notification_created (created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notification_deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    channel ENUM('EMAIL', 'IN_APP', 'WEBHOOK') NOT NULL,
    status ENUM('PENDING', 'SENT', 'FAILED', 'RETRYING') NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    last_error VARCHAR(500),
    next_retry DATETIME(6),
    sent_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_delivery_notification FOREIGN KEY (notification_id) REFERENCES notifications(id),
    INDEX idx_delivery_status (status),
    INDEX idx_delivery_next_retry (next_retry)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS webhooks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    event_types VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    secret VARCHAR(255) NOT NULL,
    retry_count INT NOT NULL DEFAULT 3,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_webhook_user FOREIGN KEY (user_id) REFERENCES app_users(id),
    INDEX idx_webhook_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS webhook_deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    webhook_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    status ENUM('PENDING', 'DELIVERED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    response_status_code INT,
    response_body TEXT,
    attempt_count INT NOT NULL DEFAULT 0,
    next_retry DATETIME(6),
    delivered_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_webhook_delivery FOREIGN KEY (webhook_id) REFERENCES webhooks(id),
    INDEX idx_webhook_delivery_status (status),
    INDEX idx_webhook_delivery_next_retry (next_retry)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS budget_alert_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    budget_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    alert_level ENUM('WARNING', 'CRITICAL') NOT NULL,
    threshold_amount DECIMAL(15,2) NOT NULL,
    current_spending DECIMAL(15,2) NOT NULL,
    percentage_used INT NOT NULL,
    triggered_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_budget_alert_user FOREIGN KEY (user_id) REFERENCES app_users(id),
    CONSTRAINT fk_budget_alert_budget FOREIGN KEY (budget_id) REFERENCES budgets(id),
    CONSTRAINT fk_budget_alert_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_budget_alert_user_date (user_id, triggered_at)
) ENGINE=InnoDB;
