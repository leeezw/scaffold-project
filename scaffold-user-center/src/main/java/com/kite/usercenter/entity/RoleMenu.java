package com.kite.usercenter.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色菜单关联
 */
@Data
public class RoleMenu implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long roleId;
    private Long menuId;
}
