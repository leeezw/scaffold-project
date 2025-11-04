package com.kite.authenticator;

import com.kite.authenticator.context.LoginUser;

/**
 * 签名接口
 * 负责 Token 的生成和验证
 * 
 * @author yourname
 */
public interface Signature {
    
    /**
     * 生成签名（Token）
     * 
     * @param loginUser 登录用户信息
     * @param key 密钥（可选，用于用户自定义密钥）
     * @return Token 字符串
     */
    String sign(LoginUser loginUser, String key);
    
    /**
     * 验证签名并解析用户信息
     * 
     * @param token Token 字符串
     * @param key 密钥（可选）
     * @return 登录用户信息
     */
    LoginUser verify(String token, String key);
}

