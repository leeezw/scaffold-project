package com.kite.organization.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 组织模块自动配置。
 */
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
public class OrganizationAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "kite.tenant", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TenantContextFilter tenantContextFilter(TenantProperties tenantProperties) {
        return new TenantContextFilter(tenantProperties);
    }

    @Bean
    @ConditionalOnBean(TenantContextFilter.class)
    public FilterRegistrationBean<TenantContextFilter> tenantContextFilterRegistration(TenantContextFilter filter,
                                                                                       TenantProperties tenantProperties) {
        FilterRegistrationBean<TenantContextFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.setName("tenantContextFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        registrationBean.setEnabled(tenantProperties.isEnabled());
        return registrationBean;
    }
}
