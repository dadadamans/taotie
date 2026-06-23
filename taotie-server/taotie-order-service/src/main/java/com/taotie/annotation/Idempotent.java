package com.taotie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String message() default "您的请求正在处理中，请勿重复提交！";
    long timeout() default 5;
    TimeUnit timeUnit() default TimeUnit.MINUTES;
}