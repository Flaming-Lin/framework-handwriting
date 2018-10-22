package com.flaming.spring.annotation;

import java.lang.annotation.*;

/**
 * @Author Flaming
 * @date 2018/10/22 12:29
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {

    String value() default "";

}
