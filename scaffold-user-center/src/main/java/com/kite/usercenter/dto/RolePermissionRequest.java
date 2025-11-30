package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionRequest {

    @Schema(description = "角色ID", required = true)
    private Long roleId;

    @Schema(description = "权限ID集合")
    private List<Long> permissionIds;
}
