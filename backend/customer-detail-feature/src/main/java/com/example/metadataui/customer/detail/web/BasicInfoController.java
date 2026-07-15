package com.example.metadataui.customer.detail.web;
import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/customers") public class BasicInfoController { @GetMapping("/{id}/basic") public Map<String,Object> basic(@PathVariable String id){return Map.of("name","示例客户 "+id,"mobile","13800138000","status","ACTIVE");} }
