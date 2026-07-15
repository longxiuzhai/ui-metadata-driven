package com.example.metadataui.card.condition;
import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component; import java.util.Arrays;
@Component @Order(30) public class TenantConditionEvaluator implements CardConditionEvaluator {
 public boolean matches(CardConditional c, CardConditionContext x){return c.tenants().length==0||Arrays.asList(c.tenants()).contains(x.getTenantId());}
}
