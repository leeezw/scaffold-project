package com.kite.authenticator.context;

/**
 * 自定义登录用户上下文处理器，可用于同步第三方上下文信息。
 */
public interface LoginUserContextCustomizer {

    /**
     * 登录用户设置到 {@link LoginUserContext} 后回调。
     */
    void onLoginUserSet(LoginUser loginUser);

    /**
     * 登录用户上下文清理后回调。
     */
    void onLoginUserCleared();
}
