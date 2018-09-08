package com.springboot.aop;

import java.lang.annotation.*;

/**
 * 限流注解
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessLimiter {
    int permitsPerSecond() default -1;
}
