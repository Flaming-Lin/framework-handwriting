package com.flaming.spring.annotation;

import java.lang.annotation.*;

/**
 * @Author Flaming
 * @date 2018/10/22 12:23
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {

    String value() default "";

}
