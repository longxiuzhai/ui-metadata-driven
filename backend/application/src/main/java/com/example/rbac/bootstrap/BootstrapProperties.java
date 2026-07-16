package com.example.rbac.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.bootstrap")
public class BootstrapProperties {
    private boolean enabled = true;
    private String adminUsername = "admin";
    private String adminPassword = "Admin123!";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
}
