package com.kite.organization.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户与岗位关联。
 */
@Data
public class UserPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private Long userId;
    private Long positionId;
    private Boolean primaryFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
