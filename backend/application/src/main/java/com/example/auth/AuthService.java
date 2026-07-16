package com.example.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.account.AccountIdentity;
import com.example.account.AccountService;
import com.example.rbac.entity.SysRole;
import com.example.rbac.entity.SysUser;
import com.example.rbac.entity.SysUserRole;
import com.example.rbac.mapper.SysRoleMapper;
import com.example.rbac.mapper.SysUserMapper;
import com.example.rbac.mapper.SysUserRoleMapper;
import com.example.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.-]{2,31}$");
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;
    private final JwtService jwtService;

    public AuthService(SysUserMapper userMapper, SysRoleMapper roleMapper, SysUserRoleMapper userRoleMapper,
                       PasswordEncoder passwordEncoder, AccountService accountService, JwtService jwtService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    public AuthResult login(String tenantId, String username, String password) {
        SysUser user = accountService.findByUsername(normalizeTenant(tenantId), normalize(username))
                .orElseThrow(() -> new AuthException("用户名或密码错误"));
        if (!Boolean.TRUE.equals(user.getEnabled()) || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthException("用户名或密码错误");
        }
        return result(user);
    }

    @Transactional
    public AuthResult register(String username, String password, String displayName, String email) {
        username = normalize(username);
        if (!USERNAME.matcher(username).matches()) {
            throw new IllegalArgumentException("用户名需以字母开头，长度 3-32，只能包含字母、数字、点、横线和下划线");
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("密码长度必须为 8-72 位");
        }
        if (displayName == null || displayName.isBlank() || displayName.length() > 100) {
            throw new IllegalArgumentException("显示名称不能为空且不能超过 100 个字符");
        }
        if (accountService.findByUsername("demo", username).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (email != null && !email.isBlank() && accountService.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        user.setTenantId("demo");
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(displayName.trim());
        user.setEmail(email == null || email.isBlank() ? null : email.trim());
        user.setEnabled(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);

        SysRole defaultRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, "USER"));
        if (defaultRole == null) throw new IllegalStateException("default USER role is missing");
        userRoleMapper.insert(new SysUserRole(user.getId(), defaultRole.getId()));
        return result(user);
    }

    private AuthResult result(SysUser user) {
        AccountIdentity account = accountService.identity(user);
        return new AuthResult(jwtService.create(user.getId(), user.getTenantId(), user.getUsername()), account);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String normalizeTenant(String value) {
        return value == null || value.isBlank() ? "demo" : value.trim();
    }

    public static final class AuthResult {
        private final String token;
        private final AccountIdentity account;

        public AuthResult(String token, AccountIdentity account) {
            this.token = token;
            this.account = account;
        }

        public String getToken() { return token; }
        public AccountIdentity getAccount() { return account; }
    }
}
