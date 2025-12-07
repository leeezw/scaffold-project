package com.kite.usercenter.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper {
    
    int insertBatch(@Param("tenantId") Long tenantId,
                    @Param("roleId") Long roleId,
                    @Param("permissionIds") List<Long> permissionIds);
    
    int deleteByRoleId(@Param("roleId") Long roleId, @Param("tenantId") Long tenantId);
    
    List<Long> listPermissionIdsByRoleId(@Param("roleId") Long roleId, @Param("tenantId") Long tenantId);

    List<Long> listPermissionIdsByRoleIds(@Param("roleIds") List<Long> roleIds, @Param("tenantId") Long tenantId);

    int deleteByPermissionId(Long permissionId);
}
