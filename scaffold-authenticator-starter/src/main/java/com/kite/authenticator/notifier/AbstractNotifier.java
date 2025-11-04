package com.kite.authenticator.notifier;

import org.springframework.beans.factory.InitializingBean;

/**
 * 抽象通知器基类
 * 实现类继承此类，自动注册到 NotifyRegistry
 * 
 * @author yourname
 */
public abstract class AbstractNotifier<T> implements Notifier<T>, InitializingBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        NotifyRegistry.getInstance().register(getType(), this);
    }
}

