package com.coldchain.backend.repository;

import com.coldchain.backend.config.DataSourceModeProperties;
import com.coldchain.backend.entity.UserRecord;
import com.coldchain.backend.entity.mysql.UserEntity;
import com.coldchain.backend.repository.mysql.UserJpaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDataRepository {
    private final DataSourceModeProperties dataSourceModeProperties;
    private final Optional<UserJpaRepository> userJpaRepository;

    private final List<UserRecord> users = new ArrayList<>();

    public AdminDataRepository(
            DataSourceModeProperties dataSourceModeProperties,
            Optional<UserJpaRepository> userJpaRepository) {
        this.dataSourceModeProperties = dataSourceModeProperties;
        this.userJpaRepository = userJpaRepository;
        initUsers();
    }

    public List<UserRecord> findAllUsers() {
        if (dataSourceModeProperties.useMysql()) {
            return userJpaRepository.orElseThrow().findAll().stream().map(this::toUserRecord).toList();
        }
        return List.copyOf(users);
    }

    public Optional<UserRecord> findUserById(String userId) {
        if (dataSourceModeProperties.useMysql()) {
            return userJpaRepository.orElseThrow().findByUserId(userId).map(this::toUserRecord);
        }
        return users.stream().filter(u -> u.id().equals(userId)).findFirst();
    }

    public Optional<UserRecord> findUserByUsername(String username) {
        if (dataSourceModeProperties.useMysql()) {
            return userJpaRepository.orElseThrow().findByUsername(username).map(this::toUserRecord);
        }
        return users.stream()
                .filter(u -> u.username().equalsIgnoreCase(username))
                .findFirst();
    }

    public boolean existsByUsername(String username) {
        if (dataSourceModeProperties.useMysql()) {
            return userJpaRepository.orElseThrow().existsByUsername(username);
        }
        return users.stream().anyMatch(u -> u.username().equalsIgnoreCase(username));
    }

    public boolean existsByPhone(String phone) {
        if (dataSourceModeProperties.useMysql()) {
            return userJpaRepository.orElseThrow().existsByPhone(phone);
        }
        return users.stream().anyMatch(u -> u.phone().equals(phone));
    }

    public boolean existsByEmail(String email) {
        if (dataSourceModeProperties.useMysql()) {
            return userJpaRepository.orElseThrow().existsByEmail(email);
        }
        return users.stream().anyMatch(u -> u.email().equalsIgnoreCase(email));
    }

    public UserRecord saveUser(UserRecord user) {
        if (dataSourceModeProperties.useMysql()) {
            UserEntity entity = toUserEntity(user);
            return toUserRecord(userJpaRepository.orElseThrow().save(entity));
        }
        users.add(user);
        return user;
    }

    public UserRecord updateUser(UserRecord user) {
        if (dataSourceModeProperties.useMysql()) {
            UserEntity entity = userJpaRepository.orElseThrow().findByUserId(user.id())
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + user.id()));
            applyUserToEntity(user, entity);
            return toUserRecord(userJpaRepository.orElseThrow().save(entity));
        }
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).id().equals(user.id())) {
                users.set(i, user);
                return user;
            }
        }
        throw new RuntimeException("用户不存在: " + user.id());
    }

    private void initUsers() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 3, 9, 0, 0);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        users.add(new UserRecord("USR-ADMIN-001", "admin", "admin", "13800000001",
                "admin@coldchain.local", encoder.encode("Admin123!"),
                "ADMIN", "启用中", "系统账号", now, 0, null));
        users.add(new UserRecord("USR-OPS-001", "operator", "operator", "13800000002",
                "operator@coldchain.local", encoder.encode("Operator123!"),
                "USER", "启用中", "系统账号", now, 0, null));
    }

    private UserRecord toUserRecord(UserEntity entity) {
        return new UserRecord(entity.getUserId(), entity.getUsername(), entity.getDisplayName(),
                entity.getPhone(), entity.getEmail(), entity.getPassword(), entity.getRole(),
                entity.getStatus(), entity.getOrigin(), entity.getCreatedAt(),
                entity.getLoginFailureCount(), entity.getLockedUntil());
    }

    private UserEntity toUserEntity(UserRecord record) {
        UserEntity entity = new UserEntity();
        applyUserToEntity(record, entity);
        return entity;
    }

    private void applyUserToEntity(UserRecord record, UserEntity entity) {
        entity.setUserId(record.id());
        entity.setUsername(record.username());
        entity.setDisplayName(record.displayName());
        entity.setPhone(record.phone());
        entity.setEmail(record.email());
        entity.setPassword(record.password());
        entity.setRole(record.role());
        entity.setStatus(record.status());
        entity.setOrigin(record.origin());
        entity.setCreatedAt(record.createdAt());
        entity.setLoginFailureCount(record.loginFailureCount());
        entity.setLockedUntil(record.lockedUntil());
    }
}
