package com.kite.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 * 
 * @author yourname
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作模块
     */
    String module() default "";
    
    /**
     * 操作类型（如：新增、删除、修改、查询）
     */
    String operationType() default "";
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;
    
    /**
     * 是否记录响应结果
     */
    boolean recordResult() default true;
}

