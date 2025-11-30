package com.kite.usercenter.service.impl;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.context.LoginUserContext;
import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import com.kite.usercenter.dto.PermissionDTO;
import com.kite.usercenter.dto.PermissionRequest;
import com.kite.usercenter.entity.Permission;
import com.kite.usercenter.mapper.PermissionMapper;
import com.kite.usercenter.mapper.RolePermissionMapper;
import com.kite.usercenter.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private static final Set<String> SUPPORTED_TYPES = new HashSet<>(Arrays.asList("menu", "button", "api"));

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper,
                                 RolePermissionMapper rolePermissionMapper) {
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public List<PermissionDTO> listAll() {
        List<Permission> permissions = permissionMapper.selectAll();
        Map<Long, List<PermissionDTO>> grouped = permissions.stream()
                .map(this::toDTO)
                .collect(Collectors.groupingBy(dto -> dto.getParentId() == null ? 0L : dto.getParentId()));
        return buildTree(0L, grouped);
    }

    @Override
    public List<PermissionDTO> listGrantedTreeForCurrentUser() {
        LoginUser loginUser = LoginUserContext.getLoginUser();
        if (loginUser == null) {
            return Collections.emptyList();
        }
        List<String> permissions = loginUser.getPermissions();
        if (permissions == null) {
            return Collections.emptyList();
        }
        if (permissions.contains("*:*:*")) {
            return listAll();
        }
        Set<String> allowedCodes = new HashSet<>(permissions);
        List<PermissionDTO> tree = listAll();
        return filterTreeRecursive(tree, allowedCodes);
    }

    @Override
    public void create(PermissionRequest request) {
        validateRequest(request);
        Permission exist = permissionMapper.selectByCode(request.getCode());
        if (exist != null) {
            throw new BusinessException(ResultCode.DATA_EXISTS.getCode(), "权限编码已存在");
        }
        Permission entity = new Permission();
        applyRequest(entity, request);
        permissionMapper.insert(entity);
    }

    @Override
    public void update(Long id, PermissionRequest request) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        validateRequest(request);
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        if (!permission.getCode().equals(request.getCode())) {
            Permission exist = permissionMapper.selectByCode(request.getCode());
            if (exist != null && !Objects.equals(exist.getId(), id)) {
                throw new BusinessException(ResultCode.DATA_EXISTS.getCode(), "权限编码已存在");
            }
        }
        applyRequest(permission, request);
        permissionMapper.update(permission);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        int children = permissionMapper.countByParentId(id);
        if (children > 0) {
            throw new BusinessException(ResultCode.FAIL.getCode(), "请先删除子节点");
        }
        rolePermissionMapper.deleteByPermissionId(id);
        permissionMapper.deleteById(id);
    }

    private void validateRequest(PermissionRequest request) {
        if (!StringUtils.hasText(request.getType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "类型不能为空");
        }
        String type = request.getType().toLowerCase();
        if (!SUPPORTED_TYPES.contains(type)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不支持的类型: " + request.getType());
        }
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "权限编码不能为空");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "权限名称不能为空");
        }
        if (request.getParentId() == null) {
            request.setParentId(0L);
        }
        if (request.getVisible() == null) {
            request.setVisible(1);
        }
        if (request.getStatus() == null) {
            request.setStatus(1);
        }
        if (request.getSort() == null) {
            request.setSort(0);
        }
    }

    private void applyRequest(Permission permission, PermissionRequest request) {
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setType(request.getType().toLowerCase());
        permission.setParentId(request.getParentId());
        permission.setPath(request.getPath());
        permission.setMethod(request.getMethod());
        permission.setIcon(request.getIcon());
        permission.setComponent(request.getComponent());
        permission.setVisible(request.getVisible());
        permission.setStatus(request.getStatus());
        permission.setSort(request.getSort());
    }

    private List<PermissionDTO> buildTree(Long parentId, Map<Long, List<PermissionDTO>> grouped) {
        List<PermissionDTO> children = grouped.getOrDefault(parentId, new ArrayList<>());
        children.sort(Comparator.comparing(PermissionDTO::getSort, Comparator.nullsFirst(Integer::compareTo)));
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
        dto.setIcon(permission.getIcon());
        dto.setComponent(permission.getComponent());
        dto.setVisible(permission.getVisible());
        dto.setStatus(permission.getStatus());
        dto.setSort(permission.getSort());
        return dto;
    }

    private List<PermissionDTO> filterTreeRecursive(List<PermissionDTO> nodes, Set<String> allowedCodes) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<PermissionDTO> result = new ArrayList<>();
        for (PermissionDTO node : nodes) {
            List<PermissionDTO> filteredChildren = filterTreeRecursive(node.getChildren(), allowedCodes);
            boolean allowed = allowedCodes.contains(node.getCode());
            if (allowed || !filteredChildren.isEmpty()) {
                node.setChildren(filteredChildren);
                result.add(node);
            }
        }
        return result;
    }
}
