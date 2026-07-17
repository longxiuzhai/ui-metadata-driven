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
    void exposesCustomerAndOrderListMetadataPages() throws Exception {
        mvc.perform(get("/api/ui/pages/customer_list/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageCode").value("customer_list"))
                .andExpect(jsonPath("$.cards[0].dataApi").value("/api/customers"))
                .andExpect(jsonPath("$.cards[0].props.tableActionCodes[0]").value("create_customer"))
                .andExpect(jsonPath("$.cards[0].props.rowActionCodes",
                        contains("view_customer", "edit_customer", "delete_customer")))
                .andExpect(jsonPath("$.cards[0].props.columns[0].fixed").value("left"))
                .andExpect(jsonPath("$.cards[0].actions[0].target.actionCode").value("customer.create"))
                .andExpect(jsonPath("$.cards[0].actions[1].params.customerId.source").value("card-data"))
                .andExpect(jsonPath("$.cards[0].actions[3].type").value("execute"));

        mvc.perform(get("/api/ui/pages/order_list/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageCode").value("order_list"))
                .andExpect(jsonPath("$.cards[0].dataApi").value("/api/orders"))
                .andExpect(jsonPath("$.cards[0].props.tableActionCodes[0]").value("create_order"))
                .andExpect(jsonPath("$.cards[0].props.rowActionCodes",
                        contains("view_order", "edit_order", "delete_order")))
                .andExpect(jsonPath("$.cards[0].props.columns[0].fixed").value("left"))
                .andExpect(jsonPath("$.cards[0].actions[3].target.actionCode").value("order.delete"));

        mvc.perform(get("/api/ui/pages/order_list/cards").param("customerId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageCode").value("order_list"))
                .andExpect(jsonPath("$.cards", hasSize(1)))
                .andExpect(jsonPath("$.cards[0].code").value("order_list"))
                .andExpect(jsonPath("$.cards[0].component").value("DataTableCard"))
                .andExpect(jsonPath("$.cards[0].props.columns", hasSize(12)))
                .andExpect(jsonPath("$.cards[0].dataApi").value("/api/customers/{customerId}/orders"));
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
        mvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerId").isNotEmpty());

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
                .andExpect(jsonPath("$[0].memberId").value("M10001"))
                .andExpect(jsonPath("$[0].memberUnionId").value("o_demo_union_1001"))
                .andExpect(jsonPath("$[0].memberMobile").value("13800138000"))
                .andExpect(jsonPath("$[0].memberLevel").value("GOLD"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[0].updatedAt").isNotEmpty());

        mvc.perform(get("/api/orders/O20260715001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("M10001"));

        mvc.perform(get("/api/orders").param("customerId", "1001").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderNo").value("O20260710002"));

        mvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/api/members/M10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("M10001"))
                .andExpect(jsonPath("$.unionId").value("o_demo_union_1001"))
                .andExpect(jsonPath("$.mobile").value("13800138000"));
    }

    @Test
    void queriesCustomerAndOrderListsWithBusinessCriteria() throws Exception {
        mvc.perform(get("/api/customers").param("customerId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value("1001"));

        mvc.perform(get("/api/customers").param("name", "示例客户"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("示例客户 1001"));

        mvc.perform(get("/api/customers").param("mobile", "00000000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(get("/api/orders").param("orderNo", "O20260715001")
                        .param("customerId", "1001").param("memberId", "M10001")
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderNo").value("O20260715001"));

        mvc.perform(get("/api/customers/1001/orders").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderNo").value("O20260710002"));

        mvc.perform(get("/api/orders").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());
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
