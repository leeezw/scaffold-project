package com.kite.organization.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位实体。
 */
@Data
public class Position implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private String name;
    private String code;
    private String category;
    private Integer rank;
    private Integer status;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
