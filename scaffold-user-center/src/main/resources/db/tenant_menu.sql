-- 租户管理菜单及权限初始化脚本
-- 适用于平台超级管理员（tenant_id = 0）默认拥有所有权限的场景

SET @system_menu_id := (SELECT id FROM sys_permission WHERE code = 'menu:system' LIMIT 1);
SET @admin_role_id  := (SELECT id FROM sys_role WHERE code = 'admin' LIMIT 1);

-- 如果没有“系统管理”父菜单，可将 parent_id 设置为 0
SET @parent_id := IFNULL(@system_menu_id, 0);

INSERT INTO sys_permission (code, name, type, parent_id, path, component, icon, visible, status, sort)
VALUES ('menu:tenants', '租户管理', 'menu', @parent_id, '/tenants', 'TenantList', 'AppstoreOutlined', 1, 1, 45)
ON DUPLICATE KEY UPDATE name = VALUES(name), path = VALUES(path), component = VALUES(component), icon = VALUES(icon), sort = VALUES(sort);

SET @tenant_menu_id := (SELECT id FROM sys_permission WHERE code = 'menu:tenants' LIMIT 1);

INSERT INTO sys_permission (code, name, type, parent_id, status, sort)
VALUES ('tenant:list', '查看租户', 'button', @tenant_menu_id, 1, 451)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sys_permission (code, name, type, parent_id, status, sort)
VALUES ('tenant:create', '创建租户', 'button', @tenant_menu_id, 1, 452)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sys_permission (code, name, type, parent_id, status, sort)
VALUES ('tenant:update', '更新租户', 'button', @tenant_menu_id, 1, 453)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sys_permission (code, name, type, parent_id, status, sort)
VALUES ('tenant:delete', '删除租户', 'button', @tenant_menu_id, 1, 454)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sys_permission (code, name, type, parent_id, status, sort)
VALUES ('tenant:status', '启停租户', 'button', @tenant_menu_id, 1, 455)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 将租户菜单及按钮授权给平台管理员角色
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT @admin_role_id, id
FROM sys_permission
WHERE code IN ('menu:tenants', 'tenant:list', 'tenant:create', 'tenant:update', 'tenant:delete', 'tenant:status')
  AND NOT EXISTS (
        SELECT 1 FROM sys_role_permission 
        WHERE role_id = @admin_role_id AND permission_id = sys_permission.id
      );
