package com.finance.manager.controller;

import com.finance.manager.entity.Transaction;
import com.finance.manager.service.TransactionService;
import com.finance.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @GetMapping
    public Map<String, Object> getTransactions(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer type) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();
            com.finance.manager.entity.User user = userService.getUserByUsername(username);
            Long userId = user.getId();
            List<Transaction> transactions = transactionService.getTransactionsByUserIdWithFilter(userId, startDate, endDate, category, type);
            result.put("code", 200);
            result.put("message", "获取账单列表成功");
            result.put("data", transactions);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取账单列表失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> addTransaction(@RequestBody Transaction transaction) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();
            com.finance.manager.entity.User user = userService.getUserByUsername(username);
            Long userId = user.getId();
            transaction.setUserId(userId);
            Transaction savedTransaction = transactionService.addTransaction(transaction);
            result.put("code", 200);
            result.put("message", "添加账单成功");
            result.put("data", savedTransaction);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加账单失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();
            com.finance.manager.entity.User user = userService.getUserByUsername(username);
            Long userId = user.getId();
            transaction.setId(id);
            transaction.setUserId(userId);
            Transaction updatedTransaction = transactionService.updateTransaction(transaction);
            result.put("code", 200);
            result.put("message", "更新账单成功");
            result.put("data", updatedTransaction);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新账单失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteTransaction(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();
            com.finance.manager.entity.User user = userService.getUserByUsername(username);
            Long userId = user.getId();
            transactionService.deleteTransaction(id, userId);
            result.put("code", 200);
            result.put("message", "删除账单成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除账单失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/categories")
    public Map<String, Object> getCategories() {
        Map<String, Object> result = new HashMap<>();
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();
            com.finance.manager.entity.User user = userService.getUserByUsername(username);
            Long userId = user.getId();
            List<String> categories = transactionService.getUserCategories(userId);
            result.put("code", 200);
            result.put("message", "获取分类列表成功");
            result.put("data", categories);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取分类列表失败：" + e.getMessage());
        }
        return result;
    }
}
