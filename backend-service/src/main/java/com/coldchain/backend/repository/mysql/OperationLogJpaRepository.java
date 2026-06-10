package com.coldchain.backend.repository.mysql;

import com.coldchain.backend.entity.mysql.OperationLogEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationLogJpaRepository extends JpaRepository<OperationLogEntity, Long> {
    List<OperationLogEntity> findTop60ByOrderByCreatedAtDesc();
}
