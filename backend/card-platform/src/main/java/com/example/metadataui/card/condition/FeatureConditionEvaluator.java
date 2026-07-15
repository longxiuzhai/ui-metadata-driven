package com.example.metadataui.card.condition;
import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component; import org.springframework.util.StringUtils;
@Component @Order(10) public class FeatureConditionEvaluator implements CardConditionEvaluator {
 public boolean matches(CardConditional c, CardConditionContext x){return !StringUtils.hasText(c.feature())||Boolean.TRUE.equals(x.getFeatureFlags().get(c.feature()));}
}
