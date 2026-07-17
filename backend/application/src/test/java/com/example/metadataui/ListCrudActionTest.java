package com.example.metadataui;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class ListCrudActionTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void createsAndUpdatesCustomerFromListActions() throws Exception {
        mvc.perform(post("/api/ui/actions/customer.create/prepare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prepareBody("customer_list", "customer_list", "{}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formCode").value("customer_list_edit"))
                .andExpect(jsonPath("$.initialValues.status").value("ACTIVE"));

        mvc.perform(post("/api/ui/actions/customer.create/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeBody("customer_list", "customer_list", "{}",
                                "{\"customerId\":\"crud-customer\",\"name\":\"新增客户\","
                                        + "\"unionId\":\"union-crud\",\"mobile\":\"13700000000\","
                                        + "\"status\":\"ACTIVE\"}", 0, "create-customer-crud")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshCards[0]").value("customer_list"));

        mvc.perform(post("/api/ui/actions/customer.list.update/prepare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prepareBody("customer_list", "customer_list",
                                "{\"customerId\":\"crud-customer\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));

        mvc.perform(post("/api/ui/actions/customer.list.update/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeBody("customer_list", "customer_list",
                                "{\"customerId\":\"crud-customer\"}",
                                "{\"name\":\"修改后的客户\",\"mobile\":\"13600000000\","
                                        + "\"status\":\"INACTIVE\"}", 1, "update-customer-crud")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/customers/crud-customer/basic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("修改后的客户"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void createsAndUpdatesOrderFromListActions() throws Exception {
        mvc.perform(post("/api/ui/actions/order.create/prepare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prepareBody("order_list", "order_list", "{}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formCode").value("order_list_edit"))
                .andExpect(jsonPath("$.initialValues.orderStatus").value("OPEN"));

        mvc.perform(post("/api/ui/actions/order.create/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeBody("order_list", "order_list", "{}",
                                "{\"orderNo\":\"CRUD-ORDER-1\",\"customerId\":\"1001\","
                                        + "\"memberId\":\"M10001\",\"orderAmount\":\"88.50\","
                                        + "\"orderStatus\":\"OPEN\",\"placedAt\":\"2026-07-17 16:00\"}",
                                0, "create-order-crud")))
                .andExpect(status().isOk());

        mvc.perform(post("/api/ui/actions/order.list.update/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeBody("order_list", "order_list",
                                "{\"orderNo\":\"CRUD-ORDER-1\"}",
                                "{\"customerId\":\"1001\",\"memberId\":\"M10001\","
                                        + "\"orderAmount\":\"99.90\",\"orderStatus\":\"PAID\","
                                        + "\"placedAt\":\"2026-07-17 16:05\"}", 0, "update-order-crud")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/orders/CRUD-ORDER-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderAmount").value(99.9))
                .andExpect(jsonPath("$.orderStatus").value("PAID"));
    }

    private String prepareBody(String pageCode, String cardCode, String params) {
        return "{\"pageCode\":\"" + pageCode + "\",\"cardCode\":\"" + cardCode
                + "\",\"params\":" + params + "}";
    }

    private String executeBody(String pageCode, String cardCode, String params, String values,
                               long version, String requestId) {
        return "{\"pageCode\":\"" + pageCode + "\",\"cardCode\":\"" + cardCode
                + "\",\"params\":" + params + ",\"values\":" + values + ",\"version\":"
                + version + ",\"requestId\":\"" + requestId + "\"}";
    }
}
