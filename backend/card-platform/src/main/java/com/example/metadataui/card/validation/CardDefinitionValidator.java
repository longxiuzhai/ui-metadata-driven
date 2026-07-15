package com.example.metadataui.card.validation;
import com.example.metadataui.card.model.CardDefinition; import org.springframework.stereotype.Component; import org.springframework.util.StringUtils; import java.util.Set; import java.util.regex.Pattern;
@Component public class CardDefinitionValidator {
 private static final Set<String> COMPONENTS=Set.of("KeyValueCard","TagGroupCard","TimelineCard"); private static final Set<String> STRATEGIES=Set.of("eager","on-visible"); private static final Pattern INTERNAL_API=Pattern.compile("^/api/[a-zA-Z0-9_/{}/.-]+$");
 public void validate(CardDefinition c){require(StringUtils.hasText(c.getCode()),"card code is required");require(COMPONENTS.contains(c.getComponent()),"unsupported component: "+c.getComponent());require(STRATEGIES.contains(c.getLoadStrategy()),"unsupported load strategy: "+c.getLoadStrategy());require(INTERNAL_API.matcher(c.getDataApi()).matches(),"dataApi must be an internal /api path");require(c.getSpan()!=null&&valid(c.getSpan().getXs())&&valid(c.getSpan().getMd())&&valid(c.getSpan().getXl()),"span must be 1..24");}
 private boolean valid(int n){return n>=1&&n<=24;} private void require(boolean b,String m){if(!b)throw new IllegalArgumentException(m);}
}
