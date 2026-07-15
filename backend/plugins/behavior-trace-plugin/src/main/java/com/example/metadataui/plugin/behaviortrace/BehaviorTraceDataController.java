package com.example.metadataui.plugin.behaviortrace;
import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/customers") public class BehaviorTraceDataController { @GetMapping("/{id}/traces") public List<Map<String,String>> traces(@PathVariable String id){return List.of(Map.of("timestamp","2026-07-15 14:20","eventName","浏览产品","description","查看了企业版方案"),Map.of("timestamp","2026-07-14 09:10","eventName","销售跟进","description","电话沟通需求"));} }
