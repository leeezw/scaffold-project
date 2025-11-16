package com.kite.usercenter.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户角色关联
 */
@Data
public class UserRole implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long userId;
    private Long roleId;
}
