package com.springboot.controller;

import com.springboot.aop.AccessLimiter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class TestController {

    private AtomicInteger atomic = new AtomicInteger();

    @RequestMapping("/limit")
    @AccessLimiter(permitsPerSecond = 20)
    public int testAccessRateLimiter() {
        int accessTimes = atomic.incrementAndGet();
        System.err.println("===进入TestController.testAccessRateLimiter===" + accessTimes + "(次)");
        return accessTimes;
    }

}
