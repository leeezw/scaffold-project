package com.kite.usercenter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单视图对象
 */
@Data
public class MenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单编码")
    private String code;

    @Schema(description = "类型 menu/button/api")
    private String type;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "前端组件标识")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "是否显示 1-显示 0-隐藏")
    private Integer visible;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "子菜单")
    private List<MenuVO> children = new ArrayList<>();
}
