# 多用户支持规格说明

## 1. 项目背景

当前"每日复盘Z"系统为个人使用，所有数据无用户隔离，无认证鉴权机制。现需开放给多用户使用，需要实现用户认证、角色权限控制、数据隔离三大核心能力。

## 2. 现有系统分析

### 2.1 技术栈
- **后端**: Spring Boot 2.7.18 + MyBatis-Plus 3.5.3 + MySQL
- **前端**: Vue 3 + Element Plus + Vite 4
- **部署**: Docker Compose + Nginx

### 2.2 现有模块与数据归属

| 模块 | 数据归属 | 当前状态 |
|------|---------|---------|
| 活跃市值复盘 | 市场公共数据 | 无用户隔离，所有人可增删改查 |
| 今日操作复盘 | 个人用户数据 | 无用户隔离 |
| 持仓情况复盘 | 个人用户数据 | 无用户隔离 |
| 选股模块 | 个人用户数据（选股记录、选股经验） | 无用户隔离 |
| 选股理由库 | 个人用户数据（每个用户独立积累） | 无用户隔离 |
| 加仓减仓理由库 | 个人用户数据（每个用户独立积累） | 无用户隔离 |
| 系统设置 | 系统级配置 | 无权限控制 |
| 股票基础信息 | 系统级公共数据 | 无权限控制 |
| 交易日历 | 系统级公共数据 | 无权限控制 |

### 2.3 现有数据库表
- `active_market_value` - 活跃市值（无user_id）
- `stock_operation` - 操作记录（无user_id）
- `position` - 持仓记录（无user_id）
- `stock_selection` - 选股记录（无user_id）
- `stock_experience` - 选股经验（无user_id）
- `selection_reason` - 选股理由库（无user_id，需添加）
- `adjust_reason` - 加仓减仓理由库（无user_id，需添加）
- `stock_info` - 股票基础信息（公共，无需user_id）
- `system_config` - 系统配置（公共，无需user_id）
- `trade_calendar` - 交易日历（公共，无需user_id）

## 3. 需求定义

### 3.1 角色定义

| 角色 | 标识 | 说明 |
|------|------|------|
| 管理员 | ADMIN | 系统管理者，拥有所有权限 |
| 普通用户 | USER | 普通使用者，仅能操作个人数据 |

### 3.2 权限矩阵

| 功能模块 | 操作 | 管理员 | 普通用户 |
|---------|------|--------|---------|
| **活跃市值复盘** | 查看 | ✅ | ✅ |
| | 录入/保存 | ✅ | ❌ |
| | 删除 | ✅ | ❌ |
| | 重新计算状态 | ✅ | ❌ |
| | 阈值设置 | ✅ | ❌ |
| **今日操作复盘** | 查看个人数据 | ✅（全部） | ✅（仅自己） |
| | 新增/编辑/删除 | ✅（仅自己） | ✅（仅自己） |
| **持仓情况复盘** | 查看个人数据 | ✅（全部） | ✅（仅自己） |
| | 新增/编辑/删除 | ✅（仅自己） | ✅（仅自己） |
| **选股模块** | 查看个人选股记录 | ✅（全部） | ✅（仅自己） |
| | 新增/编辑/删除选股记录 | ✅（仅自己） | ✅（仅自己） |
| | 查看选股经验 | ✅ | ✅ |
| | 新增/编辑/删除选股经验 | ✅（仅自己） | ✅（仅自己） |
| | 查看选股理由库 | ✅（仅自己） | ✅（仅自己） |
| | 管理选股理由库 | ✅（仅自己） | ✅（仅自己） |
| | 查看加仓减仓理由库 | ✅（仅自己） | ✅（仅自己） |
| | 管理加仓减仓理由库 | ✅（仅自己） | ✅（仅自己） |
| **系统设置** | 阈值配置 | ✅ | ❌ |
| | 股票数据预加载 | ✅ | ❌ |
| | 查看配置 | ✅ | ❌ |
| **用户管理** | 查看用户列表 | ✅ | ❌ |
| | 创建/禁用用户 | ✅ | ❌ |
| | 重置密码 | ✅ | ❌ |

### 3.3 数据隔离规则

| 数据类型 | 隔离策略 | 说明 |
|---------|---------|------|
| 个人数据 | 用户隔离 | stock_operation, position, stock_selection, stock_experience, selection_reason, adjust_reason 按 user_id 隔离 |
| 公共数据 | 共享只读 | active_market_value 所有用户可查看，仅管理员可修改 |
| 系统配置 | 管理员专属 | system_config 仅管理员可操作 |
| 基础数据 | 共享 | stock_info, trade_calendar 所有用户可访问 |

## 4. 技术方案

### 4.1 认证方案：JWT Token

**选择理由**：
- 前后端分离架构，无状态认证更合适
- JWT 不需要服务端存储 Session，适合当前单实例部署
- 实现简单，与现有 Spring Boot 项目集成方便

**Token 策略**：
- Access Token：有效期 24 小时，用于 API 请求认证
- Token 载荷包含：userId, username, role
- Token 通过 HTTP Header `Authorization: Bearer <token>` 传递
- 前端存储在 localStorage 中

