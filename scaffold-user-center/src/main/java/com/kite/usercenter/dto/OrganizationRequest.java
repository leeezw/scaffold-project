package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrganizationRequest {
    
    @Schema(description = "租户ID")
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;
    
    @Schema(description = "父级ID，根节点为0")
    private Long parentId = 0L;
    
    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    private String name;
    
    @Schema(description = "类型 ORG/DEPT")
    private String type = "DEPT";
    
    @Schema(description = "排序")
    private Integer sort = 0;
    
    @Schema(description = "负责人ID")
    private Long leaderId;
    
    @Schema(description = "状态 1-启用 0-禁用")
    private Integer status = 1;
    
    @Schema(description = "备注")
    private String remark;
}
