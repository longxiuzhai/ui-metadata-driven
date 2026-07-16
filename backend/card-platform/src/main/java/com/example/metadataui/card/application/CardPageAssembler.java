package com.example.metadataui.card.application;

import com.example.metadataui.card.condition.CardConditionContext;
import com.example.metadataui.card.condition.CardConditionEvaluator;
import com.example.metadataui.card.condition.CardConditional;
import com.example.metadataui.card.context.CardRequestContext;
import com.example.metadataui.card.model.CardAction;
import com.example.metadataui.card.model.CardDefinition;
import com.example.metadataui.card.model.CardPageDefinition;
import com.example.metadataui.card.spi.CardProvider;
import com.example.metadataui.card.validation.CardDefinitionValidator;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CardPageAssembler {
    private final List<CardConditionEvaluator> evaluators;
    private final CardDefinitionValidator validator;

    public CardPageAssembler(List<CardConditionEvaluator> evaluators, CardDefinitionValidator validator) {
        this.evaluators = evaluators;
        this.validator = validator;
    }

    public CardPageDefinition assemble(String pageCode, List<CardProvider> providers, CardRequestContext context) {
        List<CardDefinition> cards = providers.stream()
                .filter(provider -> matches(provider, context))
                .filter(provider -> provider.supports(context))
                .map(provider -> definition(provider, context))
                .peek(validator::validate)
                .map(card -> filterActions(card, context))
                .sorted(Comparator.comparingInt(CardDefinition::getOrder).thenComparing(CardDefinition::getCode))
                .collect(Collectors.toList());
        return new CardPageDefinition(pageCode, cards);
    }

    private boolean matches(Object provider, CardConditionContext context) {
        CardConditional condition = AnnotationUtils.findAnnotation(
                AopUtils.getTargetClass(provider), CardConditional.class);
        return condition == null || evaluators.stream().allMatch(evaluator -> evaluator.matches(condition, context));
    }

    private CardDefinition definition(CardProvider provider, CardRequestContext context) {
        CardDefinition definition = provider.definition(context);
        if (!provider.cardCode().equals(definition.getCode())) {
            throw new IllegalStateException(
                    "provider cardCode does not match definition: "
                            + provider.pageCode() + "/" + provider.cardCode());
        }
        return definition;
    }

    private CardDefinition filterActions(CardDefinition card, CardConditionContext context) {
        List<CardAction> actions = card.getActions().stream()
                .filter(action -> action.getPermission() == null
                        || context.getPermissions().contains(action.getPermission()))
                .collect(Collectors.toList());
        return new CardDefinition(
                card.getCode(), card.getTitle(), card.getComponent(), card.getOrder(), card.getSpan(),
                card.getDataApi(), card.getLoadStrategy(), card.getPermission(), card.getProps(), actions);
    }
}
