package com.kite.usercenter.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单/按钮实体
 */
@Data
public class Menu implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long parentId;
    private String name;
    /**
     * CATALOG/MENU/BUTTON
     */
    private String type;
    private String path;
    private String component;
    private String icon;
    private String permission;
    private Integer sort;
    private Boolean visible;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
