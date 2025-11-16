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
                          @Param("offset") Integer offset,
                          @Param("limit") Integer limit);
    
    long count(@Param("keyword") String keyword,
               @Param("status") Integer status);
}
