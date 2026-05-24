package com.finance.manager.controller;

import com.finance.manager.entity.MonthlyBudget;
import com.finance.manager.service.MonthlyBudgetService;
import com.finance.manager.service.TransactionService;
import com.finance.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    @Autowired
    private MonthlyBudgetService monthlyBudgetService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    // 默认预警阈值（80%）
    private static final BigDecimal DEFAULT_WARNING_THRESHOLD = new BigDecimal("0.80");

    @GetMapping("/current")
    public Map<String, Object> getCurrentBudget() {
        Map<String, Object> result = new HashMap<>();
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();
            com.finance.manager.entity.User user = userService.getUserByUsername(username);
            Long userId = user.getId();

            // 获取当前月份
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // 获取本月预算
            BigDecimal budgetAmount = monthlyBudgetService.getBudgetAmount(userId, currentMonth);

            // 获取本月支出
            BigDecimal monthlyExpense = transactionService.getMonthlyExpense(userId, currentMonth);

            // 计算本月剩余
            BigDecimal remaining = budgetAmount.subtract(monthlyExpense);

            // 计算预算使用率
            BigDecimal usageRate = BigDecimal.ZERO;
            if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
                usageRate = monthlyExpense.divide(budgetAmount, 4, RoundingMode.HALF_UP);
            }

            Map<String, Object> budgetInfo = new HashMap<>();
            budgetInfo.put("month", currentMonth);
            budgetInfo.put("budgetAmount", budgetAmount);
            budgetInfo.put("expense", monthlyExpense);
            budgetInfo.put("remaining", remaining);
            budgetInfo.put("usageRate", usageRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));

            result.put("code", 200);
            result.put("message", "获取本月预算成功");
            result.put("data", budgetInfo);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取本月预算失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> setBudget(@RequestBody MonthlyBudget budget) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();
            com.finance.manager.entity.User user = userService.getUserByUsername(username);
            Long userId = user.getId();
            budget.setUserId(userId);

            // 如果未指定月份，使用当前月份
            if (budget.getMonth() == null || budget.getMonth().isEmpty()) {
                budget.setMonth(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
            }

            MonthlyBudget savedBudget = monthlyBudgetService.setBudget(budget);
            result.put("code", 200);
            result.put("message", "设置预算成功");
            result.put("data", savedBudget);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "设置预算失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping
    public Map<String, Object> getBudgetByMonth(@RequestParam String month) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();
            com.finance.manager.entity.User user = userService.getUserByUsername(username);
            Long userId = user.getId();

            // 获取指定月份预算
            BigDecimal budgetAmount = monthlyBudgetService.getBudgetAmount(userId, month);

            // 获取指定月份支出
            BigDecimal monthlyExpense = transactionService.getMonthlyExpense(userId, month);

            // 计算剩余
            BigDecimal remaining = budgetAmount.subtract(monthlyExpense);

            // 计算预算使用率
            BigDecimal usageRate = BigDecimal.ZERO;
            if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
                usageRate = monthlyExpense.divide(budgetAmount, 4, RoundingMode.HALF_UP);
            }

            Map<String, Object> budgetInfo = new HashMap<>();
            budgetInfo.put("month", month);
            budgetInfo.put("budgetAmount", budgetAmount);
            budgetInfo.put("expense", monthlyExpense);
            budgetInfo.put("remaining", remaining);
            budgetInfo.put("usageRate", usageRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));

            result.put("code", 200);
            result.put("message", "获取预算成功");
            result.put("data", budgetInfo);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取预算失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/warning")
    public Map<String, Object> getBudgetWarning(@RequestParam(required = false) BigDecimal threshold) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();
            com.finance.manager.entity.User user = userService.getUserByUsername(username);
            Long userId = user.getId();

            // 获取当前月份
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // 获取本月预算
            BigDecimal budgetAmount = monthlyBudgetService.getBudgetAmount(userId, currentMonth);

            // 获取本月支出
            BigDecimal monthlyExpense = transactionService.getMonthlyExpense(userId, currentMonth);

            // 如果没有设置预算，返回无预警
            if (budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
                Map<String, Object> warningInfo = new HashMap<>();
                warningInfo.put("hasWarning", false);
                warningInfo.put("message", "本月未设置预算");
                result.put("code", 200);
                result.put("data", warningInfo);
                return result;
            }

            // 计算预算使用率
            BigDecimal usageRate = monthlyExpense.divide(budgetAmount, 4, RoundingMode.HALF_UP);

            // 使用传入的阈值或默认阈值
            BigDecimal warningThreshold = (threshold != null) 
                ? threshold.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP) 
                : DEFAULT_WARNING_THRESHOLD;

            // 判断是否超支或达到预警阈值
            boolean hasWarning = false;
            String warningLevel = "normal"; // normal, warning, danger
            String warningMessage = "";

            if (monthlyExpense.compareTo(budgetAmount) > 0) {
                // 已超支
                hasWarning = true;
                warningLevel = "danger";
                BigDecimal overspend = monthlyExpense.subtract(budgetAmount);
                warningMessage = String.format("⚠️ 预算超支！本月已超支 ¥%.2f，预算使用率 %.2f%%", 
                    overspend, usageRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            } else if (usageRate.compareTo(warningThreshold) >= 0) {
                // 达到预警阈值
                hasWarning = true;
                warningLevel = "warning";
                BigDecimal remaining = budgetAmount.subtract(monthlyExpense);
                warningMessage = String.format("⚡ 预算预警！本月预算已使用 %.2f%%，剩余 ¥%.2f", 
                    usageRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP), remaining);
            } else {
                // 正常
                BigDecimal remaining = budgetAmount.subtract(monthlyExpense);
                warningMessage = String.format("✅ 预算正常。本月已使用 %.2f%%，剩余 ¥%.2f", 
                    usageRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP), remaining);
            }

            Map<String, Object> warningInfo = new HashMap<>();
            warningInfo.put("hasWarning", hasWarning);
            warningInfo.put("warningLevel", warningLevel);
            warningInfo.put("message", warningMessage);
            warningInfo.put("budgetAmount", budgetAmount);
            warningInfo.put("expense", monthlyExpense);
            warningInfo.put("remaining", budgetAmount.subtract(monthlyExpense));
            warningInfo.put("usageRate", usageRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            warningInfo.put("threshold", warningThreshold.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP));

            result.put("code", 200);
            result.put("message", "获取预算预警成功");
            result.put("data", warningInfo);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取预算预警失败：" + e.getMessage());
        }
        return result;
    }
}
