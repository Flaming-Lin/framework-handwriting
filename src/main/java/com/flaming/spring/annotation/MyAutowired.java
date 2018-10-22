package com.flaming.spring.annotation;

import java.lang.annotation.*;

/**
 * @Author Flaming
 * @date 2018/10/22 12:28
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {

    String value() default "";

}
