package com.kite.usercenter.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
public class Role implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long tenantId;
    private String code;
    private String name;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
