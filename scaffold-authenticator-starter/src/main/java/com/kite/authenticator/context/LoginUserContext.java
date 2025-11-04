package com.kite.authenticator.context;

/**
 * 登录用户上下文
 * 使用 ThreadLocal 存储当前登录用户信息
 * 
 * @author yourname
 */
public class LoginUserContext {
    
    private static final ThreadLocal<LoginUser> CONTEXT_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置当前登录用户
     */
    public static void setLoginUser(LoginUser loginUser) {
        CONTEXT_HOLDER.set(loginUser);
    }
    
    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {
        return CONTEXT_HOLDER.get();
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }
    
    /**
     * 清除上下文
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
    
    /**
     * 判断是否已登录
     */
    public static boolean isLogin() {
        return getLoginUser() != null;
    }
}

