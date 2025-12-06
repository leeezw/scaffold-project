package com.kite.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门展示 DTO。
 */
@Data
public class DepartmentDTO {

    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "上级ID")
    private Long parentId;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "部门编码")
    private String code;

    @Schema(description = "负责人ID")
    private Long leaderUserId;

    @Schema(description = "负责人姓名")
    private String leaderName;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
