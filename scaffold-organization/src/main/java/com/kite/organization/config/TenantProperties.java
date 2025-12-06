package com.kite.organization.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 多租户配置。
 */
@Data
@ConfigurationProperties(prefix = "kite.tenant")
public class TenantProperties {

    /** 是否启用租户拦截 */
    private boolean enabled = true;

    /** 请求头中的租户ID */
    private String header = "X-Tenant-Id";

    /** 请求参数中的租户ID */
    private String param = "tenantId";

    /** 是否强制要求租户ID */
    private boolean required = false;

    /** 不参与租户校验的URI前缀 */
    private List<String> ignoreUris = new ArrayList<>();
}
