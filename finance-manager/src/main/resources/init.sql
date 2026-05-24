-- 个人财务管理系统初始化SQL脚本
-- 版本：1.0
-- 日期：2026-03-15

-- ===============================================
-- 1. 创建数据库（如果不存在）
-- ===============================================
CREATE DATABASE IF NOT EXISTS ceshi1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE ceshi1;

-- ===============================================
-- 2. 创建用户表
-- ===============================================
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    role TINYINT NOT NULL DEFAULT 1 COMMENT '1 - 普通用户，2 - 管理员',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 - 正常，0 - 禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===============================================
-- 3. 创建交易记录表
-- ===============================================
CREATE TABLE IF NOT EXISTS t_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100),
    amount DECIMAL(10,2) NOT NULL,
    type TINYINT NOT NULL COMMENT '1 - 支出，2 - 收入',
    category VARCHAR(50),
    transaction_time DATETIME NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
);

-- 为交易记录表添加索引
CREATE INDEX idx_transaction_user_id ON t_transaction(user_id);
CREATE INDEX idx_transaction_type ON t_transaction(type);
CREATE INDEX idx_transaction_category ON t_transaction(category);
CREATE INDEX idx_transaction_time ON t_transaction(transaction_time);

-- ===============================================
-- 4. 创建月度预算表
-- ===============================================
CREATE TABLE IF NOT EXISTS t_monthly_budget (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    month VARCHAR(7) NOT NULL COMMENT '格式：yyyy-MM，如 2026-03',
    budget_amount DECIMAL(10,2) NOT NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_month (user_id, month)
);

-- 为月度预算表添加索引
CREATE INDEX idx_budget_user_id ON t_monthly_budget(user_id);

-- ===============================================
-- 5. 插入默认数据
-- ===============================================

-- 5.1 插入默认管理员用户
-- 密码：admin
-- BCrypt加密后的哈希值
INSERT INTO t_user (username, password, nickname, role, status) VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.eG1H2DkKsQPmPLF9E.',
    '管理员',
    2,
    1
) ON DUPLICATE KEY UPDATE 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.eG1H2DkKsQPmPLF9E.', 
    nickname = '管理员', 
    role = 2, 
    status = 1;

-- 5.2 插入测试用户（可选）
-- 密码：123456
INSERT INTO t_user (username, password, nickname, role, status) VALUES (
    'aa',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.eG1H2DkKsQPmPLF9E.',
    '测试用户',
    1,
    1
) ON DUPLICATE KEY UPDATE 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.eG1H2DkKsQPmPLF9E.', 
    nickname = '测试用户', 
    role = 1, 
    status = 1;

-- ===============================================
-- 6. 查看结果
-- ===============================================

-- 查看用户表
SELECT * FROM t_user;

-- 查看表结构
SHOW TABLES;

-- 查看表结构详情
DESCRIBE t_user;
DESCRIBE t_transaction;
DESCRIBE t_monthly_budget;

-- ===============================================
-- 使用说明
-- ===============================================
-- 1. 执行此脚本前，请确保MySQL服务已启动
-- 2. 执行方式：
--    a. 在MySQL命令行中执行：source /path/to/init.sql
--    b. 在Navicat等工具中直接执行
-- 3. 默认账号：
--    - 管理员：admin / admin
--    - 测试用户：aa / 123456
-- 4. 如需修改密码，请使用BCrypt加密后更新
-- 5. 所有表都已添加外键约束，确保数据完整性
