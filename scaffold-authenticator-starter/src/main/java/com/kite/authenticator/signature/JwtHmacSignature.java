package com.kite.authenticator.signature;

import com.kite.authenticator.Signature;
import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.util.JwtUtils;
import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * JWT HMAC 签名实现
 * 使用 HMAC-SHA256 算法
 * 
 * @author yourname
 */
@Slf4j
public class JwtHmacSignature implements Signature {
    
    @Value("${kite.auth.secret}")
    private String defaultSecret;
    
    @Value("${kite.auth.expire-time:604800000}")
    private Long expireTime;
    
    public JwtHmacSignature() {
    }
    
    public JwtHmacSignature(String defaultSecret) {
        this.defaultSecret = defaultSecret;
    }
    
    @Override
    public String sign(LoginUser loginUser, String key) {
        if (loginUser == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "登录用户信息不能为空");
        }
        
        String secret = StringUtils.isEmpty(key) ? defaultSecret : key;
        if (StringUtils.isEmpty(secret)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "JWT 密钥不能为空");
        }
        
        // 设置过期时间
        if (loginUser.getExpireAt() == null) {
            loginUser.setExpireAt(System.currentTimeMillis() + expireTime);
        }
        
        return JwtUtils.generateToken(loginUser, secret, expireTime, null);
    }
    
    @Override
    public LoginUser verify(String token, String key) {
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Token 不能为空");
        }
        
        String secret = StringUtils.isEmpty(key) ? defaultSecret : key;
        if (StringUtils.isEmpty(secret)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "JWT 密钥不能为空");
        }
        
        return JwtUtils.parseToken(token, secret);
    }
}

