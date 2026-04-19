ALTER TABLE stock_selection ADD COLUMN execute_time VARCHAR(20) DEFAULT NULL COMMENT '执行时段:早盘/尾盘' AFTER plan_date;

ALTER TABLE position ADD COLUMN plan_amount DECIMAL(18, 2) DEFAULT NULL COMMENT '计划金额' AFTER plan_shares;

ALTER TABLE position ADD COLUMN alert_status VARCHAR(20) DEFAULT NULL COMMENT '预警状态:stop_loss/take_profit/closed' AFTER plan_status;

ALTER TABLE position ADD COLUMN plan_create_time DATETIME DEFAULT NULL COMMENT '计划创建时间' AFTER alert_status;

SET SQL_SAFE_UPDATES = 0;
UPDATE position SET plan_create_time = update_time WHERE plan_type IS NOT NULL AND plan_create_time IS NULL;
SET SQL_SAFE_UPDATES = 1;

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
