package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 重置密码请求
 */
@Data
public class ResetPasswordRequest {
    
    @NotNull
    @Schema(description = "用户ID", required = true)
    private Long userId;
    
    @NotBlank
    @Size(min = 6, message = "密码不少于6位")
    @Schema(description = "新密码", required = true)
    private String newPassword;
}
