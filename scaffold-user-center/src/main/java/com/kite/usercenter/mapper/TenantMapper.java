package com.kite.usercenter.mapper;

import com.kite.usercenter.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TenantMapper {
    
    int insert(Tenant tenant);
    
    int update(Tenant tenant);
    
    int deleteById(Long id);
    
    Tenant selectById(Long id);
    
    Tenant selectByCode(String code);
    
    List<Tenant> selectList(@Param("keyword") String keyword, @Param("status") Integer status);
}
