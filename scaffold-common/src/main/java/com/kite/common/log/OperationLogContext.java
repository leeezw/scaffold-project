package com.kite.common.log;

/**
 * 操作日志上下文
 * 用于在请求处理过程中传递用户信息
 * 
 * @author yourname
 */
public class OperationLogContext {
    
    private static final ThreadLocal<OperationLogContext> CONTEXT_HOLDER = new ThreadLocal<>();
    
    private Long userId;
    private String username;
    
    private OperationLogContext() {
    }
    
    public static OperationLogContext getContext() {
        return CONTEXT_HOLDER.get();
    }
    
    public static void setContext(OperationLogContext context) {
        CONTEXT_HOLDER.set(context);
    }
    
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
    
    public static OperationLogContext create(Long userId, String username) {
        OperationLogContext context = new OperationLogContext();
        context.setUserId(userId);
        context.setUsername(username);
        return context;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}

