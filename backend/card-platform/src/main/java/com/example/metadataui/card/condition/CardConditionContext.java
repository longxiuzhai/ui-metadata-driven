package com.example.metadataui.card.condition;

import java.util.Map;
import java.util.Set;

public interface CardConditionContext {

    String getTenantId();

    String getUserId();

    Set<String> getPermissions();

    Map<String, Boolean> getFeatureFlags();
}
