package com.kite.usercenter.filter;

import com.kite.usercenter.context.TenantContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantContextFilter extends OncePerRequestFilter {
    
    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final Long DEFAULT_TENANT_ID = 1L;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String headerValue = request.getHeader(TENANT_HEADER);
            Long tenantId = parseTenantId(headerValue);
            TenantContextHolder.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
    
    private Long parseTenantId(String headerValue) {
        if (StringUtils.hasText(headerValue)) {
            try {
                return Long.parseLong(headerValue);
            } catch (NumberFormatException ignore) {
                return DEFAULT_TENANT_ID;
            }
        }
        return DEFAULT_TENANT_ID;
    }
}
