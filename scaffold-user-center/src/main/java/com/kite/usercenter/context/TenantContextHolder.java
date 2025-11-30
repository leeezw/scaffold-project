package com.kite.usercenter.context;

/**
 * 租户上下文
 */
public final class TenantContextHolder {
    
    private static final ThreadLocal<Long> TENANT_HOLDER = new ThreadLocal<>();
    
    private TenantContextHolder() {
    }
    
    public static void setTenantId(Long tenantId) {
        if (tenantId == null) {
            TENANT_HOLDER.remove();
        } else {
            TENANT_HOLDER.set(tenantId);
        }
    }
    
    public static Long getTenantId() {
        return TENANT_HOLDER.get();
    }
    
    public static void clear() {
        TENANT_HOLDER.remove();
    }
}
