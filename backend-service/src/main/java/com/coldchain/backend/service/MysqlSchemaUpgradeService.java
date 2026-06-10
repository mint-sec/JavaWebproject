package com.coldchain.backend.service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("mysql")
public class MysqlSchemaUpgradeService {
    private final JdbcTemplate jdbcTemplate;

    public MysqlSchemaUpgradeService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void upgrade() {
        ensureColumn("vehicles", "display_code", "ALTER TABLE vehicles ADD COLUMN display_code VARCHAR(32) NOT NULL DEFAULT '' AFTER vehicle_code");
        jdbcTemplate.update("UPDATE vehicles SET display_code = vehicle_code WHERE display_code = '' OR display_code IS NULL");

        ensureColumn("vehicles", "route_distance_km", "ALTER TABLE vehicles ADD COLUMN route_distance_km DECIMAL(8,2) NOT NULL DEFAULT 30.00 AFTER route");
        jdbcTemplate.update("UPDATE vehicles SET route_distance_km = 30.00 WHERE route_distance_km IS NULL OR route_distance_km <= 0");

        ensureColumn("users", "login_failure_count", "ALTER TABLE users ADD COLUMN login_failure_count INT NOT NULL DEFAULT 0 AFTER created_at");
        jdbcTemplate.update("UPDATE users SET login_failure_count = 0 WHERE login_failure_count IS NULL OR login_failure_count < 0");

        ensureColumn("users", "locked_until", "ALTER TABLE users ADD COLUMN locked_until DATETIME NULL AFTER login_failure_count");

        Integer uniqueCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'vehicles' AND index_name = 'uk_vehicle_owner_display_code'",
                Integer.class);
        if (uniqueCount == null || uniqueCount == 0) {
            jdbcTemplate.execute("ALTER TABLE vehicles ADD UNIQUE INDEX uk_vehicle_owner_display_code (owner_user_id, display_code)");
        }
    }

    private void ensureColumn(String tableName, String columnName, String ddl) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName);
        if (count == null || count == 0) {
            jdbcTemplate.execute(ddl);
        }
    }
}
