package com.kite.usercenter.mapper;

import com.kite.usercenter.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
    
    int insert(Role role);
    
    int update(Role role);
    
    int deleteById(Long id);
    
    Role selectById(Long id);
    
    Role selectByCode(String code);
    
    List<Role> selectAll(@Param("status") Integer status);
    
    List<Role> selectByIds(@Param("ids") List<Long> ids);
}
