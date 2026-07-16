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
class CustomerDetailControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void discoversTenantProvidersThroughUnifiedEndpoint() throws Exception {
        mvc.perform(get("/api/ui/pages/customer_detail/cards").param("customerId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[*].code", contains("basic_info", "friend_tags", "behavior_trace")))
                .andExpect(jsonPath("$.cards[0].actions[0].target.formCode")
                        .value("customer_basic_edit"))
                .andExpect(jsonPath("$.cards[0].actions[1].target.routeCode").value("order_list"))
                .andExpect(jsonPath("$.cards[0].actions[1].params.customerId.source")
                        .value("page-context"));
    }

    @Test
    void filtersByPermissionAndFeature() throws Exception {
        mvc.perform(get("/api/ui/pages/customer_detail/cards").param("customerId", "1001")
                        .header("X-Permissions", "customer:read")
                        .header("X-Features", "customerTags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards", hasSize(1)))
                .andExpect(jsonPath("$.cards[0].actions", hasSize(0)));
    }

    @Test
    void exposesCurrentAccountFromCoreApplication() throws Exception {
        mvc.perform(get("/api/accounts/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("demo"))
                .andExpect(jsonPath("$.userId").value("user-1"));
    }

    @Test
    void readsCustomerTagsTracesOrdersAndMemberFromDatabase() throws Exception {
        mvc.perform(get("/api/customers/1001/basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("1001"))
                .andExpect(jsonPath("$.unionId").value("o_demo_union_1001"))
                .andExpect(jsonPath("$.mobile").value("13800138000"));

        mvc.perform(get("/api/customers/1001/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("重点客户"));

        mvc.perform(get("/api/customers/1001/traces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventName").value("浏览产品"));

        mvc.perform(get("/api/customers/1001/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderNo").value("O20260715001"))
                .andExpect(jsonPath("$[0].memberId").value("M10001"));

        mvc.perform(get("/api/orders/O20260715001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("M10001"));

        mvc.perform(get("/api/orders").param("customerId", "1001").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderNo").value("O20260710002"));

        mvc.perform(get("/api/members/M10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("M10001"))
                .andExpect(jsonPath("$.unionId").value("o_demo_union_1001"))
                .andExpect(jsonPath("$.mobile").value("13800138000"));
    }

    @Test
    void returnsNotFoundForUnknownBusinessData() throws Exception {
        mvc.perform(get("/api/customers/missing/basic"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/orders/missing"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/members/missing"))
                .andExpect(status().isNotFound());
    }
}
