package com.kite.usercenter.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织/部门实体
 */
@Data
public class Organization implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long tenantId;
    private Long parentId;
    private String name;
    /**
     * ORG/DEPT
     */
    private String type;
    private Integer sort;
    private String path;
    private Long leaderId;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
