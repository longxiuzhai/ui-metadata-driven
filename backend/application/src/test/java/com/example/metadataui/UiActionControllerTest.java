package com.example.metadataui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UiActionControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void preparesExecutesAndRefreshesCustomerData() throws Exception {
        String customerId = "action-test";
        mvc.perform(post("/api/ui/actions/customer.basic.update/prepare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prepareBody(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formCode").value("customer_basic_edit"))
                .andExpect(jsonPath("$.version").value(1));

        mvc.perform(post("/api/ui/actions/customer.basic.update/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeBody(customerId, 1, "request-1", "更新后的客户")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshCards[0]").value("basic_info"));

        mvc.perform(get("/api/customers/{id}/basic", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("更新后的客户"));

        mvc.perform(post("/api/ui/actions/customer.basic.update/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeBody(customerId, 1, "request-2", "过期修改")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsDirectExecutionWithoutPermission() throws Exception {
        mvc.perform(post("/api/ui/actions/customer.basic.update/execute")
                        .header("X-Permissions", "customer:read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeBody("1001", 1, "request-denied", "无权限修改")))
                .andExpect(status().isForbidden());
    }

    private String prepareBody(String customerId) {
        return "{\"pageCode\":\"customer_detail\",\"cardCode\":\"basic_info\","
                + "\"params\":{\"customerId\":\"" + customerId + "\"}}";
    }

    private String executeBody(String customerId, long version, String requestId, String name) {
        return "{\"pageCode\":\"customer_detail\",\"cardCode\":\"basic_info\","
                + "\"params\":{\"customerId\":\"" + customerId + "\"},"
                + "\"values\":{\"name\":\"" + name
                + "\",\"mobile\":\"13800138000\",\"status\":\"ACTIVE\"},"
                + "\"version\":" + version + ",\"requestId\":\"" + requestId + "\"}";
    }
}
