package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.example.rbac.mapper", "com.example.metadataui.customization.demo.mapper"})
public class MetadataUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetadataUiApplication.class, args);
    }
}
