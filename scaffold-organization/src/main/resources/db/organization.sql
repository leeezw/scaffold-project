-- 租户表
CREATE TABLE IF NOT EXISTS org_tenant (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code          VARCHAR(64)  NOT NULL COMMENT '租户编码',
    name          VARCHAR(128) NOT NULL COMMENT '租户名称',
    contact_name  VARCHAR(64)  DEFAULT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(32)  DEFAULT NULL COMMENT '联系人电话',
    contact_email VARCHAR(128) DEFAULT NULL COMMENT '联系人邮箱',
    industry      VARCHAR(64)  DEFAULT NULL COMMENT '行业',
    level         VARCHAR(32)  DEFAULT NULL COMMENT '套餐等级',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-停用',
    expired_at    DATE         DEFAULT NULL COMMENT '到期日',
    config_json   JSON         DEFAULT NULL COMMENT '租户配置',
    remark        VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_by    BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
    updated_by    BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tenant_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 部门表
CREATE TABLE IF NOT EXISTS org_department (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id      BIGINT UNSIGNED NOT NULL COMMENT '租户ID',
    parent_id      BIGINT UNSIGNED DEFAULT 0 COMMENT '父级ID',
    path           VARCHAR(512)   DEFAULT '/' COMMENT '层级路径 /1/2',
    name           VARCHAR(128)   NOT NULL COMMENT '部门名称',
    code           VARCHAR(64)    NOT NULL COMMENT '部门编码',
    leader_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '负责人ID',
    leader_name    VARCHAR(64)    DEFAULT NULL COMMENT '负责人姓名',
    sort           INT            NOT NULL DEFAULT 0 COMMENT '排序号',
    status         TINYINT        NOT NULL DEFAULT 1 COMMENT '状态',
    remark         VARCHAR(255)   DEFAULT NULL COMMENT '备注',
    create_time    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_dept_code (tenant_id, code),
    KEY idx_dept_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 岗位表
CREATE TABLE IF NOT EXISTS org_position (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id   BIGINT UNSIGNED NOT NULL COMMENT '租户ID',
    name        VARCHAR(128)    NOT NULL COMMENT '岗位名称',
    code        VARCHAR(64)     NOT NULL COMMENT '岗位编码',
    category    VARCHAR(64)     DEFAULT NULL COMMENT '岗位类型',
    rank        INT             DEFAULT NULL COMMENT '职级/级别',
    status      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态',
    sort        INT             NOT NULL DEFAULT 0 COMMENT '排序',
    remark      VARCHAR(255)    DEFAULT NULL COMMENT '备注',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_position_code (tenant_id, code),
    KEY idx_position_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 部门与岗位关联
CREATE TABLE IF NOT EXISTS org_department_position (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id     BIGINT UNSIGNED NOT NULL COMMENT '租户ID',
    department_id BIGINT UNSIGNED NOT NULL COMMENT '部门ID',
    position_id   BIGINT UNSIGNED NOT NULL COMMENT '岗位ID',
    quota         INT             DEFAULT NULL COMMENT '编制/人数限制',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_dept_position (tenant_id, department_id, position_id),
    KEY idx_dept_position_dept (department_id),
    KEY idx_dept_position_position (position_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门-岗位关联';

-- 用户部门关联
CREATE TABLE IF NOT EXISTS org_user_department (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id     BIGINT UNSIGNED NOT NULL COMMENT '租户ID',
    user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    department_id BIGINT UNSIGNED NOT NULL COMMENT '部门ID',
    primary_flag  TINYINT         NOT NULL DEFAULT 0 COMMENT '是否主部门',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_department (tenant_id, user_id, department_id),
    KEY idx_user_department_user (user_id),
    KEY idx_user_department_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-部门关联';

-- 用户岗位关联
CREATE TABLE IF NOT EXISTS org_user_position (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id     BIGINT UNSIGNED NOT NULL COMMENT '租户ID',
    user_id       BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    position_id   BIGINT UNSIGNED NOT NULL COMMENT '岗位ID',
    primary_flag  TINYINT         NOT NULL DEFAULT 0 COMMENT '是否主岗位',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_position (tenant_id, user_id, position_id),
    KEY idx_user_position_user (user_id),
    KEY idx_user_position_position (position_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-岗位关联';

-- 示例租户
INSERT INTO org_tenant (code, name, status, remark)
VALUES ('TENANT_DEMO', '演示租户', 1, '示例数据')
ON DUPLICATE KEY UPDATE code = code;
