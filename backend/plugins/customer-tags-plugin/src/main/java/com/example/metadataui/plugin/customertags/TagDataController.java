package com.example.metadataui.plugin.customertags;
import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/customers") public class TagDataController { @GetMapping("/{id}/tags") public List<Map<String,String>> tags(@PathVariable String id){return List.of(Map.of("name","重点客户","color","#2563eb"),Map.of("name","高活跃","color","#059669"),Map.of("name","华东区","color","#7c3aed"));} }
