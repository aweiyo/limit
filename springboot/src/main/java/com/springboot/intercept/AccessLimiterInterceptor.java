package com.springboot.intercept;

import com.google.common.util.concurrent.RateLimiter;
import com.springboot.aop.AccessLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class AccessLimiterInterceptor implements HandlerInterceptor {

    // 装载url和RateLimiter的map（根据不同的url配置不同的限流策略）
    private ConcurrentHashMap<String, RateLimiter> containerMap = new ConcurrentHashMap<>();
    private Lock lock = new ReentrantLock();
    private AtomicInteger accessCount = new AtomicInteger();
    private AtomicInteger mapInitCount = new AtomicInteger();
    private AtomicInteger perimitsCount = new AtomicInteger();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.err.println("===进入AccessLimiterInterceptor==" + accessCount.incrementAndGet() + "(次)");
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        AccessLimiter annotation = method.getAnnotation(AccessLimiter.class);
        if (annotation != null) {
            String uri = request.getRequestURI();
            int permitsPerSecond = annotation.permitsPerSecond();
            // 判断是否可以访问
            if (tryAcquire(uri, permitsPerSecond)) {
                throw new RuntimeException("访问超过限制了，请稍后重试");
            }
        }
        return true;
    }

    private boolean tryAcquire(String uri, int permitsPerSecond) {
        boolean result = true;
        if (permitsPerSecond < 0) {
            return result;
        }
        // 1、添加URL
        addLimiterUrl(uri, permitsPerSecond);
        // 2、获取访问权限
        return checkPermits(uri);
    }

    /**
     * 将URL和限制条件加进map
     *
     * @param uri
     * @param permitsPerSecond
     */
    private void addLimiterUrl(String uri, int permitsPerSecond) {
        try {
            lock.lock();
            if (!containerMap.containsKey(uri)) {
                System.err.println("===mapInitCount==" + mapInitCount.incrementAndGet() + "(次)");
                containerMap.put(uri, RateLimiter.create(permitsPerSecond));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检查访问速率是否超过阀值
     *
     * @param uri
     * @return
     */
    private boolean checkPermits(String uri) {
        boolean result = true;
        RateLimiter rateLimiter = containerMap.get(uri);
        if (rateLimiter.tryAcquire()) {
            // 没有超过阀值，则获取一个访问的令牌
            System.err.println("===perimitsCount==" + perimitsCount.incrementAndGet() + "(次)");
            rateLimiter.acquire();
            result = false;
        }
        return result;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
