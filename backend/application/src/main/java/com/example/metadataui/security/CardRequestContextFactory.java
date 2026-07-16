package com.example.metadataui.security;

import com.example.metadataui.account.AccountIdentity;
import com.example.metadataui.account.AccountService;
import com.example.metadataui.card.context.CardRequestContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CardRequestContextFactory {
    private final AccountService accountService;

    public CardRequestContextFactory(AccountService accountService) {
        this.accountService = accountService;
    }

    public CardRequestContext create(String pageCode, String tenantId, String userId,
                                     String permissionHeader, String featureHeader, Map<String, String> parameters) {
        AccountIdentity account = accountService.load(tenantId, userId);
        Set<String> permissions = permissionHeader == null ? account.getPermissions() : split(permissionHeader);
        Set<String> features = featureHeader == null ? account.getFeatures() : split(featureHeader);
        return new CardRequestContext(pageCode, tenantId, userId, permissions,
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
