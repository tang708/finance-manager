package com.finance.manager.controller;

import com.finance.manager.entity.User;
import com.finance.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> users = userService.getAllUsers();
            result.put("code", 200);
            result.put("message", "获取用户列表成功");
            result.put("data", users);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取用户列表失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/users/{id}/status")
    public Map<String, Object> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> statusData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer status = statusData.get("status");
            userService.updateUserStatus(id, status);
            result.put("code", 200);
            result.put("message", "更新用户状态成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新用户状态失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.deleteUser(id);
            result.put("code", 200);
            result.put("message", "删除用户成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除用户失败：" + e.getMessage());
        }
        return result;
    }
}