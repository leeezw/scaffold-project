package com.kite.authenticator.service;

import java.util.Set;

/**
 * Session 管理服务接口
 * 提供 Session 管理相关功能
 * 
 * @author yourname
 */
public interface SessionManagementService {
    
    /**
     * 强制用户下线（踢出所有设备）
     * 
     * @param userId 用户ID
     */
    void kickOutUser(Long userId);
    
    /**
     * 踢出指定设备
     * 
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    void kickOutDevice(Long userId, String deviceId);
    
    /**
     * 获取用户的所有 Session Key
     * 
     * @param userId 用户ID
     * @return Session Key 集合
     */
    Set<String> getUserSessionKeys(Long userId);
    
    /**
     * 禁用用户（禁用所有 Session）
     * 
     * @param userId 用户ID
     */
    void disableUser(Long userId);
    
    /**
     * 删除指定 Session（正常退出）
     * 
     * @param sessionKey Session Key
     */
    void deleteSession(String sessionKey);
}

