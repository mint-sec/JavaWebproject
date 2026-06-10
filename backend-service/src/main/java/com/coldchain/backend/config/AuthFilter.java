package com.coldchain.backend.config;

import com.coldchain.backend.entity.UserRecord;
import com.coldchain.backend.repository.AdminDataRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register");

    private final AdminDataRepository adminDataRepository;

    public AuthFilter(AdminDataRepository adminDataRepository) {
        this.adminDataRepository = adminDataRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (!path.startsWith("/api/v1")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (PUBLIC_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或令牌已过期");
            return;
        }

        String token = header.substring(7);
        if (!JwtUtil.isTokenValid(token)) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或令牌已过期");
            return;
        }

        Claims claims = JwtUtil.parseToken(token);
        UserRecord user = adminDataRepository.findUserById(claims.getSubject()).orElse(null);
        if (user == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "用户不存在或登录已失效");
            return;
        }
        if ("已封禁".equals(user.status())) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, "账号已被封禁，请联系管理员");
            return;
        }

        String role = user.role();
        if (path.startsWith("/api/v1/admin") && !"ADMIN".equals(role)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, "当前账号无管理员权限");
            return;
        }

        request.setAttribute("userId", user.id());
        request.setAttribute("username", user.username());
        request.setAttribute("role", role);

        filterChain.doFilter(request, response);
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\",\"data\":null}");
    }
}
