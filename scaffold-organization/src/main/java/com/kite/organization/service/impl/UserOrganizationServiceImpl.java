package com.kite.organization.service.impl;

import com.kite.organization.entity.UserDepartment;
import com.kite.organization.entity.UserPosition;
import com.kite.organization.mapper.UserDepartmentMapper;
import com.kite.organization.mapper.UserPositionMapper;
import com.kite.organization.service.UserOrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserOrganizationServiceImpl implements UserOrganizationService {

    private final UserDepartmentMapper userDepartmentMapper;
    private final UserPositionMapper userPositionMapper;

    public UserOrganizationServiceImpl(UserDepartmentMapper userDepartmentMapper,
                                       UserPositionMapper userPositionMapper) {
        this.userDepartmentMapper = userDepartmentMapper;
        this.userPositionMapper = userPositionMapper;
    }

    @Override
    public void bindDepartments(Long tenantId, Long userId, List<Long> departmentIds, Long primaryDepartmentId) {
        userDepartmentMapper.deleteByUserId(tenantId, userId);
        if (CollectionUtils.isEmpty(departmentIds)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long deptId : departmentIds) {
            UserDepartment relation = new UserDepartment();
            relation.setTenantId(tenantId);
            relation.setUserId(userId);
            relation.setDepartmentId(deptId);
            relation.setPrimaryFlag(primaryDepartmentId != null && primaryDepartmentId.equals(deptId));
            relation.setCreateTime(now);
            relation.setUpdateTime(now);
            userDepartmentMapper.insert(relation);
        }
    }

    @Override
    public List<Long> listDepartmentIds(Long tenantId, Long userId) {
        List<UserDepartment> relations = userDepartmentMapper.selectByUserId(tenantId, userId);
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyList();
        }
        return relations.stream()
                .map(UserDepartment::getDepartmentId)
                .collect(Collectors.toList());
    }

    @Override
    public Long getPrimaryDepartmentId(Long tenantId, Long userId) {
        List<UserDepartment> relations = userDepartmentMapper.selectByUserId(tenantId, userId);
        if (CollectionUtils.isEmpty(relations)) {
            return null;
        }
        return relations.stream()
                .filter(UserDepartment::getPrimaryFlag)
                .map(UserDepartment::getDepartmentId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void bindPositions(Long tenantId, Long userId, List<Long> positionIds, Long primaryPositionId) {
        userPositionMapper.deleteByUserId(tenantId, userId);
        if (CollectionUtils.isEmpty(positionIds)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long positionId : positionIds) {
            UserPosition relation = new UserPosition();
            relation.setTenantId(tenantId);
            relation.setUserId(userId);
            relation.setPositionId(positionId);
            relation.setPrimaryFlag(primaryPositionId != null && primaryPositionId.equals(positionId));
            relation.setCreateTime(now);
            relation.setUpdateTime(now);
            userPositionMapper.insert(relation);
        }
    }

    @Override
    public List<Long> listPositionIds(Long tenantId, Long userId) {
        List<UserPosition> relations = userPositionMapper.selectByUserId(tenantId, userId);
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyList();
        }
        return relations.stream()
                .map(UserPosition::getPositionId)
                .collect(Collectors.toList());
    }

    @Override
    public Long getPrimaryPositionId(Long tenantId, Long userId) {
        List<UserPosition> relations = userPositionMapper.selectByUserId(tenantId, userId);
        if (CollectionUtils.isEmpty(relations)) {
            return null;
        }
        return relations.stream()
                .filter(UserPosition::getPrimaryFlag)
                .map(UserPosition::getPositionId)
                .findFirst()
                .orElse(null);
    }
}
