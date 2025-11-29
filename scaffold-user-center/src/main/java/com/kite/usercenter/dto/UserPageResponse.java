package com.kite.usercenter.dto;

import com.kite.common.util.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户分页查询响应（包含统计信息）
 */
@Data
@Schema(description = "用户分页查询响应")
public class UserPageResponse {
    
    @Schema(description = "分页数据")
    private PageResult<UserDTO> pageData;
    
    @Schema(description = "用户总数")
    private Long total;
    
    @Schema(description = "启用用户数")
    private Long enabledCount;
    
    @Schema(description = "禁用用户数")
    private Long disabledCount;
    
    @Schema(description = "今日新增用户数")
    private Long todayNewCount;
}

