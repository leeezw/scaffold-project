package com.kite.usercenter.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper {
    
    int insertBatch(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
    
    int deleteByUserId(Long userId);
    
    List<Long> listRoleIdsByUserId(Long userId);
}
