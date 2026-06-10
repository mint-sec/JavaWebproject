package com.coldchain.backend.repository;

import com.coldchain.backend.config.DataSourceModeProperties;
import com.coldchain.backend.entity.LoginLogRecord;
import com.coldchain.backend.entity.OperationLogRecord;
import com.coldchain.backend.entity.mysql.LoginLogEntity;
import com.coldchain.backend.entity.mysql.OperationLogEntity;
import com.coldchain.backend.repository.mysql.LoginLogJpaRepository;
import com.coldchain.backend.repository.mysql.OperationLogJpaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogRepository {
    private final DataSourceModeProperties dataSourceModeProperties;
    private final Optional<LoginLogJpaRepository> loginLogJpaRepository;
    private final Optional<OperationLogJpaRepository> operationLogJpaRepository;

    private final List<LoginLogRecord> loginLogs = new ArrayList<>();
    private final List<OperationLogRecord> operationLogs = new ArrayList<>();

    public AuditLogRepository(
            DataSourceModeProperties dataSourceModeProperties,
            Optional<LoginLogJpaRepository> loginLogJpaRepository,
            Optional<OperationLogJpaRepository> operationLogJpaRepository) {
        this.dataSourceModeProperties = dataSourceModeProperties;
        this.loginLogJpaRepository = loginLogJpaRepository;
        this.operationLogJpaRepository = operationLogJpaRepository;
        initLogs();
    }

    public List<LoginLogRecord> findRecentLoginLogs() {
        if (dataSourceModeProperties.useMysql()) {
            return loginLogJpaRepository.orElseThrow().findTop60ByOrderByCreatedAtDesc().stream()
                    .map(this::toLoginLogRecord)
                    .toList();
        }
        return loginLogs.stream()
                .sorted(Comparator.comparing(LoginLogRecord::createdAt).reversed())
                .limit(60)
                .toList();
    }

    public List<OperationLogRecord> findRecentOperationLogs() {
        if (dataSourceModeProperties.useMysql()) {
            return operationLogJpaRepository.orElseThrow().findTop60ByOrderByCreatedAtDesc().stream()
                    .map(this::toOperationLogRecord)
                    .toList();
        }
        return operationLogs.stream()
                .sorted(Comparator.comparing(OperationLogRecord::createdAt).reversed())
                .limit(60)
                .toList();
    }

    public LoginLogRecord saveLoginLog(LoginLogRecord record) {
        if (dataSourceModeProperties.useMysql()) {
            LoginLogEntity entity = new LoginLogEntity();
            entity.setLogId(record.logId());
            entity.setAccount(record.account());
            entity.setRoleLabel(record.roleLabel());
            entity.setResult(record.result());
            entity.setIp(record.ip());
            entity.setDetail(record.detail());
            entity.setCreatedAt(record.createdAt());
            return toLoginLogRecord(loginLogJpaRepository.orElseThrow().save(entity));
        }
        loginLogs.add(record);
        return record;
    }

    public OperationLogRecord saveOperationLog(OperationLogRecord record) {
        if (dataSourceModeProperties.useMysql()) {
            OperationLogEntity entity = new OperationLogEntity();
            entity.setLogId(record.logId());
            entity.setModuleName(record.moduleName());
            entity.setActionName(record.actionName());
            entity.setOperatorName(record.operatorName());
            entity.setTargetName(record.targetName());
            entity.setResult(record.result());
            entity.setDetail(record.detail());
            entity.setCreatedAt(record.createdAt());
            return toOperationLogRecord(operationLogJpaRepository.orElseThrow().save(entity));
        }
        operationLogs.add(record);
        return record;
    }

    private void initLogs() {
        loginLogs.add(new LoginLogRecord("LGN-20260603-001", "admin", "管理员", "成功", "127.0.0.1", "本地开发环境登录", LocalDateTime.of(2026, 6, 3, 9, 8, 12)));
        loginLogs.add(new LoginLogRecord("LGN-20260603-002", "operator", "普通用户", "成功", "127.0.0.1", "业务用户登录工作台", LocalDateTime.of(2026, 6, 3, 9, 16, 45)));

        operationLogs.add(new OperationLogRecord("OPR-20260603-001", "用户管理", "修改用户角色", "admin", "operator", "成功", "将用户角色调整为普通用户", LocalDateTime.of(2026, 6, 3, 9, 22, 33)));
    }

    private LoginLogRecord toLoginLogRecord(LoginLogEntity entity) {
        return new LoginLogRecord(
                entity.getLogId(),
                entity.getAccount(),
                entity.getRoleLabel(),
                entity.getResult(),
                entity.getIp(),
                entity.getDetail(),
                entity.getCreatedAt());
    }

    private OperationLogRecord toOperationLogRecord(OperationLogEntity entity) {
        return new OperationLogRecord(
                entity.getLogId(),
                entity.getModuleName(),
                entity.getActionName(),
                entity.getOperatorName(),
                entity.getTargetName(),
                entity.getResult(),
                entity.getDetail(),
                entity.getCreatedAt());
    }
}
