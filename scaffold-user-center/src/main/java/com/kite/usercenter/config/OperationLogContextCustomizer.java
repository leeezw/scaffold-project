package com.kite.usercenter.config;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.context.LoginUserContextCustomizer;
import com.kite.common.log.OperationLogContext;
import org.springframework.stereotype.Component;

/**
 * 将登录用户信息同步到操作日志上下文，方便切面记录。
 */
@Component
public class OperationLogContextCustomizer implements LoginUserContextCustomizer {
    
    @Override
    public void onLoginUserSet(LoginUser loginUser) {
        if (loginUser == null) {
            OperationLogContext.clear();
            return;
        }
        OperationLogContext.setContext(OperationLogContext.create(
                loginUser.getUserId(),
                loginUser.getUsername()));
    }
    
    @Override
    public void onLoginUserCleared() {
        OperationLogContext.clear();
    }
}
