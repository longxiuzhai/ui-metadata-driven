package com.example.metadataui.plugin.customertags;
import com.example.metadataui.customer.detail.spi.CustomerDetailCardProvider; import org.springframework.boot.autoconfigure.condition.ConditionalOnClass; import org.springframework.context.annotation.*;
@Configuration(proxyBeanMethods=false) @ConditionalOnClass(CustomerDetailCardProvider.class) @ComponentScan(basePackageClasses=CustomerTagsPluginAutoConfiguration.class)
public class CustomerTagsPluginAutoConfiguration {}
