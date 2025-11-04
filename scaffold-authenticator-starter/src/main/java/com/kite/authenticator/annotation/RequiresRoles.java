package com.kite.authenticator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要角色注解
 * 标注在 Controller 方法上，表示需要指定角色才能访问
 * 
 * @author yourname
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRoles {
    
    /**
     * 需要的角色列表
     */
    String[] value();
    
    /**
     * 是否需要所有角色（true：需要所有角色，false：只需其中一个角色）
     */
    boolean logical() default false;
}

