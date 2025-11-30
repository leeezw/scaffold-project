package com.kite.usercenter.mapper;

import com.kite.usercenter.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper {
    
    int insert(Permission permission);
    
    int update(Permission permission);
    
    int deleteById(Long id);
    
    Permission selectById(Long id);
    
    List<Permission> selectAll();
    
    List<Permission> selectByIds(@Param("ids") List<Long> ids);

    Permission selectByCode(String code);

    int countByParentId(Long parentId);
}
