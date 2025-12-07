package com.kite.usercenter.service.impl;

import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import com.kite.organization.config.TenantContextHolder;
import com.kite.usercenter.dto.PermissionDTO;
import com.kite.usercenter.dto.RoleDTO;
import com.kite.usercenter.dto.RolePermissionRequest;
import com.kite.usercenter.dto.RoleRequest;
import com.kite.usercenter.entity.Role;
import com.kite.usercenter.mapper.PermissionMapper;
import com.kite.usercenter.mapper.RoleMapper;
import com.kite.usercenter.mapper.RolePermissionMapper;
import com.kite.usercenter.service.PermissionService;
import com.kite.usercenter.service.RoleService;
import com.kite.usercenter.vo.RolePermissionVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;
    
    public RoleServiceImpl(RoleMapper roleMapper,
                           RolePermissionMapper rolePermissionMapper,
                           PermissionService permissionService,
                           PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionService = permissionService;
        this.permissionMapper = permissionMapper;
    }
    
    @Override
    public List<RoleDTO> listAll() {
        Long tenantId = currentTenantId();
        return roleMapper.selectAll(null, tenantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoleDTO getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        ensureSameTenant(role);
        return toDTO(role);
    }
    
    @Override
    public void create(RoleRequest request) {
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "角色编码不能为空");
        }
        Long tenantId = currentTenantId();
        Role exist = roleMapper.selectByCode(request.getCode(), tenantId);
        if (exist != null) {
            throw new BusinessException(ResultCode.DATA_EXISTS.getCode(), "角色编码已存在");
        }
        Role role = new Role();
        role.setTenantId(tenantId);
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        roleMapper.insert(role);
        assignPermissions(role.getId(), tenantId, request.getPermissionIds());
    }
    
    @Override
    public void update(Long id, RoleRequest request) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        ensureSameTenant(role);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        roleMapper.update(role);
        assignPermissions(id, role.getTenantId(), request.getPermissionIds());
    }
    
    @Override
    public void delete(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        ensureSameTenant(role);
        roleMapper.deleteById(id);
        rolePermissionMapper.deleteByRoleId(id, role.getTenantId());
    }
    
    @Override
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        ensureSameTenant(role);
        assignPermissions(roleId, role.getTenantId(), permissionIds);
    }

    private void assignPermissions(Long roleId, Long tenantId, List<Long> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId, tenantId);
        List<Long> finalIds = enrichPermissionIds(permissionIds);
        if (!CollectionUtils.isEmpty(finalIds)) {
            rolePermissionMapper.insertBatch(tenantId, roleId, finalIds);
        }
    }
    
    @Override
    public List<Long> listPermissionIds(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        ensureSameTenant(role);
        List<Long> ids = rolePermissionMapper.listPermissionIdsByRoleId(roleId, role.getTenantId());
        return ids != null ? ids : Collections.emptyList();
    }

    @Override
    public RolePermissionVO getRolePermissions(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        ensureSameTenant(role);
        RolePermissionVO vo = new RolePermissionVO();
        List<PermissionDTO> filteredTree = permissionService.listGrantedTreeForCurrentUser();
        vo.setTree(filteredTree);
        List<Long> permissionIds = rolePermissionMapper.listPermissionIdsByRoleId(roleId, role.getTenantId());
        Set<Long> allowedIds = collectIds(filteredTree);
        vo.setCheckedKeys(permissionIds.stream()
                .filter(id -> allowedIds.contains(id))
                .collect(Collectors.toList()));
        return vo;
    }

    @Override
    public void grantPermissions(RolePermissionRequest request) {
        if (request == null || request.getRoleId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "角色ID不能为空");
        }
        Role role = roleMapper.selectById(request.getRoleId());
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        ensureSameTenant(role);
        assignPermissions(request.getRoleId(), role.getTenantId(), request.getPermissionIds());
    }
    
    private RoleDTO toDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setTenantId(role.getTenantId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setStatus(role.getStatus());
        dto.setDescription(role.getDescription());
        dto.setCreateTime(role.getCreateTime());
        dto.setUpdateTime(role.getUpdateTime());
        dto.setPermissionIds(rolePermissionMapper.listPermissionIdsByRoleId(role.getId(), role.getTenantId()));
        return dto;
    }

    private Set<Long> collectIds(List<PermissionDTO> nodes) {
        Set<Long> result = new HashSet<>();
        if (CollectionUtils.isEmpty(nodes)) {
            return result;
        }
        Deque<PermissionDTO> stack = new ArrayDeque<>(nodes);
        while (!stack.isEmpty()) {
            PermissionDTO node = stack.pop();
            if (node.getId() != null) {
                result.add(node.getId());
            }
            if (!CollectionUtils.isEmpty(node.getChildren())) {
                stack.addAll(node.getChildren());
            }
        }
        return result;
    }

    private List<Long> enrichPermissionIds(List<Long> permissionIds) {
        if (CollectionUtils.isEmpty(permissionIds)) {
            return Collections.emptyList();
        }
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        Deque<Long> stack = new ArrayDeque<>();
        for (Long id : permissionIds) {
            if (id != null && result.add(id)) {
                stack.push(id);
            }
        }
        while (!stack.isEmpty()) {
            Long currentId = stack.pop();
            if (currentId == null) {
                continue;
            }
            com.kite.usercenter.entity.Permission permission = permissionMapper.selectById(currentId);
            if (permission == null) {
                continue;
            }
            Long parentId = permission.getParentId();
            if (parentId != null && parentId > 0 && result.add(parentId)) {
                stack.push(parentId);
            }
        }
        return new ArrayList<>(result);
    }

    private Long currentTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId == null ? 0L : tenantId;
    }

    private void ensureSameTenant(Role role) {
        Long tenantId = currentTenantId();
        if (!Objects.equals(role.getTenantId(), tenantId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "无权操作该角色");
        }
    }
}
