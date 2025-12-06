package com.kite.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 租户创建/更新请求。
 */
@Data
public class TenantRequest {

    @Schema(description = "租户编码")
    @NotBlank(message = "租户编码不能为空")
    private String code;

    @Schema(description = "租户名称")
    @NotBlank(message = "租户名称不能为空")
    private String name;

    @Schema(description = "联系人")
    @NotBlank(message = "联系人不能为空")
    private String contactName;

    @Schema(description = "联系电话")
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String contactEmail;

    @Schema(description = "行业")
    private String industry;

    @Schema(description = "套餐等级")
    private String level;

    @Schema(description = "租户状态")
    private Integer status;

    @Schema(description = "过期时间")
    @FutureOrPresent(message = "过期时间不能早于今天")
    private LocalDate expiredAt;

    @Schema(description = "自定义配置JSON")
    @Size(max = 2000)
    private String configJson;

    @Schema(description = "备注")
    @Size(max = 500)
    private String remark;
}
