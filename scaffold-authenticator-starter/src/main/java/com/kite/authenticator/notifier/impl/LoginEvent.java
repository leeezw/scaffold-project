package com.kite.authenticator.notifier.impl;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.notifier.AuthcEventType;
import com.kite.authenticator.notifier.Event;
import com.kite.authenticator.notifier.EventType;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录事件
 * 
 * @author yourname
 */
@Data
public class LoginEvent implements Event<LoginUser>, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private LoginUser loginUser;
    
    public LoginEvent() {
    }
    
    public LoginEvent(LoginUser loginUser) {
        this.loginUser = loginUser;
    }
    
    @Override
    public EventType getEventType() {
        return AuthcEventType.LOGIN;
    }
    
    @Override
    public LoginUser getBody() {
        return loginUser;
    }
}

