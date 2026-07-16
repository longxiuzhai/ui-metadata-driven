package com.example.metadataui.card.condition;

public interface CardConditionEvaluator {

    boolean matches(CardConditional condition, CardConditionContext context);
}
