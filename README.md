# 炒股复盘系统

一个专业的炒股复盘系统，帮助用户系统化地记录和分析股票操作。

## 技术栈

### 后端
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.3.1
- MySQL 8.0
- Maven

### 前端
- Vue 3.4
- Element Plus 2.4
- Vue Router 4
- Axios
- Vite 5

## 项目结构

```
stockBack0212/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/stock/review/
│   │   │   │   ├── config/     # 配置类
│   │   │   │   ├── controller/ # 控制器
│   │   │   │   ├── entity/     # 实体类
│   │   │   │   ├── mapper/     # Mapper接口
│   │   │   │   ├── service/    # 服务层
│   │   │   │   └── utils/      # 工具类
│   │   │   └── resources/
│   │   │       ├── application.yml  # 配置文件
│   │   │       └── schema.sql       # 数据库脚本
│   │   └── test/
│   └── pom.xml
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/               # API接口
│   │   ├── components/        # 公共组件
│   │   ├── router/            # 路由配置
│   │   ├── utils/             # 工具函数
│   │   ├── views/             # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## 功能模块

### 1. 活跃市值复盘
- 录入每日活跃市值数据
- 自动计算涨跌幅
- 判断市场状态（资金流入/流出/震荡）
- 提供操作提示（操作放紧/放松/保持中性）
- 可配置阈值参数

### 2. 今日操作复盘
- 记录建仓、加仓、减仓、清仓操作
- 自动更新持仓信息
- 统计纪律符合率
- 支持按日期和股票筛选

### 3. 持仓情况复盘
- 展示当前持仓详情
- 实时获取股票价格
- 计算盈亏金额和比例
- 计算盈亏比
- 支持查看已清仓记录

### 4. 选股模块
- 管理选股经验
- 录入选股记录
- 自动获取当前价格
- 计算盈亏比
- 支持股票模糊搜索

## 环境要求

- JDK 11+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

## 快速开始

### 1. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS stock_review DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行 `backend/src/main/resources/schema.sql` 创建表结构和初始化数据。

### 2. 后端启动

```bash
cd backend

# 修改数据库配置（如需要）
# 编辑 src/main/resources/application.yml
# 修改 datasource.url, username, password

# 编译运行
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev
```

前端服务将在 http://localhost:3000 启动

## API接口

### 活跃市值
- `POST /api/active-market-value/save` - 保存活跃市值
- `GET /api/active-market-value/page` - 分页查询
- `GET /api/active-market-value/latest` - 获取最新记录
- `DELETE /api/active-market-value/{id}` - 删除记录

### 操作记录
- `POST /api/stock-operation/save` - 保存操作记录
- `GET /api/stock-operation/page` - 分页查询
- `GET /api/stock-operation/statistics` - 获取统计信息
- `DELETE /api/stock-operation/{id}` - 删除记录

### 持仓管理
- `GET /api/position/page` - 分页查询持仓
- `GET /api/position/summary` - 获取持仓汇总
- `POST /api/position/refresh-prices` - 刷新价格

### 选股管理
- `POST /api/stock-selection/save` - 保存选股记录
- `GET /api/stock-selection/page` - 分页查询
- `POST /api/stock-selection/mark-operated/{id}` - 标记已操作
- `DELETE /api/stock-selection/{id}` - 删除记录

### 选股经验
- `POST /api/stock-experience/save` - 保存经验
- `GET /api/stock-experience/list` - 获取列表
- `DELETE /api/stock-experience/{id}` - 删除经验

### 股票查询
- `GET /api/stock/search?keyword=xxx` - 搜索股票（在线）
- `GET /api/stock/search-local?keyword=xxx` - 本地搜索
- `GET /api/stock/price/{stockCode}` - 获取实时价格

### 系统配置
- `GET /api/config/outflow-threshold` - 获取流出阈值
- `POST /api/config/outflow-threshold` - 设置流出阈值
- `GET /api/config/inflow-threshold` - 获取流入阈值
- `POST /api/config/inflow-threshold` - 设置流入阈值

## 外部API

系统使用腾讯财经API获取股票实时价格：
- 接口地址: `https://qt.gtimg.cn/q=股票代码`
- 股票代码格式: 沪市以sh开头(如sh600519)，深市以sz开头(如sz000001)

## 注意事项

1. 首次运行前请确保MySQL数据库已启动并正确配置连接信息
2. 股票实时价格依赖外部API，网络不稳定时可能获取失败
3. 建议在生产环境中修改数据库密码等敏感配置
