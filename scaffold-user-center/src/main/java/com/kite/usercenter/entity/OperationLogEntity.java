package com.kite.usercenter.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类（用于Service层）
 * 
 * @author yourname
 */
@Data
public class OperationLogEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String operationType;
    private String description;
    private String method;
    private String requestUrl;
    private String requestParams;
    private String responseResult;
    private Integer status;
    private String errorMsg;
    private Long executionTime;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createTime;
}

