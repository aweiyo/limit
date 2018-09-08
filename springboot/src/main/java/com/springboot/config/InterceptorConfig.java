package com.springboot.config;

import com.springboot.intercept.AccessLimiterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class InterceptorConfig extends  WebMvcConfigurerAdapter{
    @Autowired
    AccessLimiterInterceptor accessLimiterInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLimiterInterceptor).addPathPatterns("/limit/**");
    }
}
