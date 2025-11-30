package com.kite.usercenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Session 列表查询请求
 */
@Data
public class SessionListRequest {
    
    @Schema(description = "用户ID（可选）")
    private Long userId;
    
    @Schema(description = "关键字（匹配用户名、昵称、设备、Session Key）")
    private String keyword;
    
    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页数量", defaultValue = "10")
    private Integer pageSize = 10;
}
