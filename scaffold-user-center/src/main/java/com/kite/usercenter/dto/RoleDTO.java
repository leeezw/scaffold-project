package com.kite.usercenter.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String code;
    private String name;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Long> permissionIds;
}
