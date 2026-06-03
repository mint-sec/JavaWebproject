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
    private final AdminDataRepository adminDataRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(AdminDataRepository adminDataRepository) {
        this.adminDataRepository = adminDataRepository;
    }

    public SessionResponse login(LoginRequest request) {
        UserRecord user = adminDataRepository.findUserByUsername(request.account())
                .orElseThrow(() -> new AuthException(401, "用户名或密码错误"));

        if ("已封禁".equals(user.status())) {
            throw new AuthException(403, "账号已被封禁，请联系管理员");
        }

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw new AuthException(401, "用户名或密码错误");
        }

        return buildSession(user);
    }

    public SessionResponse register(RegisterRequest request) {
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

        String userId = "USR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
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
                LocalDateTime.now());

        adminDataRepository.saveUser(user);
        return buildSession(user);
    }

    public SessionResponse me(String userId) {
        UserRecord user = adminDataRepository.findUserById(userId)
                .orElseThrow(() -> new AuthException(401, "用户不存在或已被删除"));
        return buildSession(user);
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

    static String roleToLabel(String role) {
        return "ADMIN".equals(role) ? "管理员" : "普通用户";
    }
}
