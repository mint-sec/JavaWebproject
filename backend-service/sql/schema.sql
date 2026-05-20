CREATE TABLE vehicles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicle_code VARCHAR(32) NOT NULL UNIQUE,
    plate_number VARCHAR(32) NOT NULL,
    cargo_type VARCHAR(32) NOT NULL,
    cargo_name VARCHAR(64) NOT NULL,
    safe_temp_min DECIMAL(5,2) NOT NULL,
    safe_temp_max DECIMAL(5,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE cargo_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cargo_type VARCHAR(32) NOT NULL UNIQUE,
    cargo_name VARCHAR(64) NOT NULL,
    safe_temp_min DECIMAL(5,2) NOT NULL,
    safe_temp_max DECIMAL(5,2) NOT NULL,
    sensitivity_level VARCHAR(16) NOT NULL,
    description VARCHAR(255) NULL
);

CREATE TABLE telemetry_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicle_code VARCHAR(32) NOT NULL,
    record_time DATETIME NOT NULL,
    temperature DECIMAL(5,2) NOT NULL,
    humidity DECIMAL(5,2) NOT NULL,
    door_open TINYINT(1) NOT NULL,
    speed DECIMAL(6,2) NOT NULL,
    outside_temp DECIMAL(5,2) NOT NULL,
    lng DECIMAL(10,6) NOT NULL,
    lat DECIMAL(10,6) NOT NULL,
    remaining_km DECIMAL(8,2) NOT NULL,
    trend VARCHAR(64) NULL,
    INDEX idx_telemetry_vehicle_time (vehicle_code, record_time)
);

CREATE TABLE alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alert_id VARCHAR(40) NOT NULL UNIQUE,
    vehicle_code VARCHAR(32) NOT NULL,
    alert_level VARCHAR(16) NOT NULL,
    alert_type VARCHAR(32) NOT NULL,
    title VARCHAR(128) NOT NULL,
    detail_text VARCHAR(255) NOT NULL,
    suggestion VARCHAR(255) NOT NULL,
    trigger_time DATETIME NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    INDEX idx_alert_vehicle_time (vehicle_code, trigger_time)
);

CREATE TABLE risk_assessments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicle_code VARCHAR(32) NOT NULL,
    risk_score DECIMAL(6,2) NOT NULL,
    risk_level VARCHAR(16) NOT NULL,
    risk_label VARCHAR(16) NOT NULL,
    risk_reason VARCHAR(255) NOT NULL,
    predicted_minutes_to_limit INT NULL,
    algorithm_version VARCHAR(32) NULL,
    algorithm_source VARCHAR(32) NULL,
    assessment_time DATETIME NOT NULL,
    INDEX idx_risk_vehicle_time (vehicle_code, assessment_time)
);

CREATE TABLE route_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicle_code VARCHAR(32) NOT NULL,
    plan_type VARCHAR(32) NOT NULL,
    plan_title VARCHAR(128) NOT NULL,
    plan_detail VARCHAR(255) NOT NULL,
    estimated_cost VARCHAR(128) NULL,
    estimated_benefit VARCHAR(128) NULL,
    recommended TINYINT(1) NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL,
    INDEX idx_route_plan_vehicle_time (vehicle_code, created_time)
);
