/**
 * Weidai
 * Copyright (C), 2011 - 2019, 微贷网.
 */
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @version $Id: MyWebAppConfigurer.java, v 0.1 2019-03-22 reus Exp $
 * @ClassName: MyWebAppConfigurer
 * @Description:
 * @author: reus
 */
@EnableWebMvc
@Configuration
public class MyWebAppConfigurer extends WebMvcConfigurerAdapter {

    @Bean
    MyInterceptor myInterceptor() {
        return new MyInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        registry.addInterceptor(myInterceptor()).addPathPatterns("/**")
            .excludePathPatterns("/JobManager.html");
        super.addInterceptors(registry);
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/templates/");
        super.addResourceHandlers(registry);
    }
}