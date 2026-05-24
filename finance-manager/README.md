# 个人财务管理系统 (Finance Manager)

## 项目概述

本项目是一个前后端分离的全栈 Java 项目，包含 Android 用户端与后端管理接口。核心功能为个人账单管理、月度预算规划与数据统计，支持多用户权限隔离。

## 技术栈

- **后端**: Spring Boot 3.x + MyBatis-Plus + Spring Security + JWT
- **数据库**: MySQL 8.0
- **Android 端**: Java + Retrofit + OkHttp + Gson + MPAndroidChart
- **工具**: Maven + Knife4j (接口文档)

## 项目结构

```
finance-manager/
├── src/
│   ├── main/
│   │   ├── java/com/finance/manager/
│   │   │   ├── controller/     # 控制器
│   │   │   ├── service/         # 服务层
│   │   │   ├── mapper/          # 数据访问层
│   │   │   ├── entity/          # 实体类
│   │   │   ├── config/          # 配置类
│   │   │   ├── util/            # 工具类
│   │   │   └── FinanceManagerApplication.java  # 主应用类
│   │   └── resources/
│   │       ├── application.yml  # 应用配置
│   │       └── init.sql         # 数据库初始化脚本
│   └── test/                    # 测试代码
├── pom.xml                      # Maven 配置文件
└── README.md                    # 项目说明
```

## 核心功能

### 认证与授权
- 用户注册
- 用户登录（返回 JWT Token）

### 普通用户端
- 账单管理（查看、新增、删除）
- 月度预算设置
- 数据统计（支出分类占比、近7天支出趋势）

### 管理员端
- 用户管理（查看、禁用/启用用户）
- 系统统计

## 接口清单

### 认证接口
- POST /auth/register - 用户注册
- POST /auth/login - 登录

### 账单接口（需 Token）
- GET /api/transactions - 获取我的账单列表
- POST /api/transactions - 新增账单
- DELETE /api/transactions/{id} - 删除账单

### 预算接口（需 Token）
- GET /api/budgets/current - 获取本月预算及剩余
- POST /api/budgets - 设置/修改本月预算

### 管理员接口（需 Token + Admin 角色）
- GET /api/admin/users - 获取用户列表
- PUT /api/admin/users/{id}/status - 禁用/启用用户

## 数据库设计

### 1. 用户表 (t_user)
- id: BIGINT (主键)
- username: VARCHAR(50) (唯一)
- password: VARCHAR(100) (BCrypt 加密)
- nickname: VARCHAR(50)
- role: TINYINT (1-普通用户，2-管理员)
- status: TINYINT (1-正常，0-禁用)
- create_time: DATETIME

### 2. 交易记录表 (t_transaction)
- id: BIGINT (主键)
- user_id: BIGINT (所属用户 ID)
- title: VARCHAR(100) (标题/备注)
- amount: DECIMAL(10,2) (金额)
- type: TINYINT (1-支出，2-收入)
- category: VARCHAR(50) (分类)
- transaction_time: DATETIME (交易时间)
- create_time: DATETIME

### 3. 月度预算表 (t_monthly_budget)
- id: BIGINT (主键)
- user_id: BIGINT (所属用户 ID)
- month: VARCHAR(7) (月份，格式：yyyy-MM)
- budget_amount: DECIMAL(10,2) (计划花费金额)
- update_time: DATETIME

## 运行方法

1. **初始化数据库**
   - 执行 `src/main/resources/init.sql` 脚本创建数据库和表结构
   - 数据库名：ceshi1

2. **配置数据库连接**
   - 修改 `src/main/resources/application.yml` 中的数据库连接信息

3. **构建项目**
   ```bash
   mvn clean package
   ```

4. **运行项目**
   ```bash
   java -jar target/finance-manager-1.0-SNAPSHOT.jar
   ```

5. **访问接口文档**
   - 启动项目后，访问 `http://localhost:8080/swagger-ui.html` 查看接口文档

## 默认账号

- **管理员**: username=admin, password=admin
- **普通用户**: 需注册
- 
- 
- 前端进入路径，终端输入npm run dev启动