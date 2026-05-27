package com.coldchain.backend.repository.mysql;

import com.coldchain.backend.entity.mysql.RiskAssessmentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAssessmentJpaRepository extends JpaRepository<RiskAssessmentEntity, Long> {
    List<RiskAssessmentEntity> findByVehicleCodeOrderByAssessmentTimeDesc(String vehicleCode);

    Optional<RiskAssessmentEntity> findFirstByVehicleCodeOrderByAssessmentTimeDesc(String vehicleCode);
}
