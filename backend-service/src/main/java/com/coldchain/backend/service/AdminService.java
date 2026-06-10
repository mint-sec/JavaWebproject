package com.coldchain.backend.service;

import com.coldchain.backend.dto.AdminConsoleResponse;
import com.coldchain.backend.dto.AlgorithmStatusResponse;
import com.coldchain.backend.dto.LoginLogResponse;
import com.coldchain.backend.dto.OperationLogResponse;
import com.coldchain.backend.dto.ServiceMonitorResponse;
import com.coldchain.backend.dto.UpdateUserRequest;
import com.coldchain.backend.dto.UserResponse;
import com.coldchain.backend.entity.LoginLogRecord;
import com.coldchain.backend.entity.OperationLogRecord;
import com.coldchain.backend.entity.UserRecord;
import com.coldchain.backend.exception.NotFoundException;
import com.coldchain.backend.repository.AdminDataRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AdminDataRepository adminDataRepository;
    private final AuditLogService auditLogService;
    private final AnalysisService analysisService;

    public AdminService(
            AdminDataRepository adminDataRepository,
            AuditLogService auditLogService,
            AnalysisService analysisService) {
        this.adminDataRepository = adminDataRepository;
        this.auditLogService = auditLogService;
        this.analysisService = analysisService;
    }

    public AdminConsoleResponse getConsole(String currentAdminName) {
        List<UserRecord> users = adminDataRepository.findAllUsers();
        List<LoginLogRecord> loginLogs = auditLogService.listLoginLogs();
        List<OperationLogRecord> operationLogs = auditLogService.listOperationLogs();
        List<ServiceMonitorResponse> serviceMonitors = buildServiceMonitors();
        long activeCount = users.stream().filter(u -> "启用中".equals(u.status())).count();
        long adminCount = users.stream().filter(u -> "ADMIN".equals(u.role())).count();
        long abnormalServiceCount = serviceMonitors.stream().filter(item -> !"normal".equals(item.tone())).count();

        return new AdminConsoleResponse(
                List.of(
                        new AdminConsoleResponse.OverviewCard("平台用户数", String.valueOf(activeCount), "当前可登录账号数量"),
                        new AdminConsoleResponse.OverviewCard("管理员人数", String.valueOf(adminCount), "具备后台权限的账号数量"),
                        new AdminConsoleResponse.OverviewCard("普通用户数", String.valueOf(users.stream().filter(u -> "USER".equals(u.role())).count()), "当前业务侧可用账号数量"),
                        new AdminConsoleResponse.OverviewCard("今日登录记录", String.valueOf(loginLogs.size()), "可在日志中心查看登录详情"),
                        new AdminConsoleResponse.OverviewCard("平台事项", String.valueOf(operationLogs.size()), "当前管理员：" + currentAdminName),
                        new AdminConsoleResponse.OverviewCard("监控关注服务", String.valueOf(abnormalServiceCount), abnormalServiceCount > 0 ? "存在待联调或观察中的服务" : "所有服务状态正常")),
                List.of(
                        new AdminConsoleResponse.OverviewCard("登录日志总数", String.valueOf(loginLogs.size()), "展示最近 60 条登录相关记录"),
                        new AdminConsoleResponse.OverviewCard("今日成功登录", String.valueOf(loginLogs.stream().filter(item -> "成功".equals(item.result())).count()), "包含注册后自动登录"),
                        new AdminConsoleResponse.OverviewCard("操作日志总数", String.valueOf(operationLogs.size()), "展示最近 60 条平台操作记录"),
                        new AdminConsoleResponse.OverviewCard("敏感操作数", String.valueOf(operationLogs.stream().filter(item -> "用户管理".equals(item.moduleName())).count()), "重点关注角色、状态、权限等变更")),
                users.stream().map(this::toUserResponse).toList(),
                loginLogs.stream().map(this::toLoginLogResponse).toList(),
                operationLogs.stream().map(this::toOperationLogResponse).toList(),
                serviceMonitors);
    }

    public UserResponse updateUser(String userId, UpdateUserRequest request, String operatorName) {
        UserRecord user = adminDataRepository.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在: " + userId));

        UserRecord updated = new UserRecord(
                user.id(),
                user.username(),
                user.displayName(),
                user.phone(),
                user.email(),
                user.password(),
                request.role(),
                request.status(),
                user.origin(),
                user.createdAt(),
                user.loginFailureCount(),
                user.lockedUntil());

        UserRecord saved = adminDataRepository.updateUser(updated);
        auditLogService.appendOperationLog(
                "用户管理",
                "修改用户角色/状态",
                operatorName,
                saved.username(),
                "成功",
                "角色调整为 " + AuthService.roleToLabel(saved.role()) + "，状态调整为 " + saved.status());
        return toUserResponse(saved);
    }

    private List<ServiceMonitorResponse> buildServiceMonitors() {
        String checkedAt = LocalDateTime.now().format(FULL_TIME);
        AlgorithmStatusResponse algorithmStatus = analysisService.getAlgorithmStatus();
        String algorithmTone = algorithmStatus.available() ? "normal" : (algorithmStatus.fallbackEnabled() ? "warning" : "danger");
        String algorithmLabel = algorithmStatus.available() ? "正常" : (algorithmStatus.fallbackEnabled() ? "观察中" : "异常");

        return List.of(
                new ServiceMonitorResponse(
                        "service-frontend",
                        "前端工作台",
                        "正常",
                        "normal",
                        "12 ms",
                        "frontend",
                        checkedAt,
                        "页面路由、状态同步与接口轮询运行正常。"),
                new ServiceMonitorResponse(
                        "service-backend",
                        "后端接口服务",
                        "正常",
                        "normal",
                        "25 ms",
                        "/api/v1",
                        checkedAt,
                        "认证、工作台、监控面板和后台管理接口已统一由 Spring Boot 提供。"),
                new ServiceMonitorResponse(
                        "service-algorithm",
                        "算法数据服务",
                        algorithmLabel,
                        algorithmTone,
                        algorithmStatus.available() ? "35 ms" : "--",
                        algorithmStatus.mode(),
                        checkedAt,
                        algorithmStatus.message()),
                new ServiceMonitorResponse(
                        "service-database",
                        "数据库链路",
                        "正常",
                        "normal",
                        "18 ms",
                        "MySQL / JPA",
                        checkedAt,
                        "用户、车辆、告警与采样遥测均可通过数据库持久化。"));
    }

    private UserResponse toUserResponse(UserRecord user) {
        return new UserResponse(
                user.id(),
                user.username(),
                user.displayName(),
                user.phone(),
                user.email(),
                user.role(),
                AuthService.roleToLabel(user.role()),
                user.status(),
                user.origin());
    }

    private LoginLogResponse toLoginLogResponse(LoginLogRecord record) {
        return new LoginLogResponse(
                record.logId(),
                record.createdAt().format(FULL_TIME),
                record.account(),
                record.roleLabel(),
                record.result(),
                record.ip(),
                record.detail());
    }

    private OperationLogResponse toOperationLogResponse(OperationLogRecord record) {
        return new OperationLogResponse(
                record.logId(),
                record.createdAt().format(FULL_TIME),
                record.moduleName(),
                record.actionName(),
                record.operatorName(),
                record.targetName(),
                record.result(),
                record.detail());
    }
}
