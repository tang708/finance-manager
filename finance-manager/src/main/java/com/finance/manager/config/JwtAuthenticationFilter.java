package com.finance.manager.config;

import com.finance.manager.service.UserService;
import com.finance.manager.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        
        // 对于OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        
        // 对于公开接口直接放行
        if (requestPath.startsWith("/auth/") || requestPath.startsWith("/test/") || 
            requestPath.startsWith("/v3/api-docs/") || requestPath.startsWith("/swagger-ui/") ||
            requestPath.equals("/swagger-ui.html") || requestPath.equals("/doc.html") ||
            requestPath.startsWith("/webjars/")) {
            chain.doFilter(request, response);
            return;
        }
        
        String token = request.getHeader("Authorization");
        System.out.println("JWT Filter - 路径: " + requestPath + ", Token: " + (token != null ? "存在" : "不存在"));
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                System.out.println("JWT Filter - 解析用户名: " + username);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    com.finance.manager.entity.User user = userService.getUserByUsername(username);
                    System.out.println("JWT Filter - 查询用户: " + (user != null ? user.getUsername() : "null") + ", 状态: " + (user != null ? user.getStatus() : "N/A"));
                    
                    if (user != null && user.getStatus() == 1) {
                        String role = user.getRole() == 2 ? "ADMIN" : "USER";
                        UserDetails userDetails = User.withUsername(user.getUsername())
                                .password(user.getPassword())
                                .roles(role)
                                .build();
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("JWT Filter - 认证成功，角色: " + role);
                    } else {
                        System.out.println("JWT Filter - 用户不存在或已被禁用");
                    }
                }
            } catch (Exception e) {
                System.out.println("JWT Filter - Token解析失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("JWT Filter - 未提供有效的Token");
        }
        chain.doFilter(request, response);
    }
}