-- 清空并初始化租户/用户/角色/权限数据

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE sys_role_permission;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_permission;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE org_user_position;
TRUNCATE TABLE org_user_department;
TRUNCATE TABLE org_department_position;
TRUNCATE TABLE org_position;
TRUNCATE TABLE org_department;
TRUNCATE TABLE org_tenant;

SET FOREIGN_KEY_CHECKS = 1;

-- 确保角色表存在 tenant_id 列
SET @roleTenantCol := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_role'
      AND COLUMN_NAME = 'tenant_id'
);
SET @ddl := IF(@roleTenantCol = 0,
               'ALTER TABLE sys_role ADD COLUMN tenant_id BIGINT UNSIGNED NOT NULL DEFAULT 0 AFTER id',
               'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 确保角色权限表存在 tenant_id 列
SET @rolePermTenantCol := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_role_permission'
      AND COLUMN_NAME = 'tenant_id'
);
SET @ddl := IF(@rolePermTenantCol = 0,
               'ALTER TABLE sys_role_permission ADD COLUMN tenant_id BIGINT UNSIGNED NOT NULL DEFAULT 0 AFTER id',
               'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 角色编码唯一约束调整为 (tenant_id, code)
SET @roleCodeIdx := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_role'
      AND INDEX_NAME = 'code_UNIQUE'
);
SET @ddl := IF(@roleCodeIdx > 0,
               'ALTER TABLE sys_role DROP INDEX code_UNIQUE',
               'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @roleTenantCodeIdx := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_role'
      AND INDEX_NAME = 'uk_sys_role_tenant_code'
);
SET @ddl := IF(@roleTenantCodeIdx = 0,
               'CREATE UNIQUE INDEX uk_sys_role_tenant_code ON sys_role (tenant_id, code)',
               'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 租户示例数据
INSERT INTO org_tenant (id, code, name, contact_name, contact_phone, status, remark, create_time, update_time)
VALUES
    (1, 'TENANT_DEMO',  '演示租户',   'Demo Admin', '13800000000', 1, '示例租户', NOW(), NOW()),
    (2, 'TENANT_SALES', '销售事业部', '张三',       '13800000001', 1, '销售事业部租户', NOW(), NOW()),
    (3, 'TENANT_RND',   '研发中心',   '李四',       '13800000002', 1, '研发中心租户', NOW(), NOW());

-- 平台角色
INSERT INTO sys_role (id, tenant_id, code, name, status, description, create_time, update_time) VALUES
    (1, 0, 'admin', '系统管理员', 1, '平台管理员，拥有所有权限', NOW(), NOW());

-- 为每个租户创建租户管理员角色
INSERT INTO sys_role (tenant_id, code, name, status, description, create_time, update_time)
SELECT t.id, 'tenant_admin', '租户管理员', 1, '租户内最高权限', NOW(), NOW()
FROM org_tenant t;

-- 权限 / 菜单（平台级）
INSERT INTO sys_permission (id, code, name, type, parent_id, path, method, icon, component, visible, status, sort, create_time, update_time) VALUES
    (1,  '*:*:*',        '所有权限',   'api',   0, '*', '*', NULL,            NULL,         1, 1,   0, NOW(), NOW()),
    (2,  'menu:system',  '系统管理',   'menu',  0, '/system', NULL, 'SettingOutlined', NULL, 1, 1, 10, NOW(), NOW()),
    (3,  'menu:users',   '用户管理',   'menu',  2, '/', NULL, 'TeamOutlined', 'UserList',  1, 1, 11, NOW(), NOW()),
    (4,  'user:list',    '查看用户',   'button',3, NULL, NULL, NULL, NULL,     1, 1, 111, NOW(), NOW()),
    (5,  'user:create',  '创建用户',   'button',3, NULL, NULL, NULL, NULL,     1, 1, 112, NOW(), NOW()),
    (6,  'user:update',  '更新用户',   'button',3, NULL, NULL, NULL, NULL,     1, 1, 113, NOW(), NOW()),
    (7,  'user:delete',  '删除用户',   'button',3, NULL, NULL, NULL, NULL,     1, 1, 114, NOW(), NOW()),
    (8,  'menu:roles',   '角色管理',   'menu',  2, '/roles', NULL, 'UserSwitchOutlined', 'RoleList', 1, 1, 12, NOW(), NOW()),
    (9,  'role:list',    '查看角色',   'button',8, NULL, NULL, NULL, NULL,     1, 1, 121, NOW(), NOW()),
    (10, 'role:create',  '创建角色',   'button',8, NULL, NULL, NULL, NULL,     1, 1, 122, NOW(), NOW()),
    (11, 'role:update',  '更新角色',   'button',8, NULL, NULL, NULL, NULL,     1, 1, 123, NOW(), NOW()),
    (12, 'role:delete',  '删除角色',   'button',8, NULL, NULL, NULL, NULL,     1, 1, 124, NOW(), NOW()),
    (13, 'menu:permissions','权限配置','menu', 2, '/permissions', NULL, 'SafetyOutlined','PermissionList',1,1,13,NOW(),NOW()),
    (14, 'permission:list',  '查看权限','button',13,NULL,NULL,NULL,NULL,1,1,131,NOW(),NOW()),
    (15, 'permission:create','新增权限','button',13,NULL,NULL,NULL,NULL,1,1,132,NOW(),NOW()),
    (16, 'permission:update','编辑权限','button',13,NULL,NULL,NULL,NULL,1,1,133,NOW(),NOW()),
    (17, 'permission:delete','删除权限','button',13,NULL,NULL,NULL,NULL,1,1,134,NOW(),NOW()),
    (18, 'menu:sessions','Session 管理','menu',2,'/sessions',NULL,'ClockCircleOutlined','SessionList',1,1,14,NOW(),NOW()),
    (19, 'session:list', '查看会话', 'button',18,NULL,NULL,NULL,NULL,1,1,141,NOW(),NOW()),
    (20, 'menu:tenants', '租户管理', 'menu',  2, '/tenants', NULL, 'AppstoreOutlined', 'TenantList', 1, 1, 15, NOW(), NOW()),
    (21, 'tenant:list',  '查看租户', 'button',20,NULL,NULL,NULL,NULL,1,1,151,NOW(),NOW()),
    (22, 'tenant:create','创建租户', 'button',20,NULL,NULL,NULL,NULL,1,1,152,NOW(),NOW()),
    (23, 'tenant:update','更新租户', 'button',20,NULL,NULL,NULL,NULL,1,1,153,NOW(),NOW()),
    (24, 'tenant:delete','删除租户', 'button',20,NULL,NULL,NULL,NULL,1,1,154,NOW(),NOW()),
    (25, 'tenant:status','启停租户', 'button',20,NULL,NULL,NULL,NULL,1,1,155,NOW(),NOW());

-- 平台管理员拥有所有权限
INSERT INTO sys_role_permission (tenant_id, role_id, permission_id)
SELECT 0, r.id, p.id
FROM sys_role r
JOIN sys_permission p ON 1=1
WHERE r.tenant_id = 0 AND r.code = 'admin';

-- 租户管理员拥有除租户管理外的权限
INSERT INTO sys_role_permission (tenant_id, role_id, permission_id)
SELECT r.tenant_id, r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.code NOT IN ('menu:tenants','tenant:list','tenant:create','tenant:update','tenant:delete','tenant:status')
WHERE r.code = 'tenant_admin' AND r.tenant_id <> 0;

-- 平台管理员账号
INSERT INTO sys_user (id, tenant_id, username, password, nickname, email, phone, status, remark, create_time, update_time)
VALUES (1, 0, 'admin', '$2a$10$LdMmE9tS4tuW0D1gTENveOPfbIsg8VAjVtas.5dZmp.GOHXo7R.kK', '超级管理员', 'admin@example.com', '13800000000', 1, '平台默认管理员', NOW(), NOW());

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.code = 'admin' AND r.tenant_id = 0
WHERE u.id = 1;

-- 每个租户默认管理员账号/角色绑定
INSERT INTO sys_user (tenant_id, username, password, nickname, email, phone, status, remark, create_time, update_time)
SELECT t.id,
       CONCAT('admin_', LOWER(t.code)),
       '$2a$10$LdMmE9tS4tuW0D1gTENveOPfbIsg8VAjVtas.5dZmp.GOHXo7R.kK',
       CONCAT(t.name, '管理员'),
       CONCAT(LOWER(t.code), '@tenant.local'),
       IFNULL(t.contact_phone, '00000000000'),
       1,
       '租户默认管理员',
       NOW(),
       NOW()
FROM org_tenant t;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.code = 'tenant_admin' AND r.tenant_id = u.tenant_id
WHERE u.tenant_id <> 0;
