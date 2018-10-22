package com.flaming.spring.annotation;

import java.lang.annotation.*;

/**
 * @Author Flaming
 * @date 2018/10/22 12:25
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {

    String value() default "";

}
