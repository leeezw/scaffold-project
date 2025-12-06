package com.kite.organization.config;

/**
 * 简单的租户上下文，提供线程变量保存当前请求的租户信息。
 */
public final class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_HOLDER = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void setTenantId(Long tenantId) {
        TENANT_HOLDER.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_HOLDER.get();
    }

    public static void clear() {
        TENANT_HOLDER.remove();
    }
}
