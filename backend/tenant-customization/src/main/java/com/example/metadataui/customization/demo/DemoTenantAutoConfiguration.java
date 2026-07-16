package com.example.metadataui.customization.demo;

import com.example.metadataui.card.spi.CardProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(CardProvider.class)
@ComponentScan(basePackageClasses = DemoTenantAutoConfiguration.class)
public class DemoTenantAutoConfiguration {
}
