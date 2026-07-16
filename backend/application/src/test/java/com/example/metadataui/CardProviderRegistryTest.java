package com.example.metadataui;

import com.example.metadataui.card.context.CardRequestContext;
import com.example.metadataui.card.model.CardDefinition;
import com.example.metadataui.card.registry.CardProviderRegistry;
import com.example.metadataui.card.spi.CardProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CardProviderRegistryTest {

    @Test
    void groupsProvidersByPage() {
        CardProvider home = provider("home", "summary");
        CardProvider customer = provider("customer_detail", "summary");

        CardProviderRegistry registry = new CardProviderRegistry(List.of(home, customer));

        assertEquals(List.of(home), registry.providersFor("home"));
        assertEquals(List.of(customer), registry.providersFor("customer_detail"));
        assertEquals(List.of(), registry.providersFor("unknown"));
    }

    @Test
    void rejectsDuplicateCodesWithinOnePage() {
        List<CardProvider> duplicates = List.of(
                provider("home", "summary"),
                provider("home", "summary"));

        assertThrows(IllegalStateException.class, () -> new CardProviderRegistry(duplicates));
    }

    private CardProvider provider(String pageCode, String cardCode) {
        return new CardProvider() {
            @Override
            public String pageCode() {
                return pageCode;
            }

            @Override
            public String cardCode() {
                return cardCode;
            }

            @Override
            public CardDefinition definition(CardRequestContext context) {
                throw new UnsupportedOperationException("not needed by registry test");
            }
        };
    }
}
