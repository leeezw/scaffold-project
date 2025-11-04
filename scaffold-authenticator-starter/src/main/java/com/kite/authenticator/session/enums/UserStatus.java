package com.kite.authenticator.session.enums;

/**
 * 用户状态枚举
 * 
 * @author yourname
 */
public enum UserStatus {
    
    /**
     * 正常状态
     */
    NORMAL(1),
    
    /**
     * 禁用状态
     */
    DISABLED(0),
    
    /**
     * 踢出状态（所有设备）
     */
    KICK_OUT(-1),
    
    /**
     * 设备踢出状态（特定设备）
     */
    DEVICE_KICK_OUT(-2),
    
    /**
     * 被顶替状态（其他地方登录）
     */
    REPLACED(-3);
    
    private final Integer code;
    
    UserStatus(Integer code) {
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public static UserStatus fromCode(Integer code) {
        if (code == null) {
            return NORMAL;
        }
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return NORMAL;
    }
}

