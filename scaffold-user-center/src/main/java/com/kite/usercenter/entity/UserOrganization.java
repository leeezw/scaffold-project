package com.kite.usercenter.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户组织关联
 */
@Data
public class UserOrganization implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long userId;
    private Long orgId;
    private Boolean primaryFlag;
    private String positionName;
    private LocalDateTime createTime;
}
