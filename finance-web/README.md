# 个人财务管理系统 - 前端

## 技术栈

- Vue 3
- Vite
- Element Plus
- Vue Router
- Pinia
- Axios
- ECharts

## 项目结构

```
finance-web/
├── src/
│   ├── api/              # API接口
│   ├── assets/           # 静态资源
│   ├── router/           # 路由配置
│   ├── stores/           # 状态管理
│   ├── utils/            # 工具函数
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── Register.vue  # 注册页
│   │   ├── Layout.vue    # 布局页
│   │   ├── Transactions.vue  # 账单管理
│   │   ├── Budget.vue    # 预算管理
│   │   ├── Statistics.vue   # 数据统计
│   │   └── Admin.vue     # 管理员页
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML模板
├── package.json          # 依赖配置
└── vite.config.js        # Vite配置
```

## 功能模块

### 1. 认证模块
- 用户登录
- 用户注册

### 2. 账单管理
- 查看账单列表
- 新增账单
- 删除账单

### 3. 预算管理
- 查看本月预算
- 设置月度预算
- 预算使用情况可视化

### 4. 数据统计
- 支出分类占比（饼图）
- 近7天支出趋势（折线图）

### 5. 管理员功能
- 用户列表查看
- 用户状态管理（启用/禁用）

## 运行步骤

### 1. 安装依赖

```bash
cd finance-web
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产版本

```bash
npm run preview
```

## 注意事项

1. **确保后端服务已启动**：前端需要连接到后端API（默认 http://localhost:8080）

2. **跨域配置**：已在 `vite.config.js` 中配置代理，开发环境下无需额外配置

3. **默认管理员账号**：
   - 用户名：admin
   - 密码：admin

4. **首次使用**：需要先注册一个普通用户账号

## 页面路由

- `/login` - 登录页
- `/register` - 注册页
- `/transactions` - 账单管理（需登录）
- `/budget` - 预算管理（需登录）
- `/statistics` - 数据统计（需登录）
- `/admin` - 用户管理（需管理员权限）

## API接口

所有API接口已在 `src/api/index.js` 中封装，包括：
- `authApi` - 认证相关接口
- `transactionApi` - 账单相关接口
- `budgetApi` - 预算相关接口
- `adminApi` - 管理员相关接口
