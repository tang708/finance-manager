package com.finance.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.finance.manager.mapper")
public class FinanceManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceManagerApplication.class, args);
    }
}