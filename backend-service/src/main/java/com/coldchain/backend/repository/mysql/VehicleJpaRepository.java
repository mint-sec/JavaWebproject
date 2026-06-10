package com.coldchain.backend.repository.mysql;

import com.coldchain.backend.entity.mysql.VehicleEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, Long> {
    Optional<VehicleEntity> findByVehicleCode(String vehicleCode);
    Optional<VehicleEntity> findByOwnerUserIdAndDisplayCode(String ownerUserId, String displayCode);
    List<VehicleEntity> findByOwnerUserIdOrderByVehicleCodeAsc(String ownerUserId);
}
