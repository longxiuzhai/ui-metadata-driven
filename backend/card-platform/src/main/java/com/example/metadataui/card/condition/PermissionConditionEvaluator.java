package com.example.metadataui.card.condition;
import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component; import java.util.Arrays;
@Component @Order(20) public class PermissionConditionEvaluator implements CardConditionEvaluator {
 public boolean matches(CardConditional c, CardConditionContext x){return Arrays.stream(c.permissions()).allMatch(x.getPermissions()::contains);}
}
