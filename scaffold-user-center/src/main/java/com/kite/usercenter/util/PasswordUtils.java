package com.kite.usercenter.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt 密码工具
 */
public class PasswordUtils {
    
    private PasswordUtils() {}
    
    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
    
    public static boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }

    public static void main(String[] args) {
        System.out.println(PasswordUtils.hash("123456"));
    }
}
