package com.kite.organization.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户实体，描述租户基本信息及订阅状态。
 */
@Data
public class Tenant implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String name;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String industry;
    private String level;
    private Integer status;
    private LocalDate expiredAt;
    private String configJson;
    private String remark;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
