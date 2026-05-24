package com.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.manager.entity.Transaction;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionMapper extends BaseMapper<Transaction> {
    @Select("SELECT COALESCE(SUM(amount), 0) FROM t_transaction WHERE user_id = #{userId} AND type = 1 AND DATE_FORMAT(transaction_time, '%Y-%m') = #{month}")
    BigDecimal getMonthlyExpense(Long userId, String month);

    @Select("SELECT * FROM t_transaction WHERE user_id = #{userId} ORDER BY transaction_time DESC")
    List<Transaction> getTransactionsByUserId(Long userId);

    @Select("SELECT * FROM t_transaction WHERE user_id = #{userId} AND DATE(transaction_time) >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND DATE(transaction_time) <= CURDATE() AND type = 1 ORDER BY transaction_time ASC")
    List<Transaction> getRecentExpenses(Long userId);

    @Select("SELECT category, SUM(amount) as total FROM t_transaction WHERE user_id = #{userId} AND type = 1 AND DATE_FORMAT(transaction_time, '%Y-%m') = #{month} GROUP BY category")
    List<CategoryExpense> getCategoryExpenses(Long userId, String month);

    @Select("SELECT DISTINCT category FROM t_transaction WHERE user_id = #{userId} AND category IS NOT NULL AND category != '' ORDER BY category")
    List<String> getUserCategories(Long userId);

    interface CategoryExpense {
        String getCategory();
        BigDecimal getTotal();
    }
}
