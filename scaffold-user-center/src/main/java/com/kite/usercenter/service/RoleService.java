package com.kite.usercenter.service;

import com.kite.usercenter.dto.RoleDTO;
import com.kite.usercenter.dto.RolePermissionRequest;
import com.kite.usercenter.dto.RoleRequest;
import com.kite.usercenter.vo.RolePermissionVO;

import java.util.List;

public interface RoleService {
    
    List<RoleDTO> listAll();
    
    RoleDTO getById(Long id);
    
    void create(RoleRequest request);
    
    void update(Long id, RoleRequest request);
    
    void delete(Long id);
    
    void assignPermissions(Long roleId, List<Long> permissionIds);
    
    List<Long> listPermissionIds(Long roleId);

    RolePermissionVO getRolePermissions(Long roleId);

    void grantPermissions(RolePermissionRequest request);
}
