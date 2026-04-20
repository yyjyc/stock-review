# 多用户支持 - 任务清单

## 阶段一：后端基础设施（认证与权限）

### 1.1 数据库迁移
- [ ] 创建 `migration_multi_user.sql` 迁移脚本
  - 创建 `user` 用户表
  - `stock_operation` 表添加 `user_id` 字段及索引
  - `position` 表添加 `user_id` 字段及索引
  - `stock_selection` 表添加 `user_id` 字段及索引
  - `stock_experience` 表添加 `user_id` 字段及索引
  - `selection_reason` 表添加 `user_id` 字段及索引
  - `adjust_reason` 表添加 `user_id` 字段及索引
  - 初始化管理员账号（admin/默认密码）
  - 现有数据 user_id 设为 1

### 1.2 后端依赖引入
- [ ] pom.xml 添加 Spring Security 依赖
- [ ] pom.xml 添加 JJWT (JWT) 依赖

### 1.3 用户实体与数据层
- [ ] 创建 `User.java` 实体类
- [ ] 创建 `UserMapper.java` Mapper 接口

### 1.4 JWT 工具服务
- [ ] 创建 `JwtService.java`
  - 生成 Token（包含 userId, username, role）
  - 解析 Token
  - 验证 Token 有效性
  - 从 Token 提取用户信息

### 1.5 Spring Security 配置
- [ ] 创建 `SecurityConfig.java`
  - 配置 HTTP 安全规则
  - 放行 `/api/auth/**` 路径
  - 其他路径需要认证
  - 禁用 CSRF（前后端分离）
  - 配置无状态 Session
- [ ] 创建 `JwtAuthenticationFilter.java`
  - 从请求头提取 Token
  - 验证 Token 并设置 SecurityContext
- [ ] 修改 `WebConfig.java`（CORS 与 Security 兼容）

### 1.6 认证服务与控制器
- [ ] 创建 DTO 类
  - `LoginRequest.java`（username, password）
  - `LoginResponse.java`（token, userInfo）
  - `RegisterRequest.java`（username, password, nickname）
  - `UserInfoDTO.java`（id, username, nickname, role, status）
- [ ] 创建 `AuthService.java` 及实现
  - 登录：验证用户名密码，生成 Token
  - 注册：创建新用户（默认 USER 角色）
  - 获取当前用户信息
  - 修改密码
- [ ] 创建 `AuthController.java`
  - POST `/api/auth/login`
  - POST `/api/auth/register`
  - GET `/api/auth/me`
  - PUT `/api/auth/password`

### 1.7 用户管理服务（管理员）
- [ ] 创建 `UserService.java` 及实现
  - 获取用户列表
  - 创建用户
  - 启用/禁用用户
  - 重置用户密码
- [ ] 创建 `UserController.java`
  - GET `/api/users`
  - POST `/api/users`
  - PUT `/api/users/{id}/status`
  - PUT `/api/users/{id}/password`

### 1.8 权限注解与工具
- [ ] 创建 `RequireRole.java` 自定义注解（可选，也可用 Spring Security 原生注解）
- [ ] 创建 `SecurityUtils.java` 工具类
  - 获取当前登录用户 ID
  - 获取当前登录用户角色
  - 判断是否管理员

---

## 阶段二：后端数据隔离与权限控制

### 2.1 实体类添加 userId 字段
- [ ] `StockOperation.java` 添加 `userId` 字段
- [ ] `Position.java` 添加 `userId` 字段
- [ ] `StockSelection.java` 添加 `userId` 字段
- [ ] `StockExperience.java` 添加 `userId` 字段
- [ ] `SelectionReason.java` 添加 `userId` 字段
- [ ] `AdjustReason.java` 添加 `userId` 字段

### 2.2 DTO 类适配
- [ ] `StockOperationDTO.java` 无需传 userId（后端自动填充）
- [ ] `PositionDTO.java` 无需传 userId
- [ ] `StockSelectionDTO.java` 无需传 userId
- [ ] `StockExperienceDTO.java` 无需传 userId

### 2.3 Service 层数据隔离改造
- [ ] `StockOperationServiceImpl.java`
  - 查询方法添加 userId 过滤
  - 保存方法自动填充 userId
  - 删除方法校验数据归属
- [ ] `PositionServiceImpl.java`
  - 查询方法添加 userId 过滤
  - 初始化/新增持仓自动填充 userId
  - 修改/删除校验数据归属
  - 汇总统计按 userId 过滤
- [ ] `StockSelectionServiceImpl.java`
  - 查询方法添加 userId 过滤
  - 保存方法自动填充 userId
  - 删除/标记操作校验数据归属
- [ ] `StockExperienceServiceImpl.java`
  - 查询方法添加 userId 过滤
  - 保存方法自动填充 userId
  - 删除方法校验数据归属
- [ ] `SelectionReasonServiceImpl.java`
  - 查询方法添加 userId 过滤（每个用户只看到自己的理由库）
  - 保存方法自动填充 userId
  - 删除方法校验数据归属
- [ ] `AdjustReasonServiceImpl.java`
  - 查询方法添加 userId 过滤（每个用户只看到自己的理由库）
  - 保存方法自动填充 userId
  - 删除方法校验数据归属

### 2.4 Controller 层权限控制
- [ ] `ActiveMarketValueController.java`
  - save 接口：仅 ADMIN
  - delete 接口：仅 ADMIN
  - recalculate 接口：仅 ADMIN
  - page/latest/recent 接口：所有已登录用户
