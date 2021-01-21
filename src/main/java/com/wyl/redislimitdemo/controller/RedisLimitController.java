package com.wyl.redislimitdemo.controller;

import com.wyl.redislimitdemo.annotation.RateLimit;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisLimitController {

    /**
     * 总容量5个令牌，一秒生成一个
     */
    @RequestMapping("/limitTest")
    @RateLimit(key = "limitTest", replenishRate = 1, burstCapacity = 5)
    public String limitTest(){
        System.out.println("执行了业务方法！");
        return "执行了业务方法！";
    }

}
