package com.kite.usercenter.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 
 * @author yourname
 */
@Data
public class OperationLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 操作模块
     */
    private String module;
    
    /**
     * 操作类型（如：新增、删除、修改、查询）
     */
    private String operationType;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 请求方法（GET、POST、PUT、DELETE等）
     */
    private String method;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * 请求参数
     */
    private String requestParams;
    
    /**
     * 响应结果
     */
    private String responseResult;
    
    /**
     * 操作状态（0-失败，1-成功）
     */
    private Integer status;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理（浏览器信息）
     */
    private String userAgent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

