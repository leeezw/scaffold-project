package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PermissionRequest {

    @NotBlank(message = "权限编码不能为空")
    @Schema(description = "权限编码", required = true)
    private String code;

    @NotBlank(message = "权限名称不能为空")
    @Schema(description = "权限名称", required = true)
    private String name;

    @NotBlank(message = "类型不能为空")
    @Schema(description = "类型 menu/button/api", required = true)
    private String type;

    @Schema(description = "父级ID")
    private Long parentId = 0L;

    @Schema(description = "路由/URL")
    private String path;

    @Schema(description = "HTTP 方法")
    private String method;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "前端组件")
    private String component;

    @Schema(description = "是否显示 1-显示 0-隐藏")
    private Integer visible = 1;

    @Schema(description = "状态 1-启用 0-禁用")
    private Integer status = 1;

    @NotNull(message = "排序不能为空")
    @Schema(description = "排序，值越小越靠前", required = true)
    private Integer sort = 0;
}
