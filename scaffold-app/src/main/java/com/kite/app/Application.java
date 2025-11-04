package com.kite.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类
 * 
 * @author yourname
 */
@SpringBootApplication
@MapperScan("com.kite.usercenter.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("=================================");
        System.out.println("应用启动成功！");
        System.out.println("=================================");
    }
}

