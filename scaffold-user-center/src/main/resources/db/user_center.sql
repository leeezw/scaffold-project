-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username        VARCHAR(64)  NOT NULL COMMENT '用户名',
    password        VARCHAR(255) NOT NULL COMMENT '密码（BCrypt）',
    nickname        VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    email           VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    phone           VARCHAR(32)  DEFAULT NULL COMMENT '手机号',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    avatar          VARCHAR(255) DEFAULT NULL COMMENT '头像',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    pwd_updated_at  DATETIME     DEFAULT NULL COMMENT '密码更新时间',
    remark          VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code        VARCHAR(64) NOT NULL COMMENT '角色编码',
    name        VARCHAR(64) NOT NULL COMMENT '角色名称',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code        VARCHAR(128) NOT NULL COMMENT '权限编码',
    name        VARCHAR(128) NOT NULL COMMENT '权限名称',
    type        VARCHAR(32)  NOT NULL DEFAULT 'API' COMMENT '类型：MENU/BUTTON/API',
    parent_id   BIGINT UNSIGNED DEFAULT 0 COMMENT '父级ID',
    path        VARCHAR(255) DEFAULT NULL COMMENT '路由/URL',
    method      VARCHAR(16)  DEFAULT NULL COMMENT 'HTTP方法',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联
CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联';

-- 角色权限关联
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id       BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    permission_id BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联';

-- 初始化超级管理员
INSERT INTO sys_user (username, password, nickname, email, phone, status, remark)
VALUES ('admin', '$2a$10$Q9uTEt.q6kRXKuX3sI5YbeaUvNbK/C.jYFQki71R.plVQ1BM8DTlC', '超级管理员', 'admin@example.com', '13800000000', 1, '默认管理员')
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO sys_role (code, name, status, description)
VALUES ('admin', '系统管理员', 1, '拥有所有权限')
ON DUPLICATE KEY UPDATE code = code;

INSERT INTO sys_permission (code, name, type, parent_id, path, method, status, sort)
VALUES ('*:*:*', '所有权限', 'API', 0, '*', '*', 1, 0)
ON DUPLICATE KEY UPDATE code = code;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.code = 'admin'
AND NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = u.id AND role_id = r.id);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.code = 'admin' AND p.code = '*:*:*'
AND NOT EXISTS (SELECT 1 FROM sys_role_permission WHERE role_id = r.id AND permission_id = p.id);
