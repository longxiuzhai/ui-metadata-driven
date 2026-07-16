package com.example.metadataui.card.registry;

import com.example.metadataui.card.spi.CardProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CardProviderRegistry {
    private final Map<String, List<CardProvider>> providersByPage;

    public CardProviderRegistry(List<CardProvider> providers) {
        Map<String, List<CardProvider>> groupedProviders = new LinkedHashMap<>();
        for (CardProvider provider : providers) {
            validateIdentity(provider);
            List<CardProvider> pageProviders = groupedProviders.computeIfAbsent(
                    provider.pageCode(), ignored -> new ArrayList<>());
            boolean duplicate = pageProviders.stream()
                    .anyMatch(candidate -> candidate.cardCode().equals(provider.cardCode()));
            if (duplicate) {
                throw new IllegalStateException(
                        "duplicate card provider: " + provider.pageCode() + "/" + provider.cardCode());
            }
            pageProviders.add(provider);
        }
        groupedProviders.replaceAll((pageCode, pageProviders) -> List.copyOf(pageProviders));
        this.providersByPage = Collections.unmodifiableMap(groupedProviders);
    }

    public List<CardProvider> providersFor(String pageCode) {
        return providersByPage.getOrDefault(pageCode, List.of());
    }

    public Map<String, List<CardProvider>> all() {
        return providersByPage;
    }

    private void validateIdentity(CardProvider provider) {
        if (!StringUtils.hasText(provider.pageCode())) {
            throw new IllegalStateException("card provider pageCode is required");
        }
        if (!StringUtils.hasText(provider.cardCode())) {
            throw new IllegalStateException("card provider cardCode is required");
        }
    }
}
