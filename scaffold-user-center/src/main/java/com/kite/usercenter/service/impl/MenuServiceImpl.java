package com.kite.usercenter.service.impl;

import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import com.kite.usercenter.dto.MenuDTO;
import com.kite.usercenter.dto.MenuRequest;
import com.kite.usercenter.entity.Menu;
import com.kite.usercenter.mapper.MenuMapper;
import com.kite.usercenter.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {
    
    private final MenuMapper menuMapper;
    
    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }
    
    @Override
    public List<MenuDTO> tree(Integer status) {
        List<Menu> menus = menuMapper.selectAll(status);
        if (CollectionUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }
        Map<Long, MenuDTO> cache = menus.stream()
                .map(this::toDTO)
                .collect(Collectors.toMap(MenuDTO::getId, v -> v, (a, b) -> a, LinkedHashMap::new));
        List<MenuDTO> roots = new ArrayList<>();
        for (MenuDTO dto : cache.values()) {
            Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
            if (parentId == 0) {
                roots.add(dto);
            } else {
                MenuDTO parent = cache.get(parentId);
                if (parent == null) {
                    roots.add(dto);
                } else {
                    parent.getChildren().add(dto);
                }
            }
        }
        sortMenus(roots);
        return roots;
    }
    
    @Override
    public MenuDTO getById(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return toDTO(menu);
    }
    
    @Override
    public void create(MenuRequest request) {
        Menu menu = new Menu();
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        menu.setName(request.getName());
        menu.setType(request.getType());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setIcon(request.getIcon());
        menu.setPermission(request.getPermission());
        menu.setSort(request.getSort());
        menu.setVisible(request.getVisible());
        menu.setStatus(request.getStatus());
        menu.setRemark(request.getRemark());
        menuMapper.insert(menu);
    }
    
    @Override
    public void update(Long id, MenuRequest request) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        menu.setName(request.getName());
        menu.setType(request.getType());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setIcon(request.getIcon());
        menu.setPermission(request.getPermission());
        menu.setSort(request.getSort());
        menu.setVisible(request.getVisible());
        menu.setStatus(request.getStatus());
        menu.setRemark(request.getRemark());
        menuMapper.update(menu);
    }
    
    @Override
    public void delete(Long id) {
        List<Menu> menus = menuMapper.selectAll(null);
        boolean hasChildren = menus.stream().anyMatch(menu -> Objects.equals(menu.getParentId(), id));
        if (hasChildren) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "存在子菜单，无法删除");
        }
        menuMapper.deleteById(id);
    }
    
    private void sortMenus(List<MenuDTO> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        nodes.sort(Comparator.comparing(MenuDTO::getSort, Comparator.nullsLast(Integer::compareTo)));
        for (MenuDTO node : nodes) {
            sortMenus(node.getChildren());
        }
    }
    
    private MenuDTO toDTO(Menu menu) {
        MenuDTO dto = new MenuDTO();
        dto.setId(menu.getId());
        dto.setParentId(menu.getParentId());
        dto.setName(menu.getName());
        dto.setType(menu.getType());
        dto.setPath(menu.getPath());
        dto.setComponent(menu.getComponent());
        dto.setIcon(menu.getIcon());
        dto.setPermission(menu.getPermission());
        dto.setSort(menu.getSort());
        dto.setVisible(menu.getVisible());
        dto.setStatus(menu.getStatus());
        dto.setRemark(menu.getRemark());
        dto.setCreateTime(menu.getCreateTime());
        dto.setUpdateTime(menu.getUpdateTime());
        return dto;
    }
}
