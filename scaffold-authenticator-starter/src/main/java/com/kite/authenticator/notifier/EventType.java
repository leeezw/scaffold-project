package com.kite.authenticator.notifier;

import java.io.Serializable;

/**
 * 事件类型接口
 * 
 * @author yourname
 */
public interface EventType extends Serializable {
    
    /**
     * 获取事件类型代码
     */
    Integer getCode();
}

