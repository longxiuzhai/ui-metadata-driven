package com.example.metadataui.card.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CardConditional {

    String feature() default "";

    String[] permissions() default {};

    String[] tenants() default {};
}
