package com.kite.usercenter.service.impl;

import com.kite.usercenter.dto.PermissionDTO;
import com.kite.usercenter.entity.Permission;
import com.kite.usercenter.mapper.PermissionMapper;
import com.kite.usercenter.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    
    private final PermissionMapper permissionMapper;
    
    public PermissionServiceImpl(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }
    
    @Override
    public List<PermissionDTO> listAll() {
        List<Permission> permissions = permissionMapper.selectAll();
        Map<Long, List<PermissionDTO>> grouped = permissions.stream()
                .map(this::toDTO)
                .collect(Collectors.groupingBy(dto -> dto.getParentId() == null ? 0L : dto.getParentId()));
        return buildTree(0L, grouped);
    }
    
    private List<PermissionDTO> buildTree(Long parentId, Map<Long, List<PermissionDTO>> grouped) {
        List<PermissionDTO> children = grouped.getOrDefault(parentId, new ArrayList<>());
        for (PermissionDTO child : children) {
            child.setChildren(buildTree(child.getId(), grouped));
        }
        return children;
    }
    
    private PermissionDTO toDTO(Permission permission) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setName(permission.getName());
        dto.setType(permission.getType());
        dto.setParentId(permission.getParentId());
        dto.setPath(permission.getPath());
        dto.setMethod(permission.getMethod());
        dto.setStatus(permission.getStatus());
        dto.setSort(permission.getSort());
        return dto;
    }
}
