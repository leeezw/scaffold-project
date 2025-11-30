package com.kite.usercenter.controller;

import com.kite.authenticator.context.LoginUserContext;
import com.kite.common.response.Result;
import com.kite.usercenter.service.MenuService;
import com.kite.usercenter.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单接口
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/my")
    public Result<List<MenuVO>> myMenus() {
        Long userId = LoginUserContext.getUserId();
        return Result.success(menuService.listUserMenus(userId));
    }
}
