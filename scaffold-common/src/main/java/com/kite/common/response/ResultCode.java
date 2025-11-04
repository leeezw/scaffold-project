package com.kite.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结果码枚举
 * 
 * @author yourname
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 失败
     */
    FAIL(500, "操作失败"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),
    
    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限访问"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 方法不允许
     */
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    /**
     * 服务器错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    /**
     * 业务异常
     */
    BUSINESS_ERROR(600, "业务处理失败"),
    
    /**
     * 数据已存在
     */
    DATA_EXISTS(601, "数据已存在"),
    
    /**
     * 数据不存在
     */
    DATA_NOT_EXISTS(602, "数据不存在"),
    
    /**
     * 操作过于频繁
     */
    TOO_MANY_REQUESTS(429, "操作过于频繁，请稍后再试");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 提示信息
     */
    private final String message;
}

