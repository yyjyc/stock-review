-- 多用户支持迁移脚本
-- 执行前请备份数据库

-- 1. 创建用户表
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

-- 2. 初始化管理员账号（密码: admin123，BCrypt加密）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'ADMIN', 1);

-- 3. 个人数据表添加 user_id 字段（现有数据归属管理员 user_id=1）

-- 操作记录表
ALTER TABLE stock_operation ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_so_user_id ON stock_operation(user_id);

-- 持仓表
ALTER TABLE `position` ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_pos_user_id ON `position`(user_id);

-- 选股记录表
ALTER TABLE stock_selection ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_ss_user_id ON stock_selection(user_id);

-- 选股经验表
ALTER TABLE stock_experience ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_se_user_id ON stock_experience(user_id);

-- 选股理由库表
ALTER TABLE selection_reason ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_sr_user_id ON selection_reason(user_id);

-- 加仓减仓理由库表
ALTER TABLE adjust_reason ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_ar_user_id ON adjust_reason(user_id);

-- 交易计划表
ALTER TABLE trade_plan ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 COMMENT '用户ID' AFTER id;
CREATE INDEX idx_tp_user_id ON trade_plan(user_id);