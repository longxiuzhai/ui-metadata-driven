package com.example.metadataui.customer.detail.spi;
import com.example.metadataui.card.condition.CardConditionContext; import java.util.*;
public final class CustomerDetailContext implements CardConditionContext {
 private final String tenantId,userId,customerId; private final Set<String> permissions; private final Map<String,Boolean> featureFlags;
 public CustomerDetailContext(String tenantId,String userId,String customerId,Set<String> permissions,Map<String,Boolean> featureFlags){this.tenantId=tenantId;this.userId=userId;this.customerId=customerId;this.permissions=Collections.unmodifiableSet(new HashSet<>(permissions));this.featureFlags=Collections.unmodifiableMap(new HashMap<>(featureFlags));}
 public String getTenantId(){return tenantId;} public String getUserId(){return userId;} public String getCustomerId(){return customerId;} public Set<String> getPermissions(){return permissions;} public Map<String,Boolean> getFeatureFlags(){return featureFlags;}
}
