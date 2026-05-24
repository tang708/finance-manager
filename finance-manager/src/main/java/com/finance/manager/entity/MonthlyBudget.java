package com.finance.manager.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_monthly_budget")
public class MonthlyBudget {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String month;
    private BigDecimal budgetAmount;
    private Date updateTime;
}