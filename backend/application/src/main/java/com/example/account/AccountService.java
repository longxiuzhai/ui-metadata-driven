package com.example.account;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AccountService {
    private static final Set<String> DEMO_PERMISSIONS = Set.of(
            "home:read", "home:activity:read", "customer:read", "customer:update",
            "customer:tag:read", "customer:trace:read", "order:read");
    private static final Set<String> DEMO_FEATURES = Set.of(
            "homeActivity", "customerTags", "customerBehaviorTrace");

    public AccountIdentity load(String tenantId, String userId) {
        return new AccountIdentity(tenantId, userId, DEMO_PERMISSIONS, DEMO_FEATURES);
    }
}
