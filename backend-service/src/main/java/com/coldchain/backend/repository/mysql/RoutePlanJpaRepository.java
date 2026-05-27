package com.coldchain.backend.repository.mysql;

import com.coldchain.backend.entity.mysql.RoutePlanEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutePlanJpaRepository extends JpaRepository<RoutePlanEntity, Long> {
    List<RoutePlanEntity> findByVehicleCodeOrderByCreatedTimeDesc(String vehicleCode);

    Optional<RoutePlanEntity> findFirstByVehicleCodeOrderByCreatedTimeDesc(String vehicleCode);
}
