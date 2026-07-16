package com.example.metadataui.page;

import com.example.metadataui.card.application.CardPageAssembler;
import com.example.metadataui.card.context.CardRequestContext;
import com.example.metadataui.card.model.CardDefinition;
import com.example.metadataui.card.model.CardPageDefinition;
import com.example.metadataui.card.registry.CardProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CardMetadataService {
    private static final Logger log = LoggerFactory.getLogger(CardMetadataService.class);
    private final CardProviderRegistry providerRegistry;
    private final CardPageAssembler assembler;

    public CardMetadataService(CardProviderRegistry providerRegistry, CardPageAssembler assembler) {
        this.providerRegistry = providerRegistry;
        this.assembler = assembler;
    }

    public CardPageDefinition getCards(CardRequestContext context) {
        CardPageDefinition page = assembler.assemble(
                context.getPageCode(), providerRegistry.providersFor(context.getPageCode()), context);
        log.info("page metadata: page={}, tenant={}, user={}, cards={}", context.getPageCode(),
                context.getTenantId(), context.getUserId(),
                page.getCards().stream().map(CardDefinition::getCode).collect(Collectors.toList()));
        return page;
    }
}
