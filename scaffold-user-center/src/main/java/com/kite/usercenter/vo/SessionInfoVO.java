package com.kite.usercenter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Session 信息视图对象
 */
@Data
public class SessionInfoVO {
    
    @Schema(description = "Session Key")
    private String sessionKey;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "设备ID")
    private String deviceId;
    
    @Schema(description = "Session 状态码")
    private Integer status;
    
    @Schema(description = "Session 状态描述")
    private String statusDesc;
    
    @Schema(description = "开始时间（毫秒级时间戳）")
    private Long startTime;
    
    @Schema(description = "最后访问时间（毫秒级时间戳）")
    private Long lastAccessTime;
    
    @Schema(description = "过期时间（毫秒级时间戳）")
    private Long expireAt;
    
    @Schema(description = "最近操作时间（毫秒级时间戳）")
    private Long operationTime;
}
