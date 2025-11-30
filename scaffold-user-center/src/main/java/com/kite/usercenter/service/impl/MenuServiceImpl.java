package com.kite.usercenter.service.impl;

import com.kite.usercenter.entity.Permission;
import com.kite.usercenter.mapper.PermissionMapper;
import com.kite.usercenter.mapper.RolePermissionMapper;
import com.kite.usercenter.mapper.UserRoleMapper;
import com.kite.usercenter.service.MenuService;
import com.kite.usercenter.vo.MenuVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    public MenuServiceImpl(UserRoleMapper userRoleMapper,
                           RolePermissionMapper rolePermissionMapper,
                           PermissionMapper permissionMapper) {
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<MenuVO> listUserMenus(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoleMapper.listRoleIdsByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        List<Long> permissionIds = rolePermissionMapper.listPermissionIdsByRoleIds(roleIds);
        if (CollectionUtils.isEmpty(permissionIds)) {
            return Collections.emptyList();
        }
        List<Permission> permissions = permissionMapper.selectByIds(permissionIds);
        if (CollectionUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }

        Map<Long, Permission> menuMap = permissions.stream()
                .filter(this::isMenu)
                .collect(Collectors.toMap(Permission::getId, permission -> permission, (a, b) -> a, LinkedHashMap::new));

        fillMissingParents(menuMap);

        List<Permission> menuList = new ArrayList<>(menuMap.values());
        menuList.sort(this::compareMenu);
        return buildTree(menuList);
    }

    private boolean isMenu(Permission permission) {
        if (permission == null) {
            return false;
        }
        if (!"menu".equalsIgnoreCase(permission.getType())) {
            return false;
        }
        return permission.getStatus() == null || permission.getStatus() == 1;
    }

    private void fillMissingParents(Map<Long, Permission> menuMap) {
        Set<Long> toQuery = menuMap.values().stream()
                .map(Permission::getParentId)
                .filter(parentId -> parentId != null && parentId > 0 && !menuMap.containsKey(parentId))
                .collect(Collectors.toSet());

        while (!toQuery.isEmpty()) {
            List<Long> batch = new ArrayList<>(toQuery);
            toQuery.clear();
            List<Permission> parents = permissionMapper.selectByIds(batch);
            for (Permission parent : parents) {
                if (!isMenu(parent) || menuMap.containsKey(parent.getId())) {
                    continue;
                }
                menuMap.put(parent.getId(), parent);
                Long parentId = parent.getParentId();
                if (parentId != null && parentId > 0 && !menuMap.containsKey(parentId)) {
                    toQuery.add(parentId);
                }
            }
        }
    }

    private int compareMenu(Permission a, Permission b) {
        int sortCompare = Integer.compare(
                a.getSort() == null ? 0 : a.getSort(),
                b.getSort() == null ? 0 : b.getSort()
        );
        if (sortCompare != 0) {
            return sortCompare;
        }
        return Long.compare(
                a.getId() == null ? 0L : a.getId(),
                b.getId() == null ? 0L : b.getId()
        );
    }

    private List<MenuVO> buildTree(List<Permission> menus) {
        Map<Long, MenuVO> nodeMap = new LinkedHashMap<>();
        List<MenuVO> roots = new ArrayList<>();

        for (Permission permission : menus) {
            MenuVO node = toVO(permission);
            nodeMap.put(permission.getId(), node);
        }

        for (MenuVO node : nodeMap.values()) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                roots.add(node);
                continue;
            }
            MenuVO parent = nodeMap.get(parentId);
            parent.getChildren().add(node);
        }

        roots.forEach(this::sortChildren);
        roots.sort(Comparator.comparing(MenuVO::getSort, Comparator.nullsFirst(Integer::compareTo)));
        return roots;
    }

    private void sortChildren(MenuVO node) {
        if (CollectionUtils.isEmpty(node.getChildren())) {
            return;
        }
        node.getChildren().sort(Comparator.comparing(MenuVO::getSort, Comparator.nullsFirst(Integer::compareTo)));
        node.getChildren().forEach(this::sortChildren);
    }

    private MenuVO toVO(Permission permission) {
        MenuVO vo = new MenuVO();
        vo.setId(permission.getId());
        vo.setParentId(permission.getParentId());
        vo.setName(permission.getName());
        vo.setCode(permission.getCode());
        vo.setType(permission.getType());
        vo.setPath(permission.getPath());
        vo.setComponent(permission.getComponent());
        vo.setIcon(permission.getIcon());
        vo.setVisible(permission.getVisible() == null ? 1 : permission.getVisible());
        vo.setSort(permission.getSort());
        return vo;
    }
}
