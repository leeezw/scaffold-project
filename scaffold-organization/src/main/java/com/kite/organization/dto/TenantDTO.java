package com.kite.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户展示 DTO。
 */
@Data
public class TenantDTO {

    @Schema(description = "租户ID")
    private Long id;

    @Schema(description = "租户编码")
    private String code;

    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "行业")
    private String industry;

    @Schema(description = "套餐等级")
    private String level;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "过期时间")
    private LocalDate expiredAt;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "邮箱")
    private String contactEmail;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
