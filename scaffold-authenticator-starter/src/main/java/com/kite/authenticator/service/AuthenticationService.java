package com.kite.authenticator.service;

import com.kite.authenticator.context.LoginUser;

/**
 * 认证服务接口
 * 用户需要实现此接口来提供用户认证逻辑
 * 
 * @author yourname
 */
public interface AuthenticationService {
    
    /**
     * 根据用户名和密码获取用户信息
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录用户信息
     */
    LoginUser authenticate(String username, String password);
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 登录用户信息
     */
    LoginUser getUserById(Long userId);
}

