package com.kite.authenticator.notifier;

import java.io.Serializable;

/**
 * 事件接口
 * 
 * @author yourname
 */
public interface Event<T> extends Serializable {
    
    /**
     * 获取事件类型
     */
    EventType getEventType();
    
    /**
     * 获取事件体
     */
    T getBody();
}

