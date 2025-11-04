package com.kite.authenticator.notifier;

/**
 * 通知器接口
 * 
 * @author yourname
 */
public interface Notifier<T> {
    
    /**
     * 获取通知器类型（事件类型名称）
     */
    String getType();
    
    /**
     * 通知事件
     */
    void notify(Event<T> event);
}

