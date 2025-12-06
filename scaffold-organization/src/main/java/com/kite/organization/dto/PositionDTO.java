package com.kite.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位展示 DTO。
 */
@Data
public class PositionDTO {

    @Schema(description = "岗位ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "类别")
    private String category;

    @Schema(description = "职级")
    private Integer rank;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
