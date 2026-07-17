package com.example.metadataui;

import com.example.metadataui.action.registry.UiActionHandlerRegistry;
import com.example.metadataui.customization.demo.BasicInfoCardProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "metadata.cards.behavior-trace.enabled=false")
@AutoConfigureMockMvc
class CardPluginConditionTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private BasicInfoCardProvider basicInfoCardProvider;
    @Autowired
    private UiActionHandlerRegistry actionHandlerRegistry;

    @Test
    void disablesOneCardProviderWithoutAffectingThePage() throws Exception {
        mvc.perform(get("/api/ui/pages/customer_detail/cards").param("customerId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[*].code", contains("basic_info", "friend_tags")));
    }

    @Test
    void registersTheSameBusinessBeanAsCardProviderAndActionHandler() {
        assertSame(basicInfoCardProvider,
                actionHandlerRegistry.find("customer.basic.update").orElseThrow());
    }
}
