package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MenuRequest {
    
    @Schema(description = "父级ID，根节点为0")
    private Long parentId = 0L;
    
    @Schema(description = "菜单名称")
    @NotBlank(message = "菜单名称不能为空")
    private String name;
    
    @Schema(description = "类型 CATALOG/MENU/BUTTON")
    private String type = "MENU";
    
    @Schema(description = "路由地址")
    private String path;
    
    @Schema(description = "前端组件")
    private String component;
    
    @Schema(description = "权限标识")
    private String permission;
    
    @Schema(description = "图标")
    private String icon;
    
    @Schema(description = "排序")
    private Integer sort = 0;
    
    @Schema(description = "是否可见")
    private Boolean visible = true;
    
    @Schema(description = "状态 1-启用 0-禁用")
    private Integer status = 1;
    
    @Schema(description = "备注")
    private String remark;
}
