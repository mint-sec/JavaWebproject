package com.coldchain.backend.repository.mysql;

import com.coldchain.backend.entity.mysql.AlertRecordEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRecordJpaRepository extends JpaRepository<AlertRecordEntity, Long> {
    List<AlertRecordEntity> findByVehicleCodeOrderByTriggerTimeDesc(String vehicleCode);

    Optional<AlertRecordEntity> findByAlertId(String alertId);
}
