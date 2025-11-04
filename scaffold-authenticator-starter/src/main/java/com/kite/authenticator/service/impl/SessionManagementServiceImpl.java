package com.kite.authenticator.service.impl;

import com.kite.authenticator.service.SessionManagementService;
import com.kite.authenticator.session.Session;
import com.kite.authenticator.session.SessionManager;
import com.kite.authenticator.session.enums.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Session 管理服务实现
 * 
 * @author yourname
 */
@Service
public class SessionManagementServiceImpl implements SessionManagementService {
    
    @Autowired(required = false)
    private SessionManager sessionManager;
    
    @Override
    public void kickOutUser(Long userId) {
        if (sessionManager == null) {
            return;
        }
        sessionManager.kickOutUser(userId);
    }
    
    @Override
    public void kickOutDevice(Long userId, String deviceId) {
        if (sessionManager == null) {
            return;
        }
        sessionManager.kickOutDevice(userId, deviceId);
    }
    
    @Override
    public Set<String> getUserSessionKeys(Long userId) {
        if (sessionManager == null) {
            return new java.util.HashSet<>();
        }
        return sessionManager.getUserSessionKeys(userId);
    }
    
    @Override
    public void disableUser(Long userId) {
        if (sessionManager == null) {
            return;
        }
        Set<String> sessionKeys = getUserSessionKeys(userId);
        for (String sessionKey : sessionKeys) {
            Session session = sessionManager.getSession(sessionKey);
            if (session != null) {
                session.modifyStatus(UserStatus.DISABLED);
                sessionManager.updateSession(session);
            }
        }
    }
}

