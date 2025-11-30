package com.kite.usercenter.service;

import com.kite.usercenter.dto.TenantDTO;
import com.kite.usercenter.dto.TenantRequest;

import java.util.List;

public interface TenantService {
    
    List<TenantDTO> list(String keyword, Integer status);
    
    TenantDTO getById(Long id);
    
    void create(TenantRequest request);
    
    void update(Long id, TenantRequest request);
    
    void delete(Long id);
}
