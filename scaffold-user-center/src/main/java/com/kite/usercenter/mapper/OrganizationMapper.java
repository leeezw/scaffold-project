package com.kite.usercenter.mapper;

import com.kite.usercenter.entity.Organization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrganizationMapper {
    
    int insert(Organization organization);
    
    int update(Organization organization);
    
    int deleteById(Long id);
    
    Organization selectById(Long id);
    
    List<Organization> selectByTenant(@Param("tenantId") Long tenantId);
    
    List<Organization> selectChildren(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId);
}
