package com.kite.common.constant;

/**
 * 系统常量
 * 
 * @author yourname
 */
public class Constants {
    
    /**
     * 成功标识
     */
    public static final String SUCCESS = "SUCCESS";
    
    /**
     * 失败标识
     */
    public static final String FAIL = "FAIL";
    
    /**
     * 删除标识
     */
    public static final String DEL_FLAG = "1";
    
    /**
     * 正常标识
     */
    public static final String NORMAL = "0";
    
    /**
     * 锁定标识
     */
    public static final String LOCK = "1";
    
    /**
     * 默认页码
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;
    
    /**
     * 默认每页数量
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    
    /**
     * 最大每页数量
     */
    public static final Integer MAX_PAGE_SIZE = 100;
    
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";
    
    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";
    
    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * 令牌
     */
    public static final String TOKEN = "token";
    
    /**
     * 登录用户键
     */
    public static final String LOGIN_USER_KEY = "login_user_key";
    
    /**
     * 用户ID键
     */
    public static final String USER_ID_KEY = "user_id";
    
    /**
     * 用户名键
     */
    public static final String USERNAME_KEY = "username";
    
    /**
     * 用户权限键
     */
    public static final String USER_PERMISSIONS_KEY = "user_permissions";
    
    /**
     * 用户角色键
     */
    public static final String USER_ROLES_KEY = "user_roles";
    
    /**
     * 请求ID键
     */
    public static final String REQUEST_ID_KEY = "request_id";
    
    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";
    
    /**
     * 密码最小长度
     */
    public static final Integer PASSWORD_MIN_LENGTH = 6;
    
    /**
     * 密码最大长度
     */
    public static final Integer PASSWORD_MAX_LENGTH = 20;
    
    /**
     * 验证码键前缀
     */
    public static final String CAPTCHA_KEY_PREFIX = "captcha:";
    
    /**
     * 验证码过期时间（秒）
     */
    public static final Long CAPTCHA_EXPIRE = 300L;
    
    /**
     * 令牌过期时间（秒）
     */
    public static final Long TOKEN_EXPIRE = 3600L;
    
    /**
     * 刷新令牌过期时间（秒）
     */
    public static final Long REFRESH_TOKEN_EXPIRE = 7200L;
    
    /**
     * 操作日志：操作类型 - 新增
     */
    public static final String OPERATION_TYPE_INSERT = "新增";
    
    /**
     * 操作日志：操作类型 - 删除
     */
    public static final String OPERATION_TYPE_DELETE = "删除";
    
    /**
     * 操作日志：操作类型 - 修改
     */
    public static final String OPERATION_TYPE_UPDATE = "修改";
    
    /**
     * 操作日志：操作类型 - 查询
     */
    public static final String OPERATION_TYPE_QUERY = "查询";
    
    /**
     * 操作日志：操作类型 - 导出
     */
    public static final String OPERATION_TYPE_EXPORT = "导出";
    
    /**
     * 操作日志：操作类型 - 导入
     */
    public static final String OPERATION_TYPE_IMPORT = "导入";
    
    /**
     * 操作日志：操作类型 - 登录
     */
    public static final String OPERATION_TYPE_LOGIN = "登录";
    
    /**
     * 操作日志：操作类型 - 登出
     */
    public static final String OPERATION_TYPE_LOGOUT = "登出";
}

