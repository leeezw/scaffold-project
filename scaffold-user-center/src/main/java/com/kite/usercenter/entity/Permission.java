package com.kite.usercenter.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Data
public class Permission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String code;
    private String name;
    private String type;
    private Long parentId;
    private String path;
    private String method;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
