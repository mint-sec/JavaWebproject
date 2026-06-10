package com.coldchain.backend.repository.mysql;

import com.coldchain.backend.entity.mysql.LoginLogEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginLogJpaRepository extends JpaRepository<LoginLogEntity, Long> {
    List<LoginLogEntity> findTop60ByOrderByCreatedAtDesc();
}
