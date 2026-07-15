package com.example.metadataui.card.condition;
import java.lang.annotation.*;
@Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME)
public @interface CardConditional { String feature() default ""; String[] permissions() default {}; String[] tenants() default {}; }
