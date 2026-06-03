package com.coldchain.backend.service;

import com.coldchain.backend.dto.AdminAlertResponse;
import com.coldchain.backend.dto.AdminAlertUpdateRequest;
import com.coldchain.backend.dto.AdminConsoleResponse;
import com.coldchain.backend.dto.AdminConsoleResponse.OverviewCard;
import com.coldchain.backend.dto.AdminVehicleRequest;
import com.coldchain.backend.dto.AdminVehicleResponse;
import com.coldchain.backend.dto.UpdateUserRequest;
import com.coldchain.backend.dto.UserResponse;
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
public class AdminService {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AdminDataRepository adminDataRepository;
    private final MockDataRepository mockDataRepository;

    public AdminService(AdminDataRepository adminDataRepository, MockDataRepository mockDataRepository) {
        this.adminDataRepository = adminDataRepository;
        this.mockDataRepository = mockDataRepository;
    }

    public AdminConsoleResponse getConsole() {
        List<UserRecord> users = adminDataRepository.findAllUsers();
        List<Vehicle> vehicles = mockDataRepository.findAllVehicles();
        List<AlertRecord> alerts = mockDataRepository.findAllAlerts();

        long activeCount = users.stream().filter(u -> "启用中".equals(u.status())).count();
        long adminCount = users.stream().filter(u -> "ADMIN".equals(u.role())).count();
        long pendingAlertCount = alerts.stream().filter(a -> "待处理".equals(a.processStatus())).count();

        List<OverviewCard> cards = List.of(
                new OverviewCard("活跃用户数", String.valueOf(activeCount), "当前可登录账号数量"),
                new OverviewCard("管理员人数", String.valueOf(adminCount), "具备后台权限的账号数量"),
                new OverviewCard("车辆总数", String.valueOf(vehicles.size()), "已纳入管理的车辆数量"),
                new OverviewCard("待处理告警", String.valueOf(pendingAlertCount), "当前管理员：admin"));

        return new AdminConsoleResponse(
                cards,
                users.stream().map(this::toUserResponse).toList(),
                vehicles.stream().map(this::toAdminVehicleResponse).toList(),
                alerts.stream().map(this::toAdminAlertResponse).toList());
    }

    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        UserRecord user = adminDataRepository.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在: " + userId));

        UserRecord updated = new UserRecord(
                user.id(), user.username(), user.displayName(),
                user.phone(), user.email(), user.password(),
                request.role(), request.status(), user.origin(), user.createdAt());

        return toUserResponse(adminDataRepository.updateUser(updated));
    }

    public AdminVehicleResponse createVehicle(AdminVehicleRequest request) {
        if (mockDataRepository.findVehicleByCode(request.vehicleId()).isPresent()) {
            throw new AuthException(400, "车辆编号已存在: " + request.vehicleId());
        }

        Vehicle vehicle = new Vehicle(
                null, request.vehicleId(), "", "", request.cargoName(),
                2.0, 8.0, statusToInternal(request.status()),
                request.driver(), request.route(), LocalDateTime.now());

        mockDataRepository.saveVehicle(vehicle);
        return toAdminVehicleResponse(vehicle);
    }

    public AdminVehicleResponse updateVehicle(String vehicleId, AdminVehicleRequest request) {
        Vehicle existing = mockDataRepository.findVehicleByCode(vehicleId)
                .orElseThrow(() -> new NotFoundException("车辆不存在: " + vehicleId));

        if (!vehicleId.equals(request.vehicleId())
                && mockDataRepository.findVehicleByCode(request.vehicleId()).isPresent()) {
            throw new AuthException(400, "车辆编号已存在: " + request.vehicleId());
        }

        Vehicle updated = new Vehicle(
                existing.id(), request.vehicleId(), existing.plateNumber(),
                existing.cargoType(), request.cargoName(),
                existing.safeTempMin(), existing.safeTempMax(),
                statusToInternal(request.status()),
                request.driver(), request.route(), LocalDateTime.now());

        return toAdminVehicleResponse(mockDataRepository.updateVehicle(updated));
    }

    public void deleteVehicle(String vehicleId) {
        if (mockDataRepository.findVehicleByCode(vehicleId).isEmpty()) {
            throw new NotFoundException("车辆不存在: " + vehicleId);
        }
        mockDataRepository.deleteVehicle(vehicleId);
    }

    public AdminAlertResponse updateAlert(String alertId, AdminAlertUpdateRequest request) {
        AlertRecord alert = mockDataRepository.findAlertById(alertId)
                .orElseThrow(() -> new NotFoundException("告警不存在: " + alertId));

        LocalDateTime handledAt = "已处理".equals(request.status()) ? LocalDateTime.now() : null;

        AlertRecord updated = new AlertRecord(
                alert.alertId(), alert.vehicleCode(), request.level(),
                alert.alertType(), alert.title(), alert.detail(),
                alert.suggestion(), alert.triggerTime(), alert.status(),
                request.owner(), request.status(),
                request.note() != null ? request.note() : "",
                handledAt);

        return toAdminAlertResponse(mockDataRepository.updateAlert(updated));
    }

    private UserResponse toUserResponse(UserRecord u) {
        return new UserResponse(u.id(), u.username(), u.displayName(),
                u.phone(), u.email(), u.role(),
                AuthService.roleToLabel(u.role()), u.status(), u.origin());
    }

    private AdminVehicleResponse toAdminVehicleResponse(Vehicle v) {
        return new AdminVehicleResponse(v.vehicleCode(), v.cargoName(),
                statusToDisplay(v.status()), v.driver(), v.route(),
                v.updatedAt() != null ? v.updatedAt().format(FULL_TIME) : "");
    }

    private AdminAlertResponse toAdminAlertResponse(AlertRecord a) {
        String processStatus = a.processStatus().isEmpty() ? "待处理" : a.processStatus();
        return new AdminAlertResponse(a.alertId(), a.title(), a.level(),
                levelToLabel(a.level()), a.detail(), a.vehicleCode(),
                a.owner(), processStatus, a.note(),
                a.handledAt() != null ? a.handledAt().format(FULL_TIME) : "");
    }

    static String levelToLabel(String level) {
        return switch (level) {
            case "HIGH" -> "高";
            case "MEDIUM" -> "中";
            case "LOW" -> "低";
            default -> level;
        };
    }

    static String statusToDisplay(String internal) {
        return switch (internal) {
            case "IN_TRANSIT" -> "运输中";
            case "STANDBY" -> "待命中";
            case "MAINTENANCE" -> "维修中";
            case "DECOMMISSIONED" -> "已停用";
            default -> internal;
        };
    }

    static String statusToInternal(String display) {
        return switch (display) {
            case "运输中" -> "IN_TRANSIT";
            case "待命中" -> "STANDBY";
            case "维修中" -> "MAINTENANCE";
            case "已停用" -> "DECOMMISSIONED";
            default -> display;
        };
    }
}
