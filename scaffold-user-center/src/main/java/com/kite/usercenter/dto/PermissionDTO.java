package com.kite.usercenter.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PermissionDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String code;
    private String name;
    private String type;
    private Long parentId;
    private String path;
    private String method;
    private String icon;
    private String component;
    private Integer visible;
    private Integer status;
    private Integer sort;
    private List<PermissionDTO> children;
}
