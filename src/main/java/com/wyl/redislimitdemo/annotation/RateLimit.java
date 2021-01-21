package com.wyl.redislimitdemo.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流唯一标识（请求的ip+方法类+方法名+key）
     *
     * @return
     */
    String key() default "";

    /**
     * 生成令牌的速率
     * @return
     */
    int replenishRate();

    /**
     * 总容量
     * @return
     */
    int burstCapacity();
}
