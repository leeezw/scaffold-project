package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TenantRequest {
    
    @Schema(description = "租户编码")
    @NotBlank(message = "租户编码不能为空")
    private String code;
    
    @Schema(description = "租户名称")
    @NotBlank(message = "租户名称不能为空")
    private String name;
    
    @Schema(description = "状态 1-启用 0-禁用")
    private Integer status = 1;
    
    @Schema(description = "备注")
    private String remark;
}
