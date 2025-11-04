package com.kite.authenticator.notifier;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知器注册表
 * 单例模式，用于注册和获取通知器
 * 
 * @author yourname
 */
public class NotifyRegistry {
    
    private static final NotifyRegistry INSTANCE = new NotifyRegistry();
    
    private final Map<String, Notifier> notifiers = new HashMap<>();
    
    private NotifyRegistry() {
    }
    
    public static NotifyRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册通知器
     */
    public void register(String type, Notifier notifier) {
        notifiers.put(type, notifier);
    }
    
    /**
     * 获取通知器
     */
    public Notifier get(String type) {
        return notifiers.get(type);
    }
    
    /**
     * 移除通知器
     */
    public void remove(String type) {
        notifiers.remove(type);
    }
    
    /**
     * 清除所有通知器
     */
    public void clear() {
        notifiers.clear();
    }
}

