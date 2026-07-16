package com.example.metadataui.card.condition;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Order(10)
public class FeatureConditionEvaluator implements CardConditionEvaluator {

    @Override
    public boolean matches(CardConditional condition, CardConditionContext context) {
        return !StringUtils.hasText(condition.feature())
                || Boolean.TRUE.equals(context.getFeatureFlags().get(condition.feature()));
    }
}
