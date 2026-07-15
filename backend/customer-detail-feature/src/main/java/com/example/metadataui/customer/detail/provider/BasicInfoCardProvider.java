package com.example.metadataui.customer.detail.provider;
import com.example.metadataui.card.condition.CardConditional; import com.example.metadataui.card.model.*; import com.example.metadataui.customer.detail.spi.*; import org.springframework.stereotype.Component; import java.util.*;
@Component @CardConditional(permissions="customer:read") public class BasicInfoCardProvider implements CustomerDetailCardProvider {
 public String cardCode(){return "basic_info";} public CardDefinition definition(CustomerDetailContext c){return new CardDefinition(cardCode(),"基本信息","KeyValueCard",10,new ResponsiveSpan(24,24,12),"/api/customers/{customerId}/basic","eager","customer:read",Map.of("columns",2,"fields",List.of(Map.of("key","name","label","姓名"),Map.of("key","mobile","label","手机号","formatter","mobile-mask"),Map.of("key","status","label","状态","formatter","customer-status"))),List.of(new CardAction("edit","编辑","open-form","customer_basic_edit","customer:update")));}
}
