package com.example.metadataui.plugin.behaviortrace;
import com.example.metadataui.customer.detail.spi.CustomerDetailCardProvider; import org.springframework.boot.autoconfigure.condition.ConditionalOnClass; import org.springframework.context.annotation.*;
@Configuration(proxyBeanMethods=false) @ConditionalOnClass(CustomerDetailCardProvider.class) @ComponentScan(basePackageClasses=BehaviorTracePluginAutoConfiguration.class)
public class BehaviorTracePluginAutoConfiguration {}
