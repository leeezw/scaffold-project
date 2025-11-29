package com.kite.authenticator.context;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录用户信息
 * 
 * @author yourname
 */
@Data
public class LoginUser implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String avatar;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 用户角色列表
     */
    private List<String> roles;
    
    /**
     * 用户权限列表
     */
    private List<String> permissions;
    
    /**
     * Token 过期时间（时间戳）
     */
    private Long expireAt;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 设备类型
     */
    private String deviceType;
}

