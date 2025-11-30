package com.kite.usercenter.service;

import com.kite.usercenter.dto.PermissionDTO;
import com.kite.usercenter.dto.PermissionRequest;

import java.util.List;

public interface PermissionService {
    
    List<PermissionDTO> listAll();

    void create(PermissionRequest request);

    void update(Long id, PermissionRequest request);

    void delete(Long id);
}
