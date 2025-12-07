package com.kite.usercenter.service.impl;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.service.AuthenticationService;
import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import com.kite.usercenter.entity.Permission;
import com.kite.usercenter.entity.Role;
import com.kite.usercenter.entity.User;
import com.kite.usercenter.mapper.PermissionMapper;
import com.kite.usercenter.mapper.RoleMapper;
import com.kite.usercenter.mapper.RolePermissionMapper;
import com.kite.usercenter.mapper.UserRoleMapper;
import com.kite.usercenter.service.UserService;
import com.kite.usercenter.util.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final UserService userService;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    
    public AuthenticationServiceImpl(UserService userService,
                                     UserRoleMapper userRoleMapper,
                                     RoleMapper roleMapper,
                                     RolePermissionMapper rolePermissionMapper,
                                     PermissionMapper permissionMapper) {
        this.userService = userService;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
    }
    
    @Override
    public LoginUser authenticate(String username, String password) {
        User user = userService.findByUsername(username);
        if (user == null || user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "账号不存在或已被禁用");
        }
        if (!PasswordUtils.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        return buildLoginUser(user);
    }
    
    @Override
    public LoginUser getUserById(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return null;
        }
        return buildLoginUser(user);
    }
    
    private LoginUser buildLoginUser(User user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setEmail(user.getEmail());
        loginUser.setPhone(user.getPhone());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setTenantId(user.getTenantId());
        List<Long> roleIds = userRoleMapper.listRoleIdsByUserId(user.getId());
        if (!CollectionUtils.isEmpty(roleIds)) {
            Long tenantId = user.getTenantId();
            List<Role> roles = roleMapper.selectByIds(roleIds, tenantId);
            loginUser.setRoles(roles.stream().map(Role::getCode).collect(Collectors.toList()));
            Set<Long> permissionIds = new HashSet<>();
            for (Long roleId : roleIds) {
                List<Long> ids = rolePermissionMapper.listPermissionIdsByRoleId(roleId, tenantId);
                if (!CollectionUtils.isEmpty(ids)) {
                    permissionIds.addAll(ids);
                }
            }
            if (!permissionIds.isEmpty()) {
                List<Permission> permissions = permissionMapper.selectByIds(new ArrayList<>(permissionIds));
                loginUser.setPermissions(permissions.stream()
                        .map(Permission::getCode)
                        .collect(Collectors.toList()));
            } else {
                loginUser.setPermissions(Collections.emptyList());
            }
        } else {
            loginUser.setRoles(Collections.emptyList());
            loginUser.setPermissions(Collections.emptyList());
        }
        return loginUser;
    }
}
