package com.kite.usercenter.mapper;

import com.kite.usercenter.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    
    int insert(User user);
    
    int update(User user);
    
    int deleteById(Long id);
    
    User selectById(Long id);
    
    User selectByUsername(String username);
    
    List<User> selectPage(@Param("keyword") String keyword,
                          @Param("status") Integer status,
                          @Param("sortField") String sortField,
                          @Param("sortOrder") String sortOrder,
                          @Param("offset") Integer offset,
                          @Param("limit") Integer limit);
    
    long count(@Param("keyword") String keyword,
               @Param("status") Integer status);
    
    /**
     * 统计启用用户数
     */
    long countEnabled(@Param("keyword") String keyword);
    
    /**
     * 统计禁用用户数
     */
    long countDisabled(@Param("keyword") String keyword);
    
    /**
     * 统计今日新增用户数
     */
    long countTodayNew(@Param("keyword") String keyword);
}
