package com.kite.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 租户下拉展示。
 */
@Data
public class TenantOptionVO {

    @Schema(description = "租户ID")
    private Long id;

    @Schema(description = "租户编码")
    private String code;

    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "过期时间")
    private LocalDate expiredAt;
}
