-- 创建统一的交易计划表
CREATE TABLE IF NOT EXISTS trade_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- 计划来源
    source_type VARCHAR(20) NOT NULL COMMENT 'position/selection',
    source_id BIGINT COMMENT '关联的持仓ID或选股ID',
    
    -- 股票信息
    stock_name VARCHAR(50) NOT NULL,
    stock_code VARCHAR(20) NOT NULL,
    
    -- 计划信息
    plan_type VARCHAR(20) NOT NULL COMMENT '建仓/加仓/减仓/清仓',
    plan_shares INT,
    plan_amount DECIMAL(15,2),
    plan_price DECIMAL(10,2),
    plan_reason TEXT,
    plan_date DATE NOT NULL,
    execute_time VARCHAR(20) COMMENT '早盘/尾盘',
    
    -- 目标与止损
    target_price DECIMAL(10,2),
    stop_loss_price DECIMAL(10,2),
    
    -- 执行状态
    plan_status VARCHAR(20) NOT NULL DEFAULT '待实施' COMMENT '待实施/已实施/未实施',
    
    -- 实际执行
    actual_shares INT,
    actual_amount DECIMAL(15,2),
    actual_price DECIMAL(10,2),
    
    -- 时间戳
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    
    INDEX idx_source (source_type, source_id),
    INDEX idx_plan_date (plan_date),
    INDEX idx_stock_code (stock_code),
    INDEX idx_plan_status (plan_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易计划表';

-- 从 position 表迁移计划数据
INSERT INTO trade_plan (source_type, source_id, stock_name, stock_code, plan_type, plan_shares, plan_amount, plan_reason, plan_date, execute_time, target_price, stop_loss_price, plan_status, create_time, update_time, deleted)
SELECT 
    'position' as source_type,
    id as source_id,
    stock_name,
    stock_code,
    plan_type,
    plan_shares,
    plan_amount,
    plan_reason,
    plan_date,
    execute_time,
    target_price,
    stop_loss_price,
    COALESCE(plan_status, '待实施') as plan_status,
    create_time,
    update_time,
    deleted
FROM position 
WHERE plan_date IS NOT NULL AND plan_type IS NOT NULL;

-- 从 stock_selection 表迁移计划数据
INSERT INTO trade_plan (source_type, source_id, stock_name, stock_code, plan_type, plan_shares, plan_amount, plan_price, plan_reason, plan_date, execute_time, target_price, stop_loss_price, plan_status, create_time, update_time, deleted)
SELECT 
    'selection' as source_type,
    id as source_id,
    stock_name,
    stock_code,
    '建仓' as plan_type,
    plan_shares,
    plan_amount,
    plan_price,
    selection_reason as plan_reason,
    plan_date,
    execute_time,
    target_price,
    stop_loss_price,
    CASE 
        WHEN operated = 1 THEN '已实施'
        WHEN operated = 2 THEN '未实施'
        ELSE '待实施'
    END as plan_status,
    create_time,
    update_time,
    deleted
FROM stock_selection 
WHERE plan_date IS NOT NULL;
