package com.example.metadataui.customization.demo;

import com.example.metadataui.customization.demo.query.CustomerListQuery;
import com.example.metadataui.customization.demo.query.OrderListQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DemoCardDataController {
    private final DemoCustomerRepository customerRepository;

    public DemoCardDataController(DemoCustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/api/customers")
    public List<Map<String, Object>> customers(CustomerListQuery query) {
        return customerRepository.customers(query);
    }

    @GetMapping("/api/customers/{id}/basic")
    public Map<String, Object> basic(@PathVariable String id) {
        return customerRepository.get(id).values();
    }

    @GetMapping("/api/customers/{id}/tags")
    public List<Map<String, String>> tags(@PathVariable String id) {
        return customerRepository.tags(id);
    }

    @GetMapping("/api/customers/{id}/traces")
    public List<Map<String, String>> traces(@PathVariable String id) {
        return customerRepository.traces(id);
    }

    @GetMapping("/api/customers/{id}/orders")
    public List<Map<String, Object>> orders(@PathVariable String id, OrderListQuery query) {
        query.setCustomerId(id);
        return customerRepository.orders(query);
    }

    @GetMapping("/api/orders")
    public List<Map<String, Object>> orderList(OrderListQuery query) {
        return customerRepository.orders(query);
    }

    @GetMapping("/api/orders/{orderNo}")
    public Map<String, Object> order(@PathVariable String orderNo) {
        return customerRepository.order(orderNo);
    }

    @GetMapping("/api/members/{memberId}")
    public Map<String, Object> member(@PathVariable String memberId) {
        return customerRepository.member(memberId);
    }

    @GetMapping("/api/home/{userId}/work-summary")
    public Map<String, Integer> workSummary(@PathVariable String userId) {
        return Map.of("pending", 6, "followUps", 3, "completed", 12);
    }

    @GetMapping("/api/home/{userId}/activities")
    public List<Map<String, String>> activities(@PathVariable String userId) {
        return List.of(
                Map.of(
                        "timestamp", "10:20",
                        "eventName", "客户状态更新",
                        "description", "重点客户已进入商务阶段"),
                Map.of(
                        "timestamp", "09:15",
                        "eventName", "新任务分配",
                        "description", "收到 3 条客户跟进任务"));
    }
}
