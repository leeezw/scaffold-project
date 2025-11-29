package com.kite.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置
 * 统一管理 JSON 序列化配置，避免在 DTO 中滥用 @JsonFormat 注解
 * 
 * @author yourname
 */
@Configuration
public class JacksonConfig {
    
    /**
     * 统一的日期时间格式：yyyy/MM/dd HH:mm:ss
     */
    public static final String DATE_TIME_PATTERN = "yyyy/MM/dd HH:mm:ss";
    
    /**
     * 日期时间格式化器
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 配置 LocalDateTime 序列化和反序列化
        javaTimeModule.addSerializer(LocalDateTime.class, 
            new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, 
            new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        
        return builder
            .modules(javaTimeModule)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }
}

