package com.kite.authenticator.notifier;

/**
 * 认证事件类型枚举
 * 
 * @author yourname
 */
public enum AuthcEventType implements EventType {
    
    /**
     * 登录事件
     */
    LOGIN(1),
    
    /**
     * 登出事件
     */
    LOGOUT(2),
    
    /**
     * 认证事件
     */
    AUTHENTICATE(3);
    
    private final Integer code;
    
    AuthcEventType(Integer code) {
        this.code = code;
    }
    
    @Override
    public Integer getCode() {
        return code;
    }
    
    public static AuthcEventType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AuthcEventType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}

