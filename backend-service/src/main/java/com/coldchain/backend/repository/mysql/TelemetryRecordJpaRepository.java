package com.coldchain.backend.repository.mysql;

import com.coldchain.backend.entity.mysql.TelemetryRecordEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryRecordJpaRepository extends JpaRepository<TelemetryRecordEntity, Long> {
    List<TelemetryRecordEntity> findByVehicleCodeOrderByRecordTimeAsc(String vehicleCode);

    Optional<TelemetryRecordEntity> findFirstByVehicleCodeOrderByRecordTimeDesc(String vehicleCode);
}
