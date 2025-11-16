package com.kite.usercenter.service.impl;

import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import com.kite.usercenter.dto.RoleDTO;
import com.kite.usercenter.dto.RoleRequest;
import com.kite.usercenter.entity.Role;
import com.kite.usercenter.mapper.RoleMapper;
import com.kite.usercenter.mapper.RolePermissionMapper;
import com.kite.usercenter.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    
    public RoleServiceImpl(RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }
    
    @Override
    public List<RoleDTO> listAll() {
        return roleMapper.selectAll(null).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoleDTO getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return toDTO(role);
    }
    
    @Override
    public void create(RoleRequest request) {
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "角色编码不能为空");
        }
        Role exist = roleMapper.selectByCode(request.getCode());
        if (exist != null) {
            throw new BusinessException(ResultCode.DATA_EXISTS.getCode(), "角色编码已存在");
        }
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        roleMapper.insert(role);
        assignPermissions(role.getId(), request.getPermissionIds());
    }
    
    @Override
    public void update(Long id, RoleRequest request) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        roleMapper.update(role);
        assignPermissions(id, request.getPermissionIds());
    }
    
    @Override
    public void delete(Long id) {
        roleMapper.deleteById(id);
        rolePermissionMapper.deleteByRoleId(id);
    }
    
    @Override
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (!CollectionUtils.isEmpty(permissionIds)) {
            rolePermissionMapper.insertBatch(roleId, permissionIds);
        }
    }
    
    @Override
    public List<Long> listPermissionIds(Long roleId) {
        List<Long> ids = rolePermissionMapper.listPermissionIdsByRoleId(roleId);
        return ids != null ? ids : Collections.emptyList();
    }
    
    private RoleDTO toDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setStatus(role.getStatus());
        dto.setDescription(role.getDescription());
        dto.setCreateTime(role.getCreateTime());
        dto.setUpdateTime(role.getUpdateTime());
        dto.setPermissionIds(listPermissionIds(role.getId()));
        return dto;
    }
}
