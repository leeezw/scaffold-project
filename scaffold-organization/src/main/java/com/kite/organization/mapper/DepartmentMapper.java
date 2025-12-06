package com.kite.organization.mapper;

import com.kite.organization.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    Department selectById(@Param("id") Long id);

    List<Department> selectByTenantId(@Param("tenantId") Long tenantId);

    int insert(Department department);

    int update(Department department);

    int delete(@Param("id") Long id);
}
