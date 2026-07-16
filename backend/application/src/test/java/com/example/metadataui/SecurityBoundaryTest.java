package com.example.metadataui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.security.permit-all=false")
@AutoConfigureMockMvc
class SecurityBoundaryTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void requiresBearerTokenForBusinessApis() throws Exception {
        mvc.perform(get("/api/ui/pages/home/cards"))
                .andExpect(status().isUnauthorized());

        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsByteArray())
                .get("token").asText();

        mvc.perform(get("/api/ui/pages/home/cards")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
