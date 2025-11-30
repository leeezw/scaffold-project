package com.kite.usercenter.mapper;

import com.kite.usercenter.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {
    
    int insert(Menu menu);
    
    int update(Menu menu);
    
    int deleteById(Long id);
    
    Menu selectById(Long id);
    
    List<Menu> selectAll(@Param("status") Integer status);
    
    List<Menu> selectByIds(@Param("ids") List<Long> ids);
}
