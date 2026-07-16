package com.example.metadataui.security;

import com.example.account.AccountIdentity;
import com.example.account.AccountService;
import com.example.metadataui.card.context.CardRequestContext;
import com.example.security.SecurityProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CardRequestContextFactory {
    private final AccountService accountService;
    private final SecurityProperties securityProperties;

    public CardRequestContextFactory(AccountService accountService, SecurityProperties securityProperties) {
        this.accountService = accountService;
        this.securityProperties = securityProperties;
    }

    public CardRequestContext create(String pageCode, String tenantId, String userId,
                                     String permissionHeader, String featureHeader, Map<String, String> parameters) {
        AccountIdentity account = accountService.currentIdentity().orElseGet(() -> accountService.load(tenantId, userId));
        boolean allowOverrides = securityProperties.isAllowHeaderOverrides();
        Set<String> permissions = !allowOverrides || permissionHeader == null
                ? account.getPermissions() : split(permissionHeader);
        Set<String> features = !allowOverrides || featureHeader == null
                ? account.getFeatures() : split(featureHeader);
        return new CardRequestContext(pageCode, account.getTenantId(), account.getUserId(), permissions,
                features.stream().collect(Collectors.toMap(value -> value, value -> true)), parameters);
    }

    private Set<String> split(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toSet());
    }
}
