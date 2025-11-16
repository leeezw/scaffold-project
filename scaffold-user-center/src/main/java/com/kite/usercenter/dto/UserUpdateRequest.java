package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新用户请求
 */
@Data
public class UserUpdateRequest {
    
    @NotNull
    @Schema(description = "用户ID", required = true)
    private Long id;
    
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
