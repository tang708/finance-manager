package com.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.manager.entity.MonthlyBudget;
import org.apache.ibatis.annotations.Select;

public interface MonthlyBudgetMapper extends BaseMapper<MonthlyBudget> {
    @Select("SELECT * FROM t_monthly_budget WHERE user_id = #{userId} AND month = #{month}")
    MonthlyBudget getBudgetByUserIdAndMonth(Long userId, String month);
}