package com.kite.authenticator;

import com.kite.authenticator.context.LoginUser;
import com.kite.authenticator.notifier.AuthcEventType;
import com.kite.authenticator.notifier.Event;
import com.kite.authenticator.notifier.Notifier;
import com.kite.authenticator.notifier.NotifyRegistry;
import com.kite.authenticator.notifier.impl.LoginEvent;
import com.kite.authenticator.session.Session;
import com.kite.authenticator.session.dao.SessionDao;
import com.kite.authenticator.session.SessionManager;
import com.kite.authenticator.session.SessionParser;
import com.kite.authenticator.session.enums.UserStatus;
import com.kite.authenticator.token.HostAuthenticationToken;
import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 默认安全管理器
 * 实现认证逻辑的核心类
 * 
 * @author yourname
 */
@Slf4j
public class DefaultSecurityManager implements Authenticator {
    
    private final Signature signature;
    private final Realm realm;
    private final SessionManager sessionManager;
    private final SessionDao sessionDao;
    private final AuthenticatorConfigReader config;
    private final SessionParser sessionParser;
    
    public DefaultSecurityManager(
            Realm realm,
            Signature signature,
            SessionManager sessionManager,
            SessionDao sessionDao,
            SessionParser sessionParser,
            AuthenticatorConfigReader config) {
        this.realm = realm;
        this.signature = signature;
        this.sessionManager = sessionManager;
        this.sessionDao = sessionDao;
        this.sessionParser = sessionParser;
        this.config = config;
    }
    
    @Override
    public LoginUser authenticate(HostAuthenticationToken token) {
        Boolean validateDevice = config.getValidateHost();
        Boolean isRenewal = config.getRenewal();
        Boolean validateStatus = config.getValidateStatus();
        
        // 设置默认值
        if (validateDevice == null) {
            validateDevice = true;
        }
        if (isRenewal == null) {
            isRenewal = true;
        }
        if (validateStatus == null) {
            validateStatus = true;
        }
        
        // 1. 从 Realm 获取认证信息
        AuthenticationInfo authenticationInfo = realm.getAuthenticationInfo(token);
        if (authenticationInfo == null || authenticationInfo.getUser() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "认证失败");
        }
        
        // 2. 验证 Token（通过 Signature）
        LoginUser loginUser = signature.verify(token.getCredential(), getSecret());
        
        // 3. Session 验证（如果启用）
        if (sessionDao != null && sessionManager != null) {
            String secret = getSecret();
            if (StringUtils.isEmpty(secret)) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "JWT 密钥不能为空");
            }
            String sessionKey = com.kite.authenticator.util.JwtUtils.extractSessionKey(
                token.getCredential(), secret);
            
            if (sessionKey != null && !sessionKey.isEmpty()) {
                Session session = sessionManager.getSession(sessionKey);
                if (session == null) {
                    throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Session 不存在");
                }
                
                // 检查 Session 是否过期
                if (session.isExpired()) {
                    throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Session 已过期");
                }
                
                // 检查设备是否匹配
                if (validateDevice && !session.matchDevice(token.getHost())) {
                    throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "设备不匹配");
                }
                
                // 检查用户状态
                if (validateStatus) {
                    UserStatus status = UserStatus.fromCode(session.getStatus());
                    if (status == UserStatus.KICK_OUT) {
                        throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户已被踢出");
                    }
                    if (status == UserStatus.DISABLED) {
                        throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户已被禁用");
                    }
                    if (status == UserStatus.DEVICE_KICK_OUT) {
                        throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "设备已被踢出");
                    }
                    if (status == UserStatus.REPLACED) {
                        throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户已在其他地方登录");
                    }
                }
                
                // 检查会话超时
                Long sessionTimeout = config.getSessionTimeout();
                if (sessionTimeout != null && session.exceedSessionTimeout(sessionTimeout)) {
                    throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "会话已超时，请重新登录");
                }
                
                // 更新最后访问时间
                session.touch();
                
                // Session 续期
                if (isRenewal) {
                    Long renewalInterval = config.getRenewalInterval();
                    if (renewalInterval != null) {
                        session.renewal(renewalInterval);
                    }
                }
                
                // 更新 Session
                sessionDao.update(session);
            }
        }
        
        return loginUser;
    }
    
    @Override
    public String login(AuthenticationInfo authenticationInfo) {
        LoginUser loginUser = authenticationInfo.getUser();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "登录用户信息不能为空");
        }
        
        String secret = getSecret();
        if (StringUtils.isEmpty(secret)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "JWT 密钥不能为空");
        }
        
        Long expireTime = getExpireTime();
        
        // 创建 Session（如果启用）
        String sessionKey = null;
        if (sessionManager != null && sessionDao != null) {
            // 从 authenticationInfo 中获取 deviceId（存储在 credential 中）
            String deviceId = authenticationInfo.getCredential();
            Session session = sessionManager.createSession(
                loginUser, 
                deviceId,
                expireTime);
            sessionKey = session.getSessionKey();
        }
        
        // 生成 Token（包含 sessionKey）
        String token = signature.sign(loginUser, getSecret());
        
        // 如果有 sessionKey，需要更新 Token
        if (sessionKey != null && !sessionKey.isEmpty()) {
            token = com.kite.authenticator.util.JwtUtils.generateToken(
                loginUser, secret, expireTime, sessionKey);
        }
        
        // 触发登录事件
        triggerLoginEvent(loginUser);
        
        return token;
    }
    
    /**
     * 触发登录事件
     */
    private void triggerLoginEvent(LoginUser loginUser) {
        try {
            Notifier<LoginUser> notifier = NotifyRegistry.getInstance().get(AuthcEventType.LOGIN.name());
            if (notifier != null) {
                notifier.notify(new LoginEvent(loginUser));
            }
        } catch (Exception e) {
            log.warn("触发登录事件失败", e);
        }
    }
    
    /**
     * 获取密钥（从配置中获取）
     */
    private String getSecret() {
        if (config instanceof com.kite.authenticator.config.AuthenticatorProperties) {
            return ((com.kite.authenticator.config.AuthenticatorProperties) config).getSecret();
        }
        return null;
    }
    
    /**
     * 获取过期时间（从配置中获取）
     */
    private Long getExpireTime() {
        if (config instanceof com.kite.authenticator.config.AuthenticatorProperties) {
            return ((com.kite.authenticator.config.AuthenticatorProperties) config).getExpireTime();
        }
        return 7 * 24 * 60 * 60 * 1000L;  // 默认 7 天
    }
}

