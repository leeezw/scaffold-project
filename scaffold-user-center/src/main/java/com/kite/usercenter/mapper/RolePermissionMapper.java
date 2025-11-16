package com.kite.usercenter.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper {
    
    int insertBatch(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);
    
    int deleteByRoleId(Long roleId);
    
    List<Long> listPermissionIdsByRoleId(Long roleId);
}
