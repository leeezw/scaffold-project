package com.kite.organization.mapper;

import com.kite.organization.entity.Position;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PositionMapper {

    Position selectById(@Param("id") Long id);

    List<Position> selectByTenantId(@Param("tenantId") Long tenantId);

    int insert(Position position);

    int update(Position position);

    int delete(@Param("id") Long id);
}
