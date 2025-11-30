package com.kite.usercenter.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户实体
 */
@Data
public class Tenant implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String code;
    private String name;
    /**
     * 1-启用 0-禁用
     */
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
