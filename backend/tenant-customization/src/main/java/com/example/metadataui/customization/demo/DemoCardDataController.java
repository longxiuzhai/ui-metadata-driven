package com.example.metadataui.customization.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DemoCardDataController {
    @GetMapping("/api/customers/{id}/basic")
    public Map<String, Object> basic(@PathVariable String id) {
        return Map.of("name", "示例客户 " + id, "mobile", "13800138000", "status", "ACTIVE");
    }

    @GetMapping("/api/customers/{id}/tags")
    public List<Map<String, String>> tags(@PathVariable String id) {
        return List.of(Map.of("name", "重点客户", "color", "#2563eb"),
                Map.of("name", "高活跃", "color", "#059669"), Map.of("name", "华东区", "color", "#7c3aed"));
    }

    @GetMapping("/api/customers/{id}/traces")
    public List<Map<String, String>> traces(@PathVariable String id) {
        return List.of(
                Map.of(
                        "timestamp", "2026-07-15 14:20",
                        "eventName", "浏览产品",
                        "description", "查看了企业版方案"),
                Map.of(
                        "timestamp", "2026-07-14 09:10",
                        "eventName", "销售跟进",
                        "description", "电话沟通需求"));
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
