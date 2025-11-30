-- 将前端 AppLayout 菜单同步到 sys_menu
-- 该脚本可以多次执行，通过 permission 唯一键实现幂等更新

INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, permission, sort, visible, status, remark)
VALUES
    (100, 0, '用户管理', 'MENU', '/', NULL, 'HomeOutlined', 'menu:users', 1, 1, 1, 'Synced from AppLayout'),
    (101, 0, 'Session管理', 'MENU', '/sessions', NULL, 'ClockCircleOutlined', 'menu:sessions', 2, 1, 1, 'Synced from AppLayout'),
    (102, 0, '系统管理', 'CATALOG', '/system', NULL, 'AppstoreOutlined', 'menu:system', 3, 1, 1, 'Synced from AppLayout'),
    (103, 102, '权限管理', 'CATALOG', '/system/access', NULL, 'TeamOutlined', 'menu:permission-group', 1, 1, 1, 'AppLayout 分组'),
    (104, 103, '角色管理', 'MENU', '/roles', NULL, 'TeamOutlined', 'menu:roles', 1, 1, 1, 'Synced from AppLayout'),
    (105, 103, '权限配置', 'MENU', '/permissions', NULL, 'SafetyOutlined', 'menu:permissions', 2, 1, 1, 'Synced from AppLayout'),
    (106, 0, '通知中心', 'CATALOG', '/notifications', NULL, 'MailOutlined', 'menu:notifications', 4, 1, 1, 'Synced from AppLayout'),
    (107, 106, '消息通知', 'MENU', '/notifications/messages', NULL, NULL, 'menu:notifications:messages', 1, 1, 1, 'Synced from AppLayout'),
    (108, 106, '告警通知', 'MENU', '/notifications/alerts', NULL, NULL, 'menu:notifications:alerts', 2, 1, 1, 'Synced from AppLayout')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    parent_id = VALUES(parent_id),
    type = VALUES(type),
    path = VALUES(path),
    component = VALUES(component),
    icon = VALUES(icon),
    sort = VALUES(sort),
    visible = VALUES(visible),
    status = VALUES(status),
    remark = VALUES(remark);
