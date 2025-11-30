package com.kite.usercenter.service.impl;

import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import com.kite.usercenter.dto.TenantDTO;
import com.kite.usercenter.dto.TenantRequest;
import com.kite.usercenter.entity.Tenant;
import com.kite.usercenter.mapper.TenantMapper;
import com.kite.usercenter.service.TenantService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TenantServiceImpl implements TenantService {
    
    private final TenantMapper tenantMapper;
    
    public TenantServiceImpl(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }
    
    @Override
    public List<TenantDTO> list(String keyword, Integer status) {
        List<Tenant> tenants = tenantMapper.selectList(keyword, status);
        if (CollectionUtils.isEmpty(tenants)) {
            return Collections.emptyList();
        }
        return tenants.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    @Override
    public TenantDTO getById(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return toDTO(tenant);
    }
    
    @Override
    public void create(TenantRequest request) {
        Tenant exist = tenantMapper.selectByCode(request.getCode());
        if (exist != null) {
            throw new BusinessException(ResultCode.DATA_EXISTS.getCode(), "租户编码已存在");
        }
        Tenant tenant = new Tenant();
        tenant.setCode(request.getCode());
        tenant.setName(request.getName());
        tenant.setStatus(request.getStatus());
        tenant.setRemark(request.getRemark());
        tenantMapper.insert(tenant);
    }
    
    @Override
    public void update(Long id, TenantRequest request) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        tenant.setCode(request.getCode());
        tenant.setName(request.getName());
        tenant.setStatus(request.getStatus());
        tenant.setRemark(request.getRemark());
        tenantMapper.update(tenant);
    }
    
    @Override
    public void delete(Long id) {
        tenantMapper.deleteById(id);
    }
    
    private TenantDTO toDTO(Tenant tenant) {
        TenantDTO dto = new TenantDTO();
        dto.setId(tenant.getId());
        dto.setCode(tenant.getCode());
        dto.setName(tenant.getName());
        dto.setStatus(tenant.getStatus());
        dto.setRemark(tenant.getRemark());
        dto.setCreateTime(tenant.getCreateTime());
        dto.setUpdateTime(tenant.getUpdateTime());
        return dto;
    }
}
