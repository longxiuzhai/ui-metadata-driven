package com.example.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthService.AuthResult login(@RequestBody LoginRequest request) {
        return authService.login(request.getTenantId(), request.getUsername(), request.getPassword());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthService.AuthResult register(@RequestBody RegisterRequest request) {
        return authService.register(request.getUsername(), request.getPassword(),
                request.getDisplayName(), request.getEmail());
    }

    @PostMapping("/logout")
    public Map<String, Boolean> logout() {
        return Map.of("success", true);
    }

    public static class LoginRequest {
        private String tenantId;
        private String username;
        private String password;
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String displayName;
        private String email;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
