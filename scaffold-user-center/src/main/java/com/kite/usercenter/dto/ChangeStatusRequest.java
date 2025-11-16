package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 修改状态请求
 */
@Data
public class ChangeStatusRequest {
    
    @NotNull
    @Schema(description = "主键ID", required = true)
    private Long id;
    
    @NotNull
    @Schema(description = "状态 1-启用 0-禁用", required = true)
    private Integer status;
}
