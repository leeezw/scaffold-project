package com.kite.organization.service;

import java.util.List;

/**
 * 用户与部门/岗位的关联服务。
 */
public interface UserOrganizationService {

    void bindDepartments(Long tenantId, Long userId, List<Long> departmentIds, Long primaryDepartmentId);

    List<Long> listDepartmentIds(Long tenantId, Long userId);

    Long getPrimaryDepartmentId(Long tenantId, Long userId);

    void bindPositions(Long tenantId, Long userId, List<Long> positionIds, Long primaryPositionId);

    List<Long> listPositionIds(Long tenantId, Long userId);

    Long getPrimaryPositionId(Long tenantId, Long userId);
}
