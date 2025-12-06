package com.kite.organization.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 在每个请求范围内维护租户上下文。
 */
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantContextFilter.class);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final TenantProperties tenantProperties;

    public TenantContextFilter(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!tenantProperties.isEnabled()) {
            return true;
        }
        List<String> ignoreUris = tenantProperties.getIgnoreUris();
        if (ignoreUris == null || ignoreUris.isEmpty()) {
            return false;
        }
        String uri = request.getRequestURI();
        return ignoreUris.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Long tenantId = resolveTenantId(request);
            if (tenantId == null && tenantProperties.isRequired()) {
                respondTenantMissing(response);
                return;
            }
            if (tenantId != null) {
                TenantContextHolder.setTenantId(tenantId);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private Long resolveTenantId(HttpServletRequest request) {
        String headerValue = request.getHeader(tenantProperties.getHeader());
        String tenantValue = StringUtils.hasText(headerValue) ? headerValue : request.getParameter(tenantProperties.getParam());
        if (!StringUtils.hasText(tenantValue)) {
            return null;
        }
        try {
            return Long.parseLong(tenantValue.trim());
        } catch (NumberFormatException ex) {
            LOGGER.warn("非法的租户ID: {}", tenantValue);
            return null;
        }
    }

    private void respondTenantMissing(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write("{\"code\":400,\"message\":\"Tenant header missing\"}".getBytes(StandardCharsets.UTF_8));
    }
}
