package com.kite.usercenter.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private Integer status;
    private String avatar;
    private LocalDateTime lastLoginTime;
    private LocalDateTime pwdUpdatedAt;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
