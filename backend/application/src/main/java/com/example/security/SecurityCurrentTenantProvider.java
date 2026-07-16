package com.example.security;

import com.example.metadataui.card.context.CurrentTenantProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentTenantProvider implements CurrentTenantProvider {
    @Override
    public String currentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedAccount) {
            return ((AuthenticatedAccount) authentication.getPrincipal()).getTenantId();
        }
        return "demo";
    }
}
