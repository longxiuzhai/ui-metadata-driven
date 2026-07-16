package com.example.metadataui.card.condition;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Order(20)
public class PermissionConditionEvaluator implements CardConditionEvaluator {

    @Override
    public boolean matches(CardConditional condition, CardConditionContext context) {
        return Arrays.stream(condition.permissions()).allMatch(context.getPermissions()::contains);
    }
}
