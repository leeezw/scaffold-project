package com.kite.usercenter.service;

import com.kite.usercenter.dto.MenuDTO;
import com.kite.usercenter.dto.MenuRequest;

import java.util.List;

public interface MenuService {
    
    List<MenuDTO> tree(Integer status);
    
    MenuDTO getById(Long id);
    
    void create(MenuRequest request);
    
    void update(Long id, MenuRequest request);
    
    void delete(Long id);
}
