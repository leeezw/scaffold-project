-- 创建租户表
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建组织/部门表
CREATE TABLE IF NOT EXISTS sys_org (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(16) NOT NULL DEFAULT 'DEPT',
    sort INT DEFAULT 0,
    path VARCHAR(255),
    leader_id BIGINT,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_org_tenant (tenant_id),
    INDEX idx_org_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 菜单/按钮表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(16) NOT NULL DEFAULT 'MENU',
    path VARCHAR(255),
    component VARCHAR(255),
    icon VARCHAR(64),
    permission VARCHAR(128),
    sort INT DEFAULT 0,
    visible TINYINT DEFAULT 1,
    status TINYINT DEFAULT 1,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_menu_parent (parent_id),
    UNIQUE KEY uk_menu_permission (permission)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 角色菜单关联
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_menu_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户组织关联
CREATE TABLE IF NOT EXISTS sys_user_org (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL,
    primary_flag TINYINT DEFAULT 0,
    position_name VARCHAR(64),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_org (user_id, org_id),
    INDEX idx_user_org_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户表增加租户及主部门
ALTER TABLE sys_user
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1 AFTER phone,
    ADD COLUMN IF NOT EXISTS primary_org_id BIGINT DEFAULT NULL AFTER tenant_id;

-- 角色表增加租户字段
ALTER TABLE sys_role
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1 AFTER name;

-- 初始化默认租户与组织
INSERT INTO sys_tenant (id, code, name, status, remark)
VALUES (1, 'default', '默认租户', 1, '系统初始化租户')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sys_org (id, tenant_id, parent_id, name, type, sort, path, status)
VALUES (1, 1, 0, '总部', 'ORG', 0, '0', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 示例菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, permission, sort, visible, status, remark)
VALUES
    (1, 0, '系统管理', 'CATALOG', '/system', NULL, NULL, 1, 1, 1, '系统菜单'),
    (2, 1, '用户管理', 'MENU', '/system/users', 'system/UserList', 'user:list', 1, 1, 1, NULL),
    (3, 2, '新增用户', 'BUTTON', NULL, NULL, 'user:create', 1, 1, 1, NULL),
    (4, 2, '编辑用户', 'BUTTON', NULL, NULL, 'user:update', 2, 1, 1, NULL),
    (5, 2, '删除用户', 'BUTTON', NULL, NULL, 'user:delete', 3, 1, 1, NULL)
ON DUPLICATE KEY UPDATE name = VALUES(name);
