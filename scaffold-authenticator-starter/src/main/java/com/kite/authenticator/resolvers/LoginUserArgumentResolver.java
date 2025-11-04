package com.kite.authenticator.resolvers;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.context.LoginUserContext;
import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * LoginUser 参数解析器
 * 自动将 LoginUser 注入到 Controller 方法参数中
 * 
 * @author yourname
 */
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return LoginUser.class.equals(parameter.getParameterType());
    }
    
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        LoginUser loginUser = LoginUserContext.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户未登录");
        }
        return loginUser;
    }
}

