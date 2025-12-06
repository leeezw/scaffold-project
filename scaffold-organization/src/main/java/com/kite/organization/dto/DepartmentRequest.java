package com.kite.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 部门请求。
 */
@Data
public class DepartmentRequest {

    @Schema(description = "租户ID")
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @Schema(description = "上级部门ID")
    private Long parentId;

    @Schema(description = "部门名称")
    @NotBlank(message = "部门名称不能为空")
    private String name;

    @Schema(description = "部门编码")
    @NotBlank(message = "部门编码不能为空")
    private String code;

    @Schema(description = "负责人用户ID")
    private Long leaderUserId;

    @Schema(description = "负责人名称")
    private String leaderName;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 500)
    private String remark;
}
