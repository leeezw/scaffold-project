package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求体，需要在 Header 中传递 X-Tenant-Id。
 */
@Schema(description = "用户登录请求体，需要在 Header 中传递 X-Tenant-Id 指定租户")
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
