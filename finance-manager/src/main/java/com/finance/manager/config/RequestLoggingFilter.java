package com.finance.manager.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        System.out.println("========== 请求日志 ==========");
        System.out.println("请求方法: " + httpRequest.getMethod());
        System.out.println("请求路径: " + httpRequest.getRequestURI());
        System.out.println("请求来源: " + httpRequest.getHeader("Origin"));
        System.out.println("Authorization: " + httpRequest.getHeader("Authorization"));
        System.out.println("============================");
        
        chain.doFilter(request, response);
    }
}
