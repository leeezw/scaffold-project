package com.kite.organization.mapper;

import com.kite.organization.entity.UserDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDepartmentMapper {

    List<UserDepartment> selectByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    int insert(UserDepartment relation);

    int deleteByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
