# 个人记账 App

> **GitHub**: https://github.com/tang708/finance-manager

前后端分离的个人财务管理系统，包含 Spring Boot 后端、Vue Web 端和微信小程序端，支持多用户权限隔离。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.x + MyBatis-Plus + Spring Security + JWT |
| Web 前端 | Vue 3 + Element Plus + Vite |
| 小程序端 | 微信原生小程序 |
| 数据库 | MySQL 8.0 |
| 文档 | Knife4j (Swagger) |

## 功能

- 用户注册/登录（JWT 认证 + BCrypt 加密）
- 账单记录（收入/支出，分类管理）
- 月度预算设置与预警
- 收支统计图表（分类占比、月度趋势）
- AI 智能助手（DeepSeek 集成，财务建议）
- 管理员用户管理

## 项目结构

```
├── finance-manager/        # Spring Boot 后端
│   └── src/main/java/com/finance/manager/
│       ├── controller/     # REST API 控制器
│       ├── service/        # 业务逻辑层
│       ├── entity/         # 数据实体
│       ├── mapper/         # MyBatis 映射器
│       └── config/         # 安全/过滤器配置
├── finance-web/            # Vue 3 Web 前端
│   └── src/views/          # 页面组件
├── finance-miniprogram/    # 微信小程序端
│   └── pages/              # 小程序页面
└── default.html            # 项目展示页
```

## 快速开始

### 后端
```bash
cd finance-manager
# 创建数据库 ceshi1，执行 init.sql
mvn spring-boot:run
```

### Web 前端
```bash
cd finance-web
npm install && npm run dev
```

### 环境变量
配置 `DB_PASSWORD`, `DEEPSEEK_API_KEY`, `DEEPSEEK_BASE_URL`
