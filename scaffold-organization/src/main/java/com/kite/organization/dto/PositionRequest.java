package com.kite.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 岗位请求。
 */
@Data
public class PositionRequest {

    @Schema(description = "租户ID")
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @Schema(description = "岗位名称")
    @NotBlank(message = "岗位名称不能为空")
    private String name;

    @Schema(description = "岗位编码")
    @NotBlank(message = "岗位编码不能为空")
    private String code;

    @Schema(description = "岗位类别")
    private String category;

    @Schema(description = "岗位等级/职级")
    private Integer rank;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 500)
    private String remark;
}
