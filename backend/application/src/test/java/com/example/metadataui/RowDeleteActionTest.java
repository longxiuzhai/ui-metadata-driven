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
class RowDeleteActionTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void deletesAnOrderAndRefreshesTheOrderList() throws Exception {
        mvc.perform(post("/api/ui/actions/order.delete/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeBody("order_list", "order_list", "orderNo", "O20260710002", "delete-order-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("订单已删除"))
                .andExpect(jsonPath("$.refreshCards[0]").value("order_list"));

        mvc.perform(get("/api/orders/O20260710002"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletesACustomerWithoutOrdersButRejectsCustomerWithOrders() throws Exception {
        mvc.perform(post("/api/ui/actions/customer.delete/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeBody("customer_list", "customer_list", "customerId", "1001", "delete-customer-blocked")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("客户存在关联订单，请先删除订单"));

        mvc.perform(post("/api/ui/actions/customer.delete/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executeBody("customer_list", "customer_list", "customerId", "action-test", "delete-customer-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshCards[0]").value("customer_list"));

        mvc.perform(get("/api/customers/action-test/basic"))
                .andExpect(status().isNotFound());
    }

    private String executeBody(String pageCode, String cardCode, String parameter,
                               String value, String requestId) {
        return "{\"pageCode\":\"" + pageCode + "\",\"cardCode\":\"" + cardCode + "\","
                + "\"params\":{\"" + parameter + "\":\"" + value + "\"},"
                + "\"values\":{},\"version\":0,\"requestId\":\"" + requestId + "\"}";
    }
}
