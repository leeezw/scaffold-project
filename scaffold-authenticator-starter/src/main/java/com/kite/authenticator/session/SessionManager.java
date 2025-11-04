package com.kite.authenticator.session;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.session.dao.SessionDao;
import com.kite.authenticator.session.enums.UserStatus;

/**
 * Session 管理器
 * 
 * @author yourname
 */
public class SessionManager {
    
    private final SessionDao sessionDao;
    private final SessionParser sessionParser;
    
    public SessionManager(SessionDao sessionDao, SessionParser sessionParser) {
        this.sessionDao = sessionDao;
        this.sessionParser = sessionParser;
    }
    
    /**
     * 创建 Session
     */
    public Session createSession(LoginUser loginUser, String deviceId, Long expireTime) {
        DefaultSession session = new DefaultSession();
        
        String sessionKey = sessionParser.generateSessionKey(loginUser, deviceId);
        session.setSessionKey(sessionKey);
        session.setUserId(loginUser.getUserId());
        session.setDeviceId(deviceId != null ? deviceId : "default");
        session.setStartTime(System.currentTimeMillis());
        session.setLastAccessTime(System.currentTimeMillis());
        session.setExpireAt(System.currentTimeMillis() + expireTime);
        session.setStatus(UserStatus.NORMAL.getCode());
        
        sessionDao.create(session);
        
        return session;
    }
    
    /**
     * 获取 Session
     */
    public Session getSession(String sessionKey) {
        return sessionDao.get(sessionKey);
    }
    
    /**
     * 更新 Session
     */
    public void updateSession(Session session) {
        sessionDao.update(session);
    }
    
    /**
     * 删除 Session
     */
    public void deleteSession(Session session) {
        sessionDao.delete(session);
    }
    
    /**
     * 获取用户的所有 Session
     */
    public java.util.Set<String> getUserSessionKeys(Long userId) {
        return sessionDao.getUserSessionKeys(userId);
    }
    
    /**
     * 删除用户的所有 Session（强制下线）
     */
    public void kickOutUser(Long userId) {
        java.util.Set<String> sessionKeys = getUserSessionKeys(userId);
        for (String sessionKey : sessionKeys) {
            Session session = getSession(sessionKey);
            if (session != null) {
                session.modifyStatus(UserStatus.KICK_OUT);
                sessionDao.update(session);
            }
        }
    }
    
    /**
     * 删除指定设备的 Session
     */
    public void kickOutDevice(Long userId, String deviceId) {
        java.util.Set<String> sessionKeys = getUserSessionKeys(userId);
        for (String sessionKey : sessionKeys) {
            Session session = getSession(sessionKey);
            if (session != null && session.matchDevice(deviceId)) {
                session.modifyStatus(UserStatus.DEVICE_KICK_OUT);
                sessionDao.update(session);
            }
        }
    }
}

