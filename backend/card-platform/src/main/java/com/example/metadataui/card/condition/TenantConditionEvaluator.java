package com.example.metadataui.card.condition;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Order(30)
public class TenantConditionEvaluator implements CardConditionEvaluator {

    @Override
    public boolean matches(CardConditional condition, CardConditionContext context) {
        return condition.tenants().length == 0
                || Arrays.asList(condition.tenants()).contains(context.getTenantId());
    }
}
