package com.kite.usercenter.service;

import com.kite.usercenter.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务
 */
public interface MenuService {

    /**
     * 获取当前用户可见菜单树
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    List<MenuVO> listUserMenus(Long userId);
}
