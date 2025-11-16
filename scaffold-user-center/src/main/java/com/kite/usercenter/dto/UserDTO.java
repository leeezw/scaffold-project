package com.kite.usercenter.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
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
}
