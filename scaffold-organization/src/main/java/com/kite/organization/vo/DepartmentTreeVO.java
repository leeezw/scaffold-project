package com.kite.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门树节点。
 */
@Data
public class DepartmentTreeVO {

    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "部门编码")
    private String code;

    @Schema(description = "负责人")
    private String leaderName;

    @Schema(description = "子节点")
    private List<DepartmentTreeVO> children = new ArrayList<>();
}
