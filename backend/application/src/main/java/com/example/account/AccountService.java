package com.example.account;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.rbac.entity.SysUser;
import com.example.rbac.mapper.SysUserMapper;
import com.example.rbac.service.RbacQueryService;
import com.example.security.AuthenticatedAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class AccountService {
    private static final Set<String> LEGACY_PERMISSIONS = Set.of(
            "home:read", "home:activity:read", "customer:read", "customer:update",
            "customer:tag:read", "customer:trace:read", "order:read");
    private static final Set<String> DEFAULT_FEATURES = Set.of(
            "homeActivity", "customerTags", "customerBehaviorTrace");

    private final SysUserMapper userMapper;
    private final RbacQueryService rbacQueryService;

    public AccountService(SysUserMapper userMapper, RbacQueryService rbacQueryService) {
        this.userMapper = userMapper;
        this.rbacQueryService = rbacQueryService;
    }

    public Optional<SysUser> findByUsername(String tenantId, String username) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getUsername, username)));
    }

    public Optional<SysUser> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email)));
    }

    public Optional<SysUser> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    public AccountIdentity identity(SysUser user) {
        return new AccountIdentity(
                user.getTenantId(), String.valueOf(user.getId()), user.getUsername(), user.getDisplayName(),
                rbacQueryService.roleCodes(user.getId()), rbacQueryService.permissionCodes(user.getId()),
                DEFAULT_FEATURES);
    }

    public Optional<AccountIdentity> currentIdentity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return Optional.empty();
        if (authentication.getPrincipal() instanceof AuthenticatedAccount) {
            AuthenticatedAccount principal = (AuthenticatedAccount) authentication.getPrincipal();
            return findById(principal.getId()).map(this::identity);
        }
        String username = authentication.getName();
        return findByUsername("demo", username).map(this::identity);
    }

    public AccountIdentity load(String tenantId, String userId) {
        Optional<AccountIdentity> current = currentIdentity();
        if (current.isPresent()) return current.get();
        try {
            Optional<SysUser> byId = findById(Long.valueOf(userId));
            if (byId.isPresent()) return identity(byId.get());
        } catch (NumberFormatException ignored) {
            // Legacy demonstration identifiers are not numeric database IDs.
        }
        Optional<SysUser> byUsername = findByUsername(tenantId, userId);
        if (byUsername.isPresent()) return identity(byUsername.get());
        return new AccountIdentity(tenantId, userId, userId, "演示用户", Set.of("DEMO"),
                LEGACY_PERMISSIONS, DEFAULT_FEATURES);
    }

    public AuthenticatedAccount authenticated(SysUser user) {
        return new AuthenticatedAccount(user.getId(), user.getTenantId(), user.getUsername(),
                user.getDisplayName(), rbacQueryService.roleCodes(user.getId()),
                rbacQueryService.permissionCodes(user.getId()));
    }
}
