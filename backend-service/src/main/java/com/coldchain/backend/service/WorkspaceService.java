package com.coldchain.backend.service;

import com.coldchain.backend.dto.AdminAlertUpdateRequest;
import com.coldchain.backend.dto.AdminVehicleRequest;
import com.coldchain.backend.dto.WorkspaceAlertResponse;
import com.coldchain.backend.dto.WorkspaceConsoleResponse;
import com.coldchain.backend.dto.WorkspaceVehicleResponse;
import com.coldchain.backend.entity.AlertRecord;
import com.coldchain.backend.entity.UserRecord;
import com.coldchain.backend.entity.Vehicle;
import com.coldchain.backend.exception.AuthException;
import com.coldchain.backend.exception.NotFoundException;
import com.coldchain.backend.repository.AdminDataRepository;
import com.coldchain.backend.repository.MockDataRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceService {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter VEHICLE_KEY_TIME = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final AdminDataRepository adminDataRepository;
    private final MockDataRepository mockDataRepository;
    private final AuditLogService auditLogService;

    public WorkspaceService(
            AdminDataRepository adminDataRepository,
            MockDataRepository mockDataRepository,
            AuditLogService auditLogService) {
        this.adminDataRepository = adminDataRepository;
        this.mockDataRepository = mockDataRepository;
        this.auditLogService = auditLogService;
    }

    public WorkspaceConsoleResponse getConsole(String userId) {
        UserRecord currentUser = requireUser(userId);
        List<WorkspaceVehicleResponse> vehicles = mockDataRepository.findVehiclesByOwnerUserId(userId).stream()
                .map(this::toWorkspaceVehicleResponse)
                .toList();
        List<WorkspaceAlertResponse> alerts = mockDataRepository.findAlertsByOwnerUserId(userId, "BUSINESS").stream()
                .map(this::toWorkspaceAlertResponse)
                .toList();

        long pendingCount = alerts.stream().filter(item -> "待处理".equals(item.status())).count();
        long processingCount = alerts.stream().filter(item -> "处理中".equals(item.status())).count();
        long handledCount = alerts.stream().filter(item -> "已处理".equals(item.status())).count();

        return new WorkspaceConsoleResponse(
                List.of(
                        new WorkspaceConsoleResponse.OverviewCard("我的车辆", String.valueOf(vehicles.size()), "当前归属于该账号的车辆数量"),
                        new WorkspaceConsoleResponse.OverviewCard("待处理告警", String.valueOf(pendingCount), "需要尽快跟进的业务告警"),
                        new WorkspaceConsoleResponse.OverviewCard("处理中告警", String.valueOf(processingCount), "已经接手但尚未闭环的业务告警"),
                        new WorkspaceConsoleResponse.OverviewCard("已处理告警", String.valueOf(handledCount), "当前责任人：" + currentUser.displayName())),
                vehicles,
                alerts);
    }

    public WorkspaceVehicleResponse createVehicle(String userId, AdminVehicleRequest request) {
        UserRecord currentUser = requireUser(userId);
        String displayCode = request.vehicleId().trim();
        if (mockDataRepository.findVehicleByOwnerUserIdAndDisplayCode(userId, displayCode).isPresent()) {
            throw new AuthException(400, "当前账号下已存在车辆编号：" + displayCode);
        }

        Vehicle vehicle = new Vehicle(
                null,
                buildVehicleCode(currentUser.id(), displayCode),
                displayCode,
                "",
                "VACCINE",
                request.cargoName().trim(),
                2.0,
                8.0,
                toInternalStatus(request.status()),
                currentUser.id(),
                request.driver().trim(),
                request.route().trim(),
                request.routeDistanceKm(),
                LocalDateTime.now());

        WorkspaceVehicleResponse response = toWorkspaceVehicleResponse(mockDataRepository.saveVehicle(vehicle));
        auditLogService.appendOperationLog("我的车辆", "新增车辆", currentUser.username(), response.vehicleId(), "成功", "业务用户新增本人车辆");
        return response;
    }

    public WorkspaceVehicleResponse updateVehicle(String userId, String vehicleKey, AdminVehicleRequest request) {
        UserRecord currentUser = requireUser(userId);
        Vehicle existing = findOwnedVehicle(userId, vehicleKey);
        String nextDisplayCode = request.vehicleId().trim();

        if (!existing.displayCode().equalsIgnoreCase(nextDisplayCode)
                && mockDataRepository.findVehicleByOwnerUserIdAndDisplayCode(userId, nextDisplayCode).isPresent()) {
            throw new AuthException(400, "当前账号下已存在车辆编号：" + nextDisplayCode);
        }

        Vehicle updated = new Vehicle(
                existing.id(),
                existing.vehicleCode(),
                nextDisplayCode,
                existing.plateNumber(),
                existing.cargoType(),
                request.cargoName().trim(),
                existing.safeTempMin(),
                existing.safeTempMax(),
                toInternalStatus(request.status()),
                existing.ownerUserId(),
                request.driver().trim(),
                request.route().trim(),
                request.routeDistanceKm(),
                LocalDateTime.now());

        WorkspaceVehicleResponse response = toWorkspaceVehicleResponse(mockDataRepository.updateVehicle(updated));
        auditLogService.appendOperationLog("我的车辆", "编辑车辆", currentUser.username(), response.vehicleId(), "成功", "业务用户更新本人车辆信息");
        return response;
    }

    public void deleteVehicle(String userId, String vehicleKey) {
        UserRecord currentUser = requireUser(userId);
        Vehicle existing = findOwnedVehicle(userId, vehicleKey);
        mockDataRepository.deleteVehicle(existing.vehicleCode());
        auditLogService.appendOperationLog("我的车辆", "删除车辆", currentUser.username(), existing.displayCode(), "成功", "业务用户删除本人车辆");
    }

    public WorkspaceAlertResponse updateAlert(String userId, String alertId, AdminAlertUpdateRequest request) {
        UserRecord currentUser = requireUser(userId);
        AlertRecord alert = mockDataRepository.findAlertById(alertId)
                .orElseThrow(() -> new NotFoundException("告警不存在：" + alertId));

        if (!userId.equals(alert.ownerUserId())) {
            throw new AuthException(403, "只能处理分配给当前账号的告警");
        }
        if (!"BUSINESS".equalsIgnoreCase(alert.domain())) {
            throw new AuthException(403, "平台事项不能在业务工作台中处理");
        }

        String processStatus = request.status().trim();
        LocalDateTime handledAt = "已处理".equals(processStatus) ? LocalDateTime.now() : alert.handledAt();
        AlertRecord updated = new AlertRecord(
                alert.alertId(),
                alert.vehicleCode(),
                request.level().trim(),
                alert.alertType(),
                alert.title(),
                alert.detail(),
                alert.suggestion(),
                alert.triggerTime(),
                alert.status(),
                request.owner().trim(),
                alert.ownerUserId(),
                processStatus,
                request.note() == null ? "" : request.note().trim(),
                handledAt,
                alert.domain());

        WorkspaceAlertResponse response = toWorkspaceAlertResponse(mockDataRepository.updateAlert(updated));
        auditLogService.appendOperationLog("我的告警", "处理告警", currentUser.username(), response.id(), "成功", "业务用户更新告警处理结果");
        return response;
    }

    private UserRecord requireUser(String userId) {
        return adminDataRepository.findUserById(userId)
                .orElseThrow(() -> new AuthException(401, "用户不存在或登录已失效"));
    }

    private Vehicle findOwnedVehicle(String userId, String vehicleKey) {
        Vehicle vehicle = mockDataRepository.findVehicleByCode(vehicleKey)
                .orElseThrow(() -> new NotFoundException("车辆不存在：" + vehicleKey));
        if (!userId.equals(vehicle.ownerUserId())) {
            throw new AuthException(403, "只能操作当前账号名下的车辆");
        }
        return vehicle;
    }

    private WorkspaceVehicleResponse toWorkspaceVehicleResponse(Vehicle vehicle) {
        String ownerName = adminDataRepository.findUserById(vehicle.ownerUserId())
                .map(UserRecord::displayName)
                .orElse("");
        return new WorkspaceVehicleResponse(
                vehicle.vehicleCode(),
                vehicle.displayCode(),
                vehicle.cargoName(),
                toDisplayStatus(vehicle.status()),
                vehicle.driver(),
                vehicle.route(),
                vehicle.routeDistanceKm(),
                vehicle.ownerUserId(),
                ownerName,
                vehicle.updatedAt() == null ? "" : vehicle.updatedAt().format(FULL_TIME));
    }

    private WorkspaceAlertResponse toWorkspaceAlertResponse(AlertRecord alert) {
        String vehicleDisplayCode = mockDataRepository.findVehicleByCode(alert.vehicleCode())
                .map(Vehicle::displayCode)
                .orElse(alert.vehicleCode());
        return new WorkspaceAlertResponse(
                alert.alertId(),
                alert.title(),
                alert.level(),
                levelToLabel(alert.level()),
                alert.detail(),
                vehicleDisplayCode,
                alert.owner(),
                alert.ownerUserId(),
                alert.processStatus().isBlank() ? "待处理" : alert.processStatus(),
                alert.note(),
                alert.domain(),
                alert.handledAt() == null ? "" : alert.handledAt().format(FULL_TIME));
    }

    private String buildVehicleCode(String ownerUserId, String displayCode) {
        String normalizedOwner = ownerUserId.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        String normalizedDisplay = displayCode.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        String ownerPart = normalizedOwner.length() <= 6 ? normalizedOwner : normalizedOwner.substring(normalizedOwner.length() - 6);
        String displayPart;
        if (normalizedDisplay.isBlank()) {
            displayPart = "VEHICLE";
        } else {
            displayPart = normalizedDisplay.length() <= 12 ? normalizedDisplay : normalizedDisplay.substring(0, 12);
        }
        return ownerPart + "-" + displayPart + "-" + LocalDateTime.now().format(VEHICLE_KEY_TIME);
    }

    public static String toDisplayStatus(String internal) {
        return switch (internal) {
            case "IN_TRANSIT" -> "运输中";
            case "STANDBY", "IDLE" -> "待命中";
            case "MAINTENANCE" -> "维修中";
            case "DECOMMISSIONED", "OFFLINE" -> "已停用";
            default -> internal;
        };
    }

    public static String toInternalStatus(String display) {
        return switch (display) {
            case "运输中" -> "IN_TRANSIT";
            case "待命中" -> "IDLE";
            case "维修中" -> "MAINTENANCE";
            case "已停用" -> "OFFLINE";
            default -> display;
        };
    }

    public static String levelToLabel(String level) {
        return switch (level) {
            case "HIGH" -> "高风险";
            case "MEDIUM" -> "中风险";
            case "LOW" -> "低风险";
            default -> level;
        };
    }
}
