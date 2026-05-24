package com.finance.manager.controller;

import com.finance.manager.entity.User;
import com.finance.manager.service.UserService;
import com.finance.manager.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            User existingUser = userService.getUserByUsername(user.getUsername());
            if (existingUser != null) {
                result.put("code", 400);
                result.put("message", "用户名已存在");
                return result;
            }
            User registeredUser = userService.register(user);
            result.put("code", 200);
            result.put("message", "注册成功");
            result.put("data", registeredUser);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "注册失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            System.out.println("登录尝试 - 用户名: " + username + ", 密码: " + password);
            User user = userService.login(username, password);
            if (user != null) {
                String token = jwtUtil.generateToken(user.getUsername());
                result.put("code", 200);
                result.put("message", "登录成功");
                result.put("token", token);
                result.put("user", user);
                System.out.println("登录成功 - 用户: " + username);
            } else {
                result.put("code", 401);
                result.put("message", "用户名或密码错误");
                System.out.println("登录失败 - 用户名或密码错误");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "登录失败：" + e.getMessage());
            System.out.println("登录异常: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @GetMapping("/hash")
    public Map<String, Object> getPasswordHash(String password) {
        Map<String, Object> result = new HashMap<>();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        result.put("password", password);
        result.put("hash", hash);
        return result;
    }
}