package com.wyl.redislimitdemo.aop;

import com.wyl.redislimitdemo.annotation.RateLimit;
import com.wyl.redislimitdemo.util.IPUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Aspect
@Configuration
public class RateLimitAspect {

    @Resource
    private RedisTemplate<String, Serializable> redisTemplate;

    @Resource
    private DefaultRedisScript<Long> redisLuaScript;


    @Pointcut(value = "execution(* com.wyl.redislimitdemo.controller ..*(..) )")
    public void rateLimit() {
    }

    @Around(value = "rateLimit()")
    public Object methodAround(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取拦截的方法名
        Method method = signature.getMethod();
        //获取拦截的方法所属于的类
        Class<?> targetClass = method.getDeclaringClass();

        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        if (rateLimit != null) {

            //获取request对象
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

            //根据request获取ip
            String ipAddress = IPUtil.getIPAddress(request);

            //拼接唯一key
            String id = ipAddress + "-" + targetClass.getName() + "- " + method.getName() + "-" + rateLimit.key();

            //准备执行lua脚本需要的参数
            List<String> keys = Arrays.asList(id + ".tokens", id + ".timestamp", rateLimit.replenishRate() + "", rateLimit.burstCapacity() + "", Instant.now().getEpochSecond() + "", "1");

            Long number = redisTemplate.execute(redisLuaScript, keys);
            if (number != null && number.intValue() != 0) {
                return joinPoint.proceed();
            }
        } else {
            return joinPoint.proceed();
        }

        System.out.println("达到限流的最大阈值，禁止访问！");

        return "达到限流的最大阈值，禁止访问！";
    }

}