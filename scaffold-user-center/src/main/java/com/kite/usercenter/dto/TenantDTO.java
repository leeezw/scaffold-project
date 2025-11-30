package com.kite.usercenter.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TenantDTO {
    
    private Long id;
    private String code;
    private String name;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
