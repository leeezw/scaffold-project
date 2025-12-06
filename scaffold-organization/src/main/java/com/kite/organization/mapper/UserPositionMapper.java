package com.kite.organization.mapper;

import com.kite.organization.entity.UserPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPositionMapper {

    List<UserPosition> selectByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    int insert(UserPosition relation);

    int deleteByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
