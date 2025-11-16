package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 创建用户请求
 */
@Data
public class UserCreateRequest {
    
    @NotBlank
    @Schema(description = "用户名", required = true)
    private String username;
    
    @NotBlank
    @Size(min = 6, message = "密码不少于6位")
    @Schema(description = "初始密码", required = true)
    private String password;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Email
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "角色ID集合")
    private List<Long> roleIds;
    
    @Schema(description = "备注")
    private String remark;
}
