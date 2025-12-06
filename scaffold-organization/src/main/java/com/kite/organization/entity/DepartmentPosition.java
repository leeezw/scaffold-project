package com.kite.organization.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门与岗位的关联关系。
 */
@Data
public class DepartmentPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private Long departmentId;
    private Long positionId;
    private Integer quota;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
