package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询请求
 */
@Data
public class UserPageRequest {
    
    @Schema(description = "关键字（支持用户名、昵称、邮箱、手机号）")
    private String keyword;
    
    @Schema(description = "状态 1-启用 0-禁用，null-全部")
    private Integer status;
    
    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", defaultValue = "10")
    private Integer pageSize = 10;
    
    @Schema(description = "排序字段（如：createTime, username, status等）", defaultValue = "createTime")
    private String sortField = "createTime";
    
    @Schema(description = "排序方向（asc-升序, desc-降序）", defaultValue = "desc")
    private String sortOrder = "desc";
}
