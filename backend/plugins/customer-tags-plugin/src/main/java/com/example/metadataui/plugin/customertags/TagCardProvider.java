package com.example.metadataui.plugin.customertags;
import com.example.metadataui.card.condition.CardConditional; import com.example.metadataui.card.model.*; import com.example.metadataui.customer.detail.spi.*; import org.springframework.stereotype.Component; import java.util.*;
@Component @CardConditional(feature="customerTags",permissions="customer:tag:read") public class TagCardProvider implements CustomerDetailCardProvider {
 public String cardCode(){return "friend_tags";} public CardDefinition definition(CustomerDetailContext c){return new CardDefinition(cardCode(),"好友标签","TagGroupCard",20,new ResponsiveSpan(24,12,12),"/api/customers/{customerId}/tags","eager","customer:tag:read",Map.of("labelField","name","colorField","color"),List.of());}
}
