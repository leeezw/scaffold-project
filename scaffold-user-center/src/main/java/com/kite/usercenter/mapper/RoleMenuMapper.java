package com.kite.usercenter.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMenuMapper {
    
    int insertBatch(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
    
    int deleteByRoleId(Long roleId);
    
    List<Long> selectMenuIdsByRoleId(Long roleId);
}
