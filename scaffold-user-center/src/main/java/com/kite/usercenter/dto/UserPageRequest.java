package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询请求
 */
@Data
public class UserPageRequest {
    
    @Schema(description = "用户名关键字")
    private String keyword;
    
    @Schema(description = "状态 1-启用 0-禁用")
    private Integer status;
    
    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", defaultValue = "10")
    private Integer pageSize = 10;
}
