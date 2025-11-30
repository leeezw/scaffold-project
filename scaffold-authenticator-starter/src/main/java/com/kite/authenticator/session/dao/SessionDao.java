package com.kite.authenticator.session.dao;

import com.kite.authenticator.session.Session;

/**
 * Session 数据访问接口
 * 
 * @author yourname
 */
public interface SessionDao {
    
    /**
     * 创建 Session
     */
    void create(Session session);
    
    /**
     * 更新 Session
     */
    void update(Session session);
    
    /**
     * 删除 Session
     */
    void delete(Session session);
    
    /**
     * 根据 SessionKey 获取 Session
     */
    Session get(String sessionKey);
    
    /**
     * 根据用户ID获取所有 Session Key
     */
    java.util.Set<String> getUserSessionKeys(Long userId);
    
    /**
     * 删除用户的所有 Session
     */
    void deleteUserSessions(Long userId);
    
    /**
     * 获取所有 Session 列表
     */
    java.util.List<Session> listAllSessions();
}
