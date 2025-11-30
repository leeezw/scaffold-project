package com.kite.usercenter.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrganizationDTO {
    
    private Long id;
    private Long tenantId;
    private Long parentId;
    private String name;
    private String type;
    private Integer sort;
    private String path;
    private Long leaderId;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<OrganizationDTO> children = new ArrayList<>();
}
