package com.kite.organization.mapper;

import com.kite.organization.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TenantMapper {

    Tenant selectById(@Param("id") Long id);

    Tenant selectByCode(@Param("code") String code);

    List<Tenant> selectList(@Param("keyword") String keyword);

    int insert(Tenant tenant);

    int update(Tenant tenant);

    int delete(@Param("id") Long id);
}
