package com.kite.authenticator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要权限注解
 * 标注在 Controller 方法上，表示需要指定权限才能访问
 * 
 * @author yourname
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermissions {
    
    /**
     * 需要的权限列表
     */
    String[] value();
    
    /**
     * 是否需要所有权限（true：需要所有权限，false：只需其中一个权限）
     */
    boolean logical() default false;
}

