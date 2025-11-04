-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    `operation_type` VARCHAR(20) DEFAULT NULL COMMENT '操作类型（新增、删除、修改、查询）',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
    `method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法（GET、POST、PUT、DELETE）',
    `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `request_params` TEXT COMMENT '请求参数',
    `response_result` TEXT COMMENT '响应结果',
    `status` TINYINT(1) DEFAULT 1 COMMENT '操作状态（0-失败，1-成功）',
    `error_msg` TEXT COMMENT '错误信息',
    `execution_time` BIGINT(20) DEFAULT NULL COMMENT '执行时间（毫秒）',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_module` (`module`),
    KEY `idx_operation_type` (`operation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

