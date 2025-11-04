package com.kite.authenticator.notifier.impl;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.notifier.AbstractNotifier;
import com.kite.authenticator.notifier.AuthcEventType;
import com.kite.authenticator.notifier.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 登录通知器（示例实现）
 * 用户可以继承 AbstractNotifier 实现自己的通知器
 * 
 * @author yourname
 */
@Slf4j
@Component
public class LoginNotifier extends AbstractNotifier<LoginUser> {
    
    @Override
    public String getType() {
        return AuthcEventType.LOGIN.name();
    }
    
    @Override
    public void notify(Event<LoginUser> event) {
        LoginUser loginUser = event.getBody();
        if (loginUser != null) {
            log.info("用户登录成功: userId={}, username={}", 
                loginUser.getUserId(), loginUser.getUsername());
            // 可以在这里添加登录后的业务逻辑，如：
            // - 记录登录日志
            // - 发送登录通知
            // - 更新最后登录时间
            // - 登录奖励等
        }
    }
}

