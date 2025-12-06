package com.kite.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 岗位下拉展示。
 */
@Data
public class PositionOptionVO {

    @Schema(description = "岗位ID")
    private Long id;

    @Schema(description = "岗位名称")
    private String name;

    @Schema(description = "岗位编码")
    private String code;
}
