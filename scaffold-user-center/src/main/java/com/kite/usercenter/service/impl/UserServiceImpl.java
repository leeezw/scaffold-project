package com.kite.usercenter.service.impl;

import com.kite.authenticator.service.SessionManagementService;
import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import com.kite.common.util.PageResult;
import com.kite.organization.config.TenantContextHolder;
import com.kite.organization.service.UserOrganizationService;
import com.kite.usercenter.dto.*;
import com.kite.usercenter.entity.User;
import com.kite.usercenter.mapper.UserMapper;
import com.kite.usercenter.mapper.UserRoleMapper;
import com.kite.usercenter.service.UserService;
import com.kite.usercenter.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final SessionManagementService sessionManagementService;
    private final UserOrganizationService userOrganizationService;
    
    @Autowired
    public UserServiceImpl(UserMapper userMapper,
                           UserRoleMapper userRoleMapper,
                           @Autowired(required = false) SessionManagementService sessionManagementService,
                           @Autowired(required = false) UserOrganizationService userOrganizationService) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.sessionManagementService = sessionManagementService;
        this.userOrganizationService = userOrganizationService;
    }
    
    @Override
    public UserPageResponse pageUsers(UserPageRequest request) {
        Long tenantId = requireTenantId();
        int pageNum = request.getPageNum() != null && request.getPageNum() > 0 ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;
        int offset = (pageNum - 1) * pageSize;
        
        // 获取排序字段和排序方向
        String sortField = request.getSortField() != null ? request.getSortField() : "createTime";
        String sortOrder = request.getSortOrder() != null ? request.getSortOrder() : "desc";
        
        // 查询分页数据
        List<User> list = userMapper.selectPage(
            tenantId,
            request.getKeyword(), 
            request.getStatus(), 
            sortField,
            sortOrder,
            offset, 
            pageSize
        );

        // 查询总数和统计信息
        long total = userMapper.count(tenantId, request.getKeyword(), request.getStatus());
        long enabledCount = userMapper.countEnabled(tenantId, request.getKeyword());
        long disabledCount = userMapper.countDisabled(tenantId, request.getKeyword());
        long todayNewCount = userMapper.countTodayNew(tenantId, request.getKeyword());
        
        // 转换为 DTO
        List<UserDTO> dtoList = list.stream().map(this::convertToDTO).collect(Collectors.toList());
        
        // 构建响应对象
        UserPageResponse response = new UserPageResponse();
        response.setPageData(PageResult.of(dtoList, total, pageNum, pageSize));
        response.setTotal(total);
        response.setEnabledCount(enabledCount);
        response.setDisabledCount(disabledCount);
        response.setTodayNewCount(todayNewCount);
        
        return response;
    }
    
    @Override
    public UserDTO getUserDetail(Long id) {
        User user = userMapper.selectById(id, requireTenantId());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return convertToDTO(user);
    }
    
    @Override
    public void createUser(UserCreateRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名和密码不能为空");
        }
        Long tenantId = requireTenantId();
        User exist = userMapper.selectByUsername(request.getUsername(), tenantId);
        if (exist != null) {
            throw new BusinessException(ResultCode.DATA_EXISTS.getCode(), "用户名已存在");
        }
        User user = new User();
        user.setTenantId(tenantId);
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.hash(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        user.setRemark(request.getRemark());
        userMapper.insert(user);
        assignRoles(user.getId(), request.getRoleIds());
        bindOrganizations(tenantId, user.getId(), request.getDepartmentIds(), request.getPrimaryDepartmentId(),
                request.getPositionIds(), request.getPrimaryPositionId());
    }
    
    @Override
    public void updateUser(UserUpdateRequest request) {
        Long tenantId = requireTenantId();
        User user = userMapper.selectById(request.getId(), tenantId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRemark(request.getRemark());
        userMapper.update(user);
        assignRoles(request.getId(), request.getRoleIds());
        bindOrganizations(tenantId, request.getId(), request.getDepartmentIds(), request.getPrimaryDepartmentId(),
                request.getPositionIds(), request.getPrimaryPositionId());
    }
    
    @Override
    public void changeStatus(ChangeStatusRequest request) {
        User user = userMapper.selectById(request.getId(), requireTenantId());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        user.setStatus(request.getStatus());
        userMapper.update(user);
        if (sessionManagementService != null && request.getStatus() != null && request.getStatus() == 0) {
            sessionManagementService.disableUser(user.getId());
        }
    }
    
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userMapper.selectById(request.getUserId(), requireTenantId());
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        user.setPassword(PasswordUtils.hash(request.getNewPassword()));
        user.setPwdUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        if (sessionManagementService != null) {
            sessionManagementService.disableUser(user.getId());
        }
    }
    
    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (!CollectionUtils.isEmpty(roleIds)) {
            userRoleMapper.insertBatch(userId, roleIds);
        }
    }
    
    @Override
    public List<Long> listRoleIds(Long userId) {
        List<Long> ids = userRoleMapper.listRoleIdsByUserId(userId);
        return ids != null ? ids : Collections.emptyList();
    }
    
    @Override
    public User findByUsername(String username) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return userMapper.selectByUsernameGlobal(username);
        }
        return userMapper.selectByUsername(username, tenantId);
    }
    
    @Override
    public User getById(Long id) {
        return userMapper.selectById(id, requireTenantId());
    }
    
    private UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setTenantId(user.getTenantId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setAvatar(user.getAvatar());
        dto.setRemark(user.getRemark());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        dto.setRoleIds(listRoleIds(user.getId()));
        if (userOrganizationService != null) {
            Long tenantId = user.getTenantId();
            dto.setDepartmentIds(userOrganizationService.listDepartmentIds(tenantId, user.getId()));
            dto.setPrimaryDepartmentId(userOrganizationService.getPrimaryDepartmentId(tenantId, user.getId()));
            dto.setPositionIds(userOrganizationService.listPositionIds(tenantId, user.getId()));
            dto.setPrimaryPositionId(userOrganizationService.getPrimaryPositionId(tenantId, user.getId()));
        }
        return dto;
    }

    private Long requireTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "租户上下文缺失，请在请求头中提供 X-Tenant-Id");
        }
        return tenantId;
    }

    private void bindOrganizations(Long tenantId,
                                   Long userId,
                                   List<Long> departmentIds,
                                   Long primaryDepartmentId,
                                   List<Long> positionIds,
                                   Long primaryPositionId) {
        if (userOrganizationService == null) {
            return;
        }
        userOrganizationService.bindDepartments(tenantId, userId, departmentIds, primaryDepartmentId);
        userOrganizationService.bindPositions(tenantId, userId, positionIds, primaryPositionId);
    }
}
