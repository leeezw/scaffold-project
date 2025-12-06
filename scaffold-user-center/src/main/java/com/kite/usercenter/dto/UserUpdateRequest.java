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
    
    @Schema(description = "用户ID")
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

    @Schema(description = "部门ID集合")
    private List<Long> departmentIds;

    @Schema(description = "主部门ID")
    private Long primaryDepartmentId;

    @Schema(description = "岗位ID集合")
    private List<Long> positionIds;

    @Schema(description = "主岗位ID")
    private Long primaryPositionId;
    
    @Schema(description = "备注")
    private String remark;
}
