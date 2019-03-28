package com.example.demo;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/****
 * 
 * @author sun
 * @date 2018.01.08
 */
public class MyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin") == null ? "*" : request.getHeader("Origin"));
    	response.setHeader("Access-Control-Allow-Methods", request.getHeader("Access-Control-Request-Method"));
    	response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
    	response.setHeader("Access-Control-Allow-Credentials", "true");
        return true; // 只有返回true才会继续向下执行，返回false取消当前请求
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
   
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
   
    }

}