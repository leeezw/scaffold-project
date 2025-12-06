package com.kite.organization.mapper;

import com.kite.organization.entity.DepartmentPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentPositionMapper {

    List<DepartmentPosition> selectByDepartmentId(@Param("departmentId") Long departmentId);

    int batchInsert(@Param("list") List<DepartmentPosition> relations);

    int deleteByDepartmentId(@Param("departmentId") Long departmentId);
}
