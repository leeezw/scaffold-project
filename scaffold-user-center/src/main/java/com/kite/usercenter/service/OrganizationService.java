package com.kite.usercenter.service;

import com.kite.usercenter.dto.OrganizationDTO;
import com.kite.usercenter.dto.OrganizationRequest;

import java.util.List;

public interface OrganizationService {
    
    List<OrganizationDTO> tree(Long tenantId);
    
    OrganizationDTO getById(Long id);
    
    void create(OrganizationRequest request);
    
    void update(Long id, OrganizationRequest request);
    
    void delete(Long id);
}