### 4.2 密码安全
- 使用 BCrypt 算法加密存储（Spring Security 默认提供）
- 密码强度要求：至少6位

### 4.3 后端架构变更

```
新增包结构：
com.stock.review
├── config/
│   ├── SecurityConfig.java        # Spring Security 配置
│   ├── JwtAuthenticationFilter.java # JWT 认证过滤器
│   └── WebConfig.java             # 保留现有 CORS 配置
├── controller/
│   ├── AuthController.java        # 登录/注册/用户信息
│   ├── UserController.java        # 用户管理（管理员）
│   └── ... (现有控制器增加权限注解)
├── entity/
│   ├── User.java                  # 用户实体
│   └── ...
├── mapper/
│   ├── UserMapper.java
│   └── ...
├── service/
│   ├── AuthService.java           # 认证服务
│   ├── UserService.java           # 用户管理服务
│   ├── JwtService.java            # JWT 工具服务
│   └── ... (现有服务增加用户隔离逻辑)
└── dto/
    ├── LoginRequest.java
    ├── LoginResponse.java
    ├── RegisterRequest.java
    └── UserInfoDTO.java
```

### 4.4 数据库变更

#### 4.4.1 新增用户表
```sql
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色:ADMIN/USER',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### 4.4.2 个人数据表增加 user_id 字段
```sql
-- 操作记录表
ALTER TABLE stock_operation ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_user_id ON stock_operation(user_id);

-- 持仓表
ALTER TABLE position ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_user_id ON position(user_id);

-- 选股记录表
ALTER TABLE stock_selection ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_user_id ON stock_selection(user_id);

-- 选股经验表
ALTER TABLE stock_experience ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_user_id ON stock_experience(user_id);

-- 选股理由库表
ALTER TABLE selection_reason ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_user_id ON selection_reason(user_id);

-- 加仓减仓理由库表
ALTER TABLE adjust_reason ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_user_id ON adjust_reason(user_id);
```

> `DEFAULT 1` 表示将现有数据归属给 ID 为 1 的管理员用户。

#### 4.4.3 初始化管理员账号
```sql
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
VALUES ('admin', '$2a$10$...BCrypt加密后的密码...', '管理员', 'ADMIN', 1);
```

### 4.5 前端架构变更

#### 4.5.1 新增页面与组件
- `views/login/index.vue` - 登录页面
- `views/admin/UserManagement.vue` - 用户管理页面（管理员）

#### 4.5.2 新增工具模块
- `utils/auth.js` - Token 管理（存储、获取、删除）
- `store/user.js` - 用户状态管理（使用 Vue3 reactive 简易状态管理，不引入额外依赖）

#### 4.5.3 路由守卫
- 未登录用户重定向到登录页
- 管理员专属路由权限控制

#### 4.5.4 请求拦截器改造
- 请求拦截：自动附加 Authorization Header
- 响应拦截：401 状态码自动跳转登录页

#### 4.5.5 UI 权限控制
- 使用自定义指令 `v-permission` 控制按钮/元素的显示隐藏
- 根据用户角色动态显示/隐藏菜单项

### 4.6 依赖变更

#### 后端新增依赖（pom.xml）
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

#### 前端无新增依赖
使用现有 Vue3 + Element Plus 即可满足需求。

## 5. API 设计

### 5.1 认证相关 API

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/auth/login | 用户登录 | 公开 |
| POST | /api/auth/register | 用户注册 | 公开 |
| GET | /api/auth/me | 获取当前用户信息 | 已登录 |
| PUT | /api/auth/password | 修改密码 | 已登录 |

### 5.2 用户管理 API（管理员）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/users | 获取用户列表 | ADMIN |
| POST | /api/users | 创建用户 | ADMIN |
| PUT | /api/users/{id}/status | 启用/禁用用户 | ADMIN |
| PUT | /api/users/{id}/password | 重置用户密码 | ADMIN |

### 5.3 现有 API 变更

所有个人数据相关 API 需自动根据当前登录用户的 user_id 过滤数据：
- 查询接口：自动添加 `WHERE user_id = ?` 条件
- 新增接口：自动设置 `user_id` 为当前用户ID
- 修改/删除接口：校验数据归属当前用户

管理员在个人数据模块中可以看到所有用户的数据（可选，后续迭代）。

## 6. 安全考虑

1. **密码安全**：BCrypt 加密存储，永不明文
2. **Token 安全**：JWT 签名密钥配置在环境变量中，不硬编码
3. **接口安全**：所有 /api/** 接口默认需要认证，仅 /api/auth/** 放行
4. **数据隔离**：Service 层强制按 user_id 过滤，防止越权访问
5. **CORS 安全**：保持现有 CORS 配置，生产环境应限制允许的域名
6. **SQL 注入**：MyBatis-Plus 参数化查询已防护
7. **XSS 防护**：前端使用 Vue 模板语法自动转义

## 7. 迁移策略

1. 执行数据库迁移脚本，创建 user 表，为个人数据表添加 user_id 字段
2. 现有数据 user_id 默认设为 1（管理员用户）
3. 初始化管理员账号
4. 部署新版本后，管理员首次登录后可创建其他用户
5. 注册功能默认开放，管理员可在系统设置中关闭开放注册
