-- ═══════════════════════════════════════════════════════════════
-- V9: Centralized Logging (ELK Stack Integration)
-- ═══════════════════════════════════════════════════════════════
-- Structured logging for ELK (Elasticsearch, Logstash, Kibana) integration

CREATE TABLE IF NOT EXISTS application_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME(6) NOT NULL,
    level ENUM('DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL') NOT NULL,
    logger_name VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    exception_message TEXT,
    exception_stack_trace LONGTEXT,
    user_id BIGINT,
    request_id VARCHAR(255),
    correlation_id VARCHAR(255),
    request_method VARCHAR(10),
    request_uri VARCHAR(1000),
    response_status INT,
    response_time_ms INT,
    thread_name VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_log_timestamp (timestamp),
    INDEX idx_log_level (level),
    INDEX idx_log_user (user_id),
    INDEX idx_log_correlation (correlation_id),
    INDEX idx_log_request_id (request_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS event_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME(6) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    user_id BIGINT,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    action VARCHAR(50),
    old_value JSON,
    new_value JSON,
    status VARCHAR(20),
    error_message VARCHAR(500),
    request_id VARCHAR(255),
    correlation_id VARCHAR(255),
    metadata JSON,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_event_timestamp (timestamp),
    INDEX idx_event_type (event_type),
    INDEX idx_event_category (category),
    INDEX idx_event_user (user_id),
    INDEX idx_event_correlation (correlation_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS performance_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME(6) NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    metric_type ENUM('TIMER', 'COUNTER', 'GAUGE', 'HISTOGRAM') NOT NULL,
    endpoint VARCHAR(500),
    method VARCHAR(10),
    duration_ms INT,
    status_code INT,
    success BOOLEAN,
    user_id BIGINT,
    metadata JSON,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_metric_timestamp (timestamp),
    INDEX idx_metric_name (metric_name),
    INDEX idx_metric_endpoint (endpoint),
    INDEX idx_metric_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS security_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME(6) NOT NULL,
    event VARCHAR(100) NOT NULL,
    user_id BIGINT,
    username VARCHAR(255),
    ip_address VARCHAR(45),
    resource VARCHAR(500),
    action VARCHAR(100),
    result ENUM('SUCCESS', 'FAILURE', 'DENIED') NOT NULL,
    reason VARCHAR(500),
    request_id VARCHAR(255),
    correlation_id VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_security_timestamp (timestamp),
    INDEX idx_security_user (user_id),
    INDEX idx_security_result (result),
    INDEX idx_security_ip (ip_address),
    INDEX idx_security_correlation (correlation_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS log_aggregation_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

-- Insert default ELK configuration
INSERT IGNORE INTO log_aggregation_config (config_key, config_value, description) VALUES
('elasticsearch.host', 'localhost', 'Elasticsearch host address'),
('elasticsearch.port', '9200', 'Elasticsearch port'),
('elasticsearch.index.prefix', 'finsight-', 'Index prefix for log indices'),
('logstash.host', 'localhost', 'Logstash host address'),
('logstash.port', '5000', 'Logstash port for log shipment'),
('kibana.url', 'http://localhost:5601', 'Kibana dashboard URL'),
('log.retention.days', '30', 'Days to retain logs in database'),
('log.batch.size', '1000', 'Batch size for log aggregation'),
('elk.enabled', 'false', 'Enable ELK integration'),
('elk.async', 'true', 'Send logs to ELK asynchronously');
