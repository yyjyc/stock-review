-- 创建数据库
CREATE DATABASE IF NOT EXISTS stock_review DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE stock_review;

-- 活跃市值表
CREATE TABLE IF NOT EXISTS `active_market_value` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `record_date` DATE NOT NULL COMMENT '记录日期',
    `market_value` DECIMAL(18, 2) DEFAULT NULL COMMENT '活跃市值(亿)',
    `change_percent` DECIMAL(6, 2) DEFAULT NULL COMMENT '涨跌幅(%)',
    `market_status` VARCHAR(20) DEFAULT '震荡' COMMENT '市场状态:资金流出/资金流入/震荡',
    `operation_tip` VARCHAR(50) DEFAULT '保持中性' COMMENT '操作提示',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活跃市值表';

-- 股票操作记录表
CREATE TABLE IF NOT EXISTS `stock_operation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID',
    `stock_name` VARCHAR(50) NOT NULL COMMENT '股票名称',
    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型:建仓/清仓/加仓/减仓',
    `operation_amount` DECIMAL(18, 2) NOT NULL COMMENT '操作金额',
    `operation_price` DECIMAL(10, 2) NOT NULL COMMENT '操作价格',
    `operation_reason` VARCHAR(500) NOT NULL COMMENT '操作原因',
    `is_follow_rule` TINYINT DEFAULT 1 COMMENT '是否符合纪律:0-否,1-是',
    `operation_date` DATE NOT NULL COMMENT '操作日期',
    `position_id` BIGINT DEFAULT NULL COMMENT '关联持仓ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_stock_code` (`stock_code`),
    KEY `idx_operation_date` (`operation_date`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票操作记录表';

-- 持仓表
CREATE TABLE IF NOT EXISTS `position` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID',
    `stock_name` VARCHAR(50) NOT NULL COMMENT '股票名称',
    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',
    `hold_amount` DECIMAL(18, 2) DEFAULT 0.00 COMMENT '持仓金额',
    `hold_shares` INT DEFAULT 0 COMMENT '持仓股数',
    `cost_price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '成本均价',
    `current_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '当前价格',
    `target_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '目标价格',
    `stop_loss_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '止损价格',
    `profit_loss` DECIMAL(18, 2) DEFAULT 0.00 COMMENT '盈亏金额',
    `profit_loss_percent` DECIMAL(6, 2) DEFAULT 0.00 COMMENT '盈亏比例(%)',
    `profit_loss_ratio` DECIMAL(6, 2) DEFAULT NULL COMMENT '盈亏比',
    `status` VARCHAR(20) DEFAULT '持仓中' COMMENT '状态:持仓中/已清仓',
    `clear_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '清仓价格',
    `clear_profit_loss` DECIMAL(18, 2) DEFAULT NULL COMMENT '清仓盈亏',
    `clear_date` DATE DEFAULT NULL COMMENT '清仓日期',
    `plan_type` VARCHAR(20) DEFAULT NULL COMMENT '计划类型:加仓/减仓/清仓',
    `plan_shares` INT DEFAULT NULL COMMENT '计划股数',
    `plan_amount` DECIMAL(18, 2) DEFAULT NULL COMMENT '计划金额',
    `plan_reason` VARCHAR(500) DEFAULT NULL COMMENT '计划理由',
    `plan_date` DATE DEFAULT NULL COMMENT '预期执行日期',
    `execute_time` VARCHAR(20) DEFAULT NULL COMMENT '执行时段:早盘/尾盘',
    `plan_status` VARCHAR(20) DEFAULT NULL COMMENT '计划状态:待实施/已实施/未实施',
    `alert_status` VARCHAR(20) DEFAULT NULL COMMENT '预警状态:stop_loss/take_profit/closed',
    `plan_create_time` DATETIME DEFAULT NULL COMMENT '计划创建时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_stock_code` (`stock_code`),
    KEY `idx_status` (`status`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='持仓表';

-- 选股记录表
CREATE TABLE IF NOT EXISTS `stock_selection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID',
    `stock_name` VARCHAR(50) NOT NULL COMMENT '股票名称',
    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',
    `target_price` DECIMAL(10, 2) NOT NULL COMMENT '目标价格',
    `stop_loss_price` DECIMAL(10, 2) NOT NULL COMMENT '止损价格',
    `plan_amount` DECIMAL(18, 2) NOT NULL COMMENT '计划操作金额',
    `selection_reason` VARCHAR(500) NOT NULL COMMENT '选股理由',
    `current_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '当前价格',
    `profit_loss_ratio` DECIMAL(6, 2) DEFAULT NULL COMMENT '盈亏比',
    `selection_date` DATE NOT NULL COMMENT '选股日期',
    `operated` TINYINT DEFAULT 0 COMMENT '是否已操作:0-否,1-是',
    `plan_shares` INT DEFAULT NULL COMMENT '计划股数',
    `plan_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '计划价格',
    `plan_date` DATE DEFAULT NULL COMMENT '计划日期',
    `execute_time` VARCHAR(20) DEFAULT NULL COMMENT '执行时段:早盘/尾盘',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_stock_code` (`stock_code`),
    KEY `idx_selection_date` (`selection_date`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选股记录表';

-- 选股经验表
CREATE TABLE IF NOT EXISTS `stock_experience` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '经验标题',
    `content` TEXT NOT NULL COMMENT '经验内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选股经验表';

-- 选股理由库表
CREATE TABLE IF NOT EXISTS `selection_reason` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID',
    `reason_name` VARCHAR(100) NOT NULL COMMENT '理由名称',
    `reason_content` VARCHAR(500) NOT NULL COMMENT '理由内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选股理由库表';

-- 加仓减仓理由库表
CREATE TABLE IF NOT EXISTS `adjust_reason` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID',
    `reason_name` VARCHAR(100) NOT NULL COMMENT '理由名称',
    `reason_content` VARCHAR(500) NOT NULL COMMENT '理由内容',
    `reason_type` VARCHAR(20) DEFAULT 'all' COMMENT '理由类型:add-加仓,reduce-减仓,all-通用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加仓减仓理由库表';

-- 股票基础信息表
CREATE TABLE IF NOT EXISTS `stock_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',
    `stock_name` VARCHAR(50) NOT NULL COMMENT '股票名称',
    `pinyin` VARCHAR(50) DEFAULT NULL COMMENT '拼音全拼',
    `first_letter` VARCHAR(20) DEFAULT NULL COMMENT '首字母',
    `market` VARCHAR(10) DEFAULT NULL COMMENT '市场:sh/sz',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_code` (`stock_code`, `deleted`),
    KEY `idx_stock_name` (`stock_name`),
    KEY `idx_pinyin` (`pinyin`),
    KEY `idx_first_letter` (`first_letter`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票基础信息表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(255) NOT NULL COMMENT '配置值',
    `config_desc` VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 交易日历表
CREATE TABLE IF NOT EXISTS `trade_calendar` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trade_date` DATE NOT NULL COMMENT '日期',
    `is_trade_day` TINYINT NOT NULL COMMENT '是否交易日:0-否,1-是',
    `remark` VARCHAR(50) DEFAULT NULL COMMENT '备注(春节、国庆等)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_date` (`trade_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易日历表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
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

-- 交易计划表
CREATE TABLE IF NOT EXISTS `trade_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID',
    `source_type` VARCHAR(20) NOT NULL COMMENT '来源类型:position/selection',
    `source_id` BIGINT NOT NULL COMMENT '来源ID',
    `stock_name` VARCHAR(50) NOT NULL COMMENT '股票名称',
    `stock_code` VARCHAR(20) NOT NULL COMMENT '股票代码',
    `plan_type` VARCHAR(20) NOT NULL COMMENT '计划类型:建仓/加仓/减仓/清仓',
    `plan_amount` DECIMAL(18, 2) NOT NULL COMMENT '计划金额',
    `plan_shares` INT NOT NULL COMMENT '计划股数',
    `plan_price` DECIMAL(10, 2) NOT NULL COMMENT '计划价格',
    `plan_date` DATE NOT NULL COMMENT '计划日期',
    `plan_reason` VARCHAR(500) NOT NULL COMMENT '计划理由',
    `execute_time` VARCHAR(20) NOT NULL COMMENT '执行时段:早盘/尾盘',
    `plan_status` VARCHAR(20) DEFAULT '待实施' COMMENT '计划状态:待实施/已实施/未实施',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_source` (`source_type`, `source_id`),
    KEY `idx_plan_date` (`plan_date`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易计划表';

-- 初始化系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`) VALUES
('outflow_threshold', '-2.3', '资金流出阈值(%)'),
('inflow_threshold', '4', '资金流入阈值(%)');

-- 初始化管理员账号（密码: admin123，BCrypt加密）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
VALUES ('admin', '$2a$10$foKO70h//oVy3tf0Ve3qi.LWSPOj5OBi.lTr6uovbbWZeQikSmM0C', '管理员', 'ADMIN', 1);

-- 插入部分示例股票数据
INSERT INTO `stock_info` (`stock_code`, `stock_name`, `pinyin`, `first_letter`, `market`) VALUES
('600519', '贵州茅台', 'guizhoumaotai', 'GZMT', 'sh'),
('000001', '平安银行', 'pinganyinhang', 'PAYH', 'sz'),
('600036', '招商银行', 'zhaoshangyinhang', 'ZSYH', 'sh'),
('000002', '万科A', 'wankeA', 'WKA', 'sz'),
('601318', '中国平安', 'zhongguopingan', 'ZGPA', 'sh'),
('600276', '恒瑞医药', 'hengruiyiyao', 'HRYY', 'sh'),
('000333', '美的集团', 'meidejituan', 'MDJT', 'sz'),
('600887', '伊利股份', 'yiligufen', 'YLCF', 'sh'),
('002415', '海康威视', 'haikangweishi', 'HKWS', 'sz'),
('300750', '宁德时代', 'ningdeshidai', 'NDSD', 'sz'),
('600900', '长江电力', 'changjiangdianli', 'CJDL', 'sh'),
('601166', '兴业银行', 'xingyeyinhang', 'XYYH', 'sh'),
('000858', '五粮液', 'wuliangye', 'WLY', 'sz'),
('002352', '顺丰控股', 'shunfengkonggu', 'SFKG', 'sz'),
('600309', '万华化学', 'wanhuahuaxue', 'WHHX', 'sh'),
('601888', '中国中免', 'zhongguozhongmian', 'ZGZM', 'sh'),
('000568', '泸州老窖', 'luzhoulaojiao', 'LZLJ', 'sz'),
('002714', '牧原股份', 'muyuangufen', 'MYGF', 'sz'),
('600809', '山西汾酒', 'shanxifenjiu', 'SXFJ', 'sh'),
('601012', '隆基绿能', 'longjilvneng', 'LJLN', 'sh');