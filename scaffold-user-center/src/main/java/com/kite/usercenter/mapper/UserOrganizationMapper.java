package com.kite.usercenter.mapper;

import com.kite.usercenter.entity.UserOrganization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserOrganizationMapper {
    
    int insert(UserOrganization relation);
    
    int deleteByUserId(Long userId);
    
    List<UserOrganization> selectByUserId(Long userId);
    
    int insertBatch(@Param("relations") List<UserOrganization> relations);
}
