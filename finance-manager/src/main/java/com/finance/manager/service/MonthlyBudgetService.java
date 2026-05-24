package com.finance.manager.service;

import com.finance.manager.entity.MonthlyBudget;
import com.finance.manager.mapper.MonthlyBudgetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MonthlyBudgetService {
    @Autowired
    private MonthlyBudgetMapper monthlyBudgetMapper;

    public MonthlyBudget setBudget(MonthlyBudget budget) {
        MonthlyBudget existingBudget = monthlyBudgetMapper.getBudgetByUserIdAndMonth(budget.getUserId(), budget.getMonth());
        if (existingBudget != null) {
            existingBudget.setBudgetAmount(budget.getBudgetAmount());
            monthlyBudgetMapper.updateById(existingBudget);
            return existingBudget;
        } else {
            monthlyBudgetMapper.insert(budget);
            return budget;
        }
    }

    public MonthlyBudget getBudgetByUserIdAndMonth(Long userId, String month) {
        return monthlyBudgetMapper.getBudgetByUserIdAndMonth(userId, month);
    }

    public BigDecimal getBudgetAmount(Long userId, String month) {
        MonthlyBudget budget = getBudgetByUserIdAndMonth(userId, month);
        return budget != null ? budget.getBudgetAmount() : BigDecimal.ZERO;
    }
}