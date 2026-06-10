package com.coldchain.backend.service;

import com.coldchain.backend.entity.LoginLogRecord;
import com.coldchain.backend.entity.OperationLogRecord;
import com.coldchain.backend.repository.AuditLogRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private static final DateTimeFormatter ID_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<LoginLogRecord> listLoginLogs() {
        return auditLogRepository.findRecentLoginLogs();
    }

    public List<OperationLogRecord> listOperationLogs() {
        return auditLogRepository.findRecentOperationLogs();
    }

    public void appendLoginLog(String account, String roleLabel, String result, String ip, String detail) {
        LocalDateTime now = LocalDateTime.now();
        auditLogRepository.saveLoginLog(new LoginLogRecord(
                "LGN-" + now.format(ID_TIME),
                account,
                roleLabel,
                result,
                ip,
                detail,
                now));
    }

    public void appendOperationLog(String moduleName, String actionName, String operatorName, String targetName, String result, String detail) {
        LocalDateTime now = LocalDateTime.now();
        auditLogRepository.saveOperationLog(new OperationLogRecord(
                "OPR-" + now.format(ID_TIME),
                moduleName,
                actionName,
                operatorName,
                targetName,
                result,
                detail,
                now));
    }
}
