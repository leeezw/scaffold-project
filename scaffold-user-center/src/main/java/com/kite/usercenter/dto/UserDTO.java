package com.kite.usercenter.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long tenantId;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private Integer status;
    private String avatar;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Long> roleIds;
    private List<Long> departmentIds;
    private Long primaryDepartmentId;
    private List<Long> positionIds;
    private Long primaryPositionId;
}