- [ ] `SystemConfigController.java`
  - 所有接口：仅 ADMIN
- [ ] `StockController.java`
  - preload 接口：仅 ADMIN
  - search/search-local/price/batch-price：所有已登录用户
- [ ] `SelectionReasonController.java`
  - save/delete 接口：已登录用户，Service 层按 userId 隔离
  - list 接口：已登录用户，Service 层按 userId 隔离
- [ ] `AdjustReasonController.java`
  - save/delete 接口：已登录用户，Service 层按 userId 隔离
  - list 接口：已登录用户，Service 层按 userId 隔离
- [ ] `StockOperationController.java` - 已登录用户，Service 层自动隔离
- [ ] `PositionController.java` - 已登录用户，Service 层自动隔离
- [ ] `StockSelectionController.java` - 已登录用户，Service 层自动隔离
- [ ] `StockExperienceController.java` - 已登录用户，Service 层自动隔离
- [ ] `TradeCalendarController.java` - 所有已登录用户
- [ ] `PlanController.java` - 已登录用户，Service 层自动隔离

---

## 阶段三：前端改造

### 3.1 认证工具模块
- [ ] 创建 `utils/auth.js`
  - getToken() / setToken() / removeToken()
  - Token 存储在 localStorage
- [ ] 创建 `store/user.js`（简易状态管理）
  - 用户信息存储（id, username, nickname, role）
  - isLoggedIn 状态
  - isAdmin 计算属性
  - login/logout/setUserInfo 方法

### 3.2 请求拦截器改造
- [ ] 修改 `utils/request.js`
  - 请求拦截：自动附加 `Authorization: Bearer <token>`
  - 响应拦截：401 状态码清除 Token 并跳转登录页

### 3.3 登录页面
- [ ] 创建 `views/login/index.vue`
  - 用户名/密码表单
  - 登录按钮
  - 注册入口
- [ ] 创建 `views/login/Register.vue`（或合并到登录页）
  - 用户名/密码/昵称表单
  - 注册按钮

### 3.4 路由改造
- [ ] 修改 `router/index.js`
  - 添加 `/login` 路由
  - 添加 `/admin/users` 路由（管理员用户管理）
  - 添加路由守卫（beforeEach）
    - 未登录重定向到 /login
    - 管理员路由权限校验

### 3.5 App.vue 改造
- [ ] 侧边栏菜单权限控制
  - 系统设置菜单：仅管理员可见
  - 显示当前登录用户信息
  - 退出登录按钮
- [ ] 复盘引导进度存储改造
  - localStorage key 加入 userId 前缀，避免多用户共用浏览器时冲突

### 3.6 各页面权限适配
- [ ] `active-market/index.vue`
  - 非管理员：隐藏提交表单、删除按钮、阈值设置按钮
  - 非管理员：仅展示表格数据
- [ ] `settings/index.vue`
  - 非管理员：隐藏整个页面或显示无权限提示
  - 添加用户管理入口（管理员）
- [ ] `selection/index.vue`
  - 选股理由库和加仓减仓理由库：所有用户可管理自己的数据，无需隐藏按钮
  - 数据按用户隔离，Service 层自动处理
- [ ] `position/index.vue`
  - 理由维护对话框：所有用户可管理自己的加仓减仓理由，无需隐藏按钮
  - 数据按用户隔离，Service 层自动处理

### 3.7 权限指令
- [ ] 创建 `directives/permission.js`
  - `v-permission="['ADMIN']"` 指令
  - 根据用户角色控制元素显示/隐藏

### 3.8 用户管理页面（管理员）
- [ ] 创建 `views/admin/UserManagement.vue`
  - 用户列表表格
  - 创建用户对话框
  - 启用/禁用用户
  - 重置密码

### 3.9 API 模块新增
- [ ] 创建 `api/auth.js`
  - login / register / getMe / changePassword
- [ ] 创建 `api/user.js`
  - getUserList / createUser / updateUserStatus / resetPassword

---

## 阶段四：配置与部署

### 4.1 后端配置
- [ ] `application.yml` 添加 JWT 配置
  - jwt.secret（从环境变量读取）
  - jwt.expiration（Token 有效期）
- [ ] `application-prod.yml` 同步更新

### 4.2 Docker/部署配置
- [ ] `.env.example` 添加 JWT_SECRET 环境变量
- [ ] `docker-compose.yml` 传递环境变量
- [ ] `Dockerfile.backend` 无需变更

### 4.3 Nginx 配置
- [ ] 确认 SPA 路由回退配置正确（新增 /login 等路由）

---

## 阶段五：测试与验证

### 5.1 后端测试
- [ ] 认证流程测试：登录、注册、Token 验证
- [ ] 权限控制测试：管理员/普通用户访问不同接口
- [ ] 数据隔离测试：不同用户数据互不可见
- [ ] 边界测试：Token 过期、无效 Token、无权限访问

### 5.2 前端测试
- [ ] 登录流程测试
- [ ] 路由守卫测试：未登录跳转
- [ ] 权限 UI 测试：按钮/菜单按角色显示
- [ ] 数据隔离测试：切换用户后数据正确

### 5.3 迁移验证
- [ ] 现有数据正确归属管理员
- [ ] 管理员登录后可查看所有历史数据
