package com.kite.authenticator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 允许匿名访问注解
 * 标注在 Controller 方法上，表示该接口不需要认证即可访问
 * 
 * @author yourname
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowAnonymous {
}

