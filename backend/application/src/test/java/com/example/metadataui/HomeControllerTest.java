package com.example.metadataui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void assemblesCoreAndPluginCards() throws Exception {
        mvc.perform(get("/api/ui/pages/home/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageCode").value("home"))
                .andExpect(jsonPath("$.cards[*].code", contains("work_summary", "recent_activity")));
    }

    @Test
    void filtersPluginByPermissionAndFeature() throws Exception {
        mvc.perform(get("/api/ui/home/cards")
                        .header("X-Permissions", "home:read")
                        .header("X-Features", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards", hasSize(1)))
                .andExpect(jsonPath("$.cards[0].code").value("work_summary"));
    }

    @Test
    void exposesCardDataEndpoints() throws Exception {
        mvc.perform(get("/api/home/user-1/work-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(6));
        mvc.perform(get("/api/home/user-1/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
