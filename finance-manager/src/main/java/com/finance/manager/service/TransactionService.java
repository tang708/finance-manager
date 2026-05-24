package com.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.finance.manager.entity.Transaction;
import com.finance.manager.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionMapper transactionMapper;

    public Transaction addTransaction(Transaction transaction) {
        transactionMapper.insert(transaction);
        return transaction;
    }

    public Transaction updateTransaction(Transaction transaction) {
        QueryWrapper<Transaction> wrapper = new QueryWrapper<>();
        wrapper.eq("id", transaction.getId()).eq("user_id", transaction.getUserId());
        
        // 检查记录是否存在且属于当前用户
        Transaction existing = transactionMapper.selectOne(wrapper);
        if (existing == null) {
            throw new RuntimeException("账单不存在或无权限修改");
        }
        
        transactionMapper.updateById(transaction);
        return transaction;
    }

    public void deleteTransaction(Long id, Long userId) {
        QueryWrapper<Transaction> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
        transactionMapper.delete(wrapper);
    }

    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionMapper.getTransactionsByUserId(userId);
    }

    public List<Transaction> getTransactionsByUserIdWithFilter(Long userId, String startDate, String endDate, String category, Integer type) {
        QueryWrapper<Transaction> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        
        // 日期范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("transaction_time", startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le("transaction_time", endDate + " 23:59:59");
        }
        
        // 分类筛选
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        
        // 类型筛选
        if (type != null) {
            wrapper.eq("type", type);
        }
        
        // 按时间倒序排列
        wrapper.orderByDesc("transaction_time");
        
        return transactionMapper.selectList(wrapper);
    }

    public List<String> getUserCategories(Long userId) {
        return transactionMapper.getUserCategories(userId);
    }

    public BigDecimal getMonthlyExpense(Long userId, String month) {
        return transactionMapper.getMonthlyExpense(userId, month);
    }

    public List<Transaction> getRecentExpenses(Long userId) {
        return transactionMapper.getRecentExpenses(userId);
    }

    public List<TransactionMapper.CategoryExpense> getCategoryExpenses(Long userId, String month) {
        return transactionMapper.getCategoryExpenses(userId, month);
    }
}
