package com.example.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private String jwtSecret;
    private long tokenTtlSeconds = 28800;
    private boolean permitAll;
    private boolean allowHeaderOverrides;

    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
    public long getTokenTtlSeconds() { return tokenTtlSeconds; }
    public void setTokenTtlSeconds(long tokenTtlSeconds) { this.tokenTtlSeconds = tokenTtlSeconds; }
    public boolean isPermitAll() { return permitAll; }
    public void setPermitAll(boolean permitAll) { this.permitAll = permitAll; }
    public boolean isAllowHeaderOverrides() { return allowHeaderOverrides; }
    public void setAllowHeaderOverrides(boolean allowHeaderOverrides) { this.allowHeaderOverrides = allowHeaderOverrides; }
}
