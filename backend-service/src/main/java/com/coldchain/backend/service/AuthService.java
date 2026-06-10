package com.coldchain.backend.service;

import com.coldchain.backend.config.JwtUtil;
import com.coldchain.backend.dto.LoginRequest;
import com.coldchain.backend.dto.RegisterRequest;
import com.coldchain.backend.dto.SessionResponse;
import com.coldchain.backend.entity.UserRecord;
import com.coldchain.backend.exception.AuthException;
import com.coldchain.backend.repository.AdminDataRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter USER_ID_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int MAX_LOGIN_FAILURES = 5;
    private static final int LOCK_MINUTES = 15;

    private final AdminDataRepository adminDataRepository;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(AdminDataRepository adminDataRepository, AuditLogService auditLogService) {
        this.adminDataRepository = adminDataRepository;
        this.auditLogService = auditLogService;
    }

    public SessionResponse login(LoginRequest request, String ipAddress) {
        String account = request.account().trim();
        LocalDateTime now = LocalDateTime.now();
        UserRecord user = adminDataRepository.findUserByUsername(account)
                .orElseThrow(() -> {
                    auditLogService.appendLoginLog(account, "未知", "失败", ipAddress, "用户名或密码错误");
                    return new AuthException(401, "用户名或密码错误");
                });

        user = releaseExpiredLockIfNecessary(user, now);

        if ("已封禁".equals(user.status())) {
            auditLogService.appendLoginLog(user.username(), roleToLabel(user.role()), "失败", ipAddress, "账号已被封禁，登录被拒绝");
            throw new AuthException(403, "账号已被封禁，请联系管理员");
        }

        if (isLocked(user, now)) {
            String lockedUntil = user.lockedUntil().format(FULL_TIME);
            auditLogService.appendLoginLog(user.username(), roleToLabel(user.role()), "失败", ipAddress, "登录失败次数过多，账号冻结至 " + lockedUntil);
            throw new AuthException(423, "登录失败次数过多，账号已冻结至 " + lockedUntil + "，请稍后再试");
        }

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw buildLoginFailure(user, ipAddress, now);
        }

        user = clearLoginFailures(user);
        auditLogService.appendLoginLog(user.username(), roleToLabel(user.role()), "成功", ipAddress, "登录成功");
        return buildSession(user);
    }

    public SessionResponse register(RegisterRequest request, String ipAddress) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new AuthException(400, "两次密码不一致");
        }

        if (adminDataRepository.existsByUsername(request.username())) {
            throw new AuthException(400, "用户名已存在");
        }

        if (adminDataRepository.existsByPhone(request.phone())) {
            throw new AuthException(400, "手机号已被注册");
        }

        if (adminDataRepository.existsByEmail(request.email())) {
            throw new AuthException(400, "邮箱已被注册");
        }

        String userId = "USR-" + LocalDateTime.now().format(USER_ID_TIME);
        UserRecord user = new UserRecord(
                userId,
                request.username(),
                request.username(),
                request.phone(),
                request.email(),
                passwordEncoder.encode(request.password()),
                "USER",
                "启用中",
                "用户注册",
                LocalDateTime.now(),
                0,
                null);

        adminDataRepository.saveUser(user);
        auditLogService.appendLoginLog(user.username(), "普通用户", "成功", ipAddress, "注册成功并自动登录");
        return buildSession(user);
    }

    public SessionResponse me(String userId) {
        UserRecord user = adminDataRepository.findUserById(userId)
                .orElseThrow(() -> new AuthException(401, "用户不存在或已被删除"));
        if ("已封禁".equals(user.status())) {
            throw new AuthException(403, "账号已被封禁，请联系管理员");
        }
        return buildSession(user);
    }

    public void logout(String username, String role, String ipAddress) {
        if (username == null || username.isBlank()) {
            return;
        }
        auditLogService.appendLoginLog(username, roleToLabel(role), "退出", ipAddress, "用户主动退出登录");
    }

    private SessionResponse buildSession(UserRecord user) {
        String token = JwtUtil.generateToken(user.id(), user.username(), user.role());
        return new SessionResponse(
                user.id(),
                user.username(),
                user.displayName(),
                user.role(),
                roleToLabel(user.role()),
                token,
                LocalDateTime.now().format(FULL_TIME));
    }

    private AuthException buildLoginFailure(UserRecord user, String ipAddress, LocalDateTime now) {
        int nextFailureCount = user.loginFailureCount() + 1;
        if (nextFailureCount >= MAX_LOGIN_FAILURES) {
            LocalDateTime lockedUntil = now.plusMinutes(LOCK_MINUTES);
            UserRecord lockedUser = updateUserSecurityState(user, nextFailureCount, lockedUntil);
            auditLogService.appendLoginLog(
                    lockedUser.username(),
                    roleToLabel(lockedUser.role()),
                    "失败",
                    ipAddress,
                    "连续登录失败 " + nextFailureCount + " 次，账号冻结至 " + lockedUntil.format(FULL_TIME));
            return new AuthException(423, "连续登录失败 5 次，账号已冻结 15 分钟，请稍后再试");
        }

        updateUserSecurityState(user, nextFailureCount, null);
        int remainingAttempts = MAX_LOGIN_FAILURES - nextFailureCount;
        auditLogService.appendLoginLog(
                user.username(),
                roleToLabel(user.role()),
                "失败",
                ipAddress,
                "用户名或密码错误，还可尝试 " + remainingAttempts + " 次");
        return new AuthException(401, "用户名或密码错误，还可尝试 " + remainingAttempts + " 次");
    }

    private UserRecord releaseExpiredLockIfNecessary(UserRecord user, LocalDateTime now) {
        if (user.lockedUntil() != null && !user.lockedUntil().isAfter(now)) {
            return clearLoginFailures(user);
        }
        return user;
    }

    private UserRecord clearLoginFailures(UserRecord user) {
        if (user.loginFailureCount() == 0 && user.lockedUntil() == null) {
            return user;
        }
        return updateUserSecurityState(user, 0, null);
    }

    private boolean isLocked(UserRecord user, LocalDateTime now) {
        return user.lockedUntil() != null && user.lockedUntil().isAfter(now);
    }

    private UserRecord updateUserSecurityState(UserRecord user, int loginFailureCount, LocalDateTime lockedUntil) {
        UserRecord updated = new UserRecord(
                user.id(),
                user.username(),
                user.displayName(),
                user.phone(),
                user.email(),
                user.password(),
                user.role(),
                user.status(),
                user.origin(),
                user.createdAt(),
                loginFailureCount,
                lockedUntil);
        return adminDataRepository.updateUser(updated);
    }

    static String roleToLabel(String role) {
        return "ADMIN".equals(role) ? "管理员" : "普通用户";
    }
}
