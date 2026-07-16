package com.example.metadataui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthRbacControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registersUserAndLoadsRoleMenus() throws Exception {
        MvcResult registration = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"Password123!\","
                                + "\"displayName\":\"新用户\",\"email\":\"newuser@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account.username").value("newuser"))
                .andExpect(jsonPath("$.account.roles", hasItem("USER")))
                .andReturn();

        String token = token(registration);
        mvc.perform(get("/api/menus/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].code", hasItem("home")))
                .andExpect(jsonPath("$[*].code", hasItem("customer")));
    }

    @Test
    void authenticatesAdminAndProtectsManagementApi() throws Exception {
        mvc.perform(get("/api/admin/roles"))
                .andExpect(status().isUnauthorized());

        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":\"demo\",\"username\":\"admin\","
                                + "\"password\":\"Admin123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.roles", hasItem("ADMIN")))
                .andReturn();

        mvc.perform(get("/api/admin/roles")
                        .header("Authorization", "Bearer " + token(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", hasItem("ADMIN")))
                .andExpect(jsonPath("$[*].code", hasItem("USER")));
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    private String token(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return body.get("token").asText();
    }
}
