package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class RoleRequest {
    
    @NotBlank
    @Schema(description = "角色名称", required = true)
    private String name;
    
    @NotBlank
    @Schema(description = "角色编码", required = true)
    private String code;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "状态 1-启用 0-禁用")
    private Integer status = 1;
    
    @Schema(description = "绑定的权限ID集合")
    private List<Long> permissionIds;
}
