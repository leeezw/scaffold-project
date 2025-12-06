package com.kite.organization.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门实体，支持多级组织树。
 */
@Data
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private Long parentId;
    private String path;
    private String name;
    private String code;
    private Long leaderUserId;
    private String leaderName;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
