package com.kite.authenticator.config;

import com.kite.authenticator.annotation.AllowAnonymous;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 收集标记了 {@link AllowAnonymous} 的接口，供认证过滤器在运行时放行。
 */
public class AllowAnonymousRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private final ObjectProvider<List<RequestMappingHandlerMapping>> handlerMappingsProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<AnonymousDefinition> definitions = new CopyOnWriteArrayList<>();

    public AllowAnonymousRegistry(ObjectProvider<List<RequestMappingHandlerMapping>> handlerMappingsProvider) {
        this.handlerMappingsProvider = handlerMappingsProvider;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        reload();
    }

    /**
     * 判断当前请求是否命中 AllowAnonymous。
     */
    public boolean isAllowAnonymous(String path, String method) {
        if (definitions.isEmpty()) {
            return false;
        }
        HttpMethod httpMethod = null;
        if (method != null) {
            try {
                httpMethod = HttpMethod.valueOf(method);
            } catch (IllegalArgumentException ignored) {
                // ignore unknown http method
            }
        }
        for (AnonymousDefinition definition : definitions) {
            if (!definition.matches(pathMatcher, path, httpMethod)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private void reload() {
        definitions.clear();
        List<RequestMappingHandlerMapping> handlerMappings =
                handlerMappingsProvider.getIfAvailable(Collections::emptyList);
        if (handlerMappings == null || handlerMappings.isEmpty()) {
            return;
        }
        for (RequestMappingHandlerMapping handlerMapping : handlerMappings) {
            Map<RequestMappingInfo, HandlerMethod> mappingInfo = handlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mappingInfo.entrySet()) {
                HandlerMethod handlerMethod = entry.getValue();
                if (!isAllowAnonymous(handlerMethod)) {
                    continue;
                }
                definitions.addAll(buildDefinitions(entry.getKey()));
            }
        }
    }

    private boolean isAllowAnonymous(HandlerMethod handlerMethod) {
        return AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), AllowAnonymous.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), AllowAnonymous.class);
    }

    private List<AnonymousDefinition> buildDefinitions(RequestMappingInfo info) {
        Set<String> patterns = extractPatterns(info);
        Set<HttpMethod> methods = extractMethods(info);
        if (patterns.isEmpty()) {
            return Collections.emptyList();
        }
        List<AnonymousDefinition> result = new ArrayList<>(patterns.size());
        for (String pattern : patterns) {
            result.add(new AnonymousDefinition(pattern, methods));
        }
        return result;
    }

    private Set<String> extractPatterns(RequestMappingInfo info) {
        Set<String> patterns = new HashSet<>();
        if (info == null) {
            return patterns;
        }
        PatternsRequestCondition condition = info.getPatternsCondition();
        if (condition != null && condition.getPatterns() != null) {
            patterns.addAll(condition.getPatterns());
        }
        // Spring 5.x 只使用 PatternsRequestCondition，这里提前返回即可
        return patterns;
    }

    private Set<HttpMethod> extractMethods(RequestMappingInfo info) {
        if (info == null || info.getMethodsCondition() == null) {
            return Collections.emptySet();
        }
        Set<HttpMethod> methods = new HashSet<>();
        info.getMethodsCondition().getMethods().forEach(requestMethod -> methods.add(HttpMethod.valueOf(requestMethod.name())));
        return methods;
    }

    private static class AnonymousDefinition {
        private final String pattern;
        private final Set<HttpMethod> methods;

        AnonymousDefinition(String pattern, Set<HttpMethod> methods) {
            this.pattern = pattern;
            this.methods = methods == null ? Collections.emptySet() : methods;
        }

        boolean matches(AntPathMatcher matcher, String path, HttpMethod method) {
            if (!matcher.match(pattern, path)) {
                return false;
            }
            if (methods.isEmpty() || method == null) {
                return true;
            }
            return methods.contains(method);
        }
    }
}
