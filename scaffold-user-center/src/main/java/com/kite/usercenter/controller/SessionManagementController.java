package com.kite.usercenter.controller;

import com.kite.authenticator.annotation.RequiresRoles;
import com.kite.authenticator.context.LoginUserContext;
import com.kite.authenticator.service.SessionManagementService;
import com.kite.authenticator.service.TokenBlacklistService;
import com.kite.authenticator.session.Session;
import com.kite.authenticator.session.enums.UserStatus;
import com.kite.common.annotation.OperationLog;
import com.kite.common.response.Result;
import com.kite.common.util.PageResult;
import com.kite.usercenter.dto.SessionListRequest;
import com.kite.usercenter.dto.UserDTO;
import com.kite.usercenter.service.UserService;
import com.kite.usercenter.vo.SessionInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Session 管理控制器
 * 
 * @author yourname
 */
@Tag(name = "Session 管理", description = "用户 Session 管理接口")
@RestController
@RequestMapping("/api/auth/session")
@RequiresRoles("admin")
public class SessionManagementController {
    
    @Autowired(required = false)
    private SessionManagementService sessionManagementService;
    
    @Autowired(required = false)
    private TokenBlacklistService tokenBlacklistService;
    
    @Autowired(required = false)
    private UserService userService;
    
    @Operation(summary = "获取当前用户的 Session 列表", description = "获取当前登录用户的所有 Session")
    @GetMapping("/my-sessions")
    @OperationLog(module = "Session管理", operationType = "查询", description = "查询我的Session列表")
    public Result<List<SessionInfoVO>> getMySessions() {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        List<Session> sessions = sessionManagementService.getUserSessions(userId);
        return Result.success(convertSessions(sessions));
    }
    
    @Operation(summary = "获取指定用户的 Session 列表", description = "管理员查询指定用户的所有 Session")
    @GetMapping("/user/{userId}")
    @OperationLog(module = "Session管理", operationType = "查询", description = "查询用户Session列表")
    public Result<List<SessionInfoVO>> getUserSessions(@Parameter(description = "用户ID") @PathVariable Long userId) {
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        List<Session> sessions = sessionManagementService.getUserSessions(userId);
        return Result.success(convertSessions(sessions));
    }
    
    @Operation(summary = "强制用户下线", description = "管理员强制指定用户下线（踢出所有设备）")
    @PostMapping("/kick-out/{userId}")
    @OperationLog(module = "Session管理", operationType = "删除", description = "强制用户下线")
    public Result<String> kickOutUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        sessionManagementService.kickOutUser(userId);
        return Result.success("用户已强制下线");
    }
    
    @Operation(summary = "踢出指定设备", description = "管理员踢出用户的指定设备")
    @PostMapping("/kick-out-device")
    @OperationLog(module = "Session管理", operationType = "删除", description = "踢出指定设备")
    public Result<String> kickOutDevice(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "设备ID", required = true) @RequestParam String deviceId) {
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        sessionManagementService.kickOutDevice(userId, deviceId);
        return Result.success("设备已踢出");
    }
    
    @Operation(summary = "禁用用户", description = "管理员禁用用户（禁用所有 Session）")
    @PostMapping("/disable/{userId}")
    @OperationLog(module = "Session管理", operationType = "修改", description = "禁用用户")
    public Result<String> disableUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        sessionManagementService.disableUser(userId);
        return Result.success("用户已禁用");
    }
    
    @Operation(summary = "撤销 Token", description = "管理员撤销指定的 Token（安全事件，将 Token 加入黑名单）")
    @PostMapping("/revoke-token")
    @OperationLog(module = "Session管理", operationType = "删除", description = "撤销Token（安全事件）")
    public Result<String> revokeToken(
            @Parameter(description = "Token", required = true) @RequestParam String token,
            @Parameter(description = "撤销原因", required = false) @RequestParam(required = false) String reason) {
        if (tokenBlacklistService == null) {
            return Result.fail("Token 黑名单服务未启用");
        }
        
        // 安全事件：将 Token 加入黑名单，并记录原因
        String finalReason = reason != null && !reason.isEmpty() 
            ? reason 
            : "管理员撤销（未指定原因）";
        
        tokenBlacklistService.blacklistToken(token, finalReason);
        return Result.success("Token 已撤销（安全事件）");
    }
    
    @Operation(summary = "分页获取 Session 列表", description = "支持按用户、关键字筛选 Session 列表")
    @PostMapping("/list")
    @OperationLog(module = "Session管理", operationType = "查询", description = "分页查询Session列表")
    public Result<PageResult<SessionInfoVO>> listSessions(@RequestBody SessionListRequest request) {
        if (sessionManagementService == null) {
            return Result.fail("Session 管理服务未启用");
        }
        
        int pageNum = request.getPageNum() != null && request.getPageNum() > 0 ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;
        
        List<Session> sessionList = sessionManagementService.listAllSessions();
        if (sessionList == null) {
            sessionList = new ArrayList<>();
        }
        
        Long targetUserId = request.getUserId();
        if (targetUserId != null) {
            final Long filterUserId = targetUserId;
            sessionList = sessionList.stream()
                    .filter(session -> filterUserId.equals(session.getUserId()))
                    .collect(Collectors.toList());
        }
        
        Set<Long> userIds = sessionList.stream()
                .map(Session::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, UserDTO> userInfoMap = loadUserInfo(userIds);
        
        String keyword = request.getKeyword();
        if (StringUtils.hasText(keyword)) {
            String lowerKeyword = keyword.toLowerCase();
            sessionList = sessionList.stream().filter(session -> {
                if (session.getSessionKey() != null && session.getSessionKey().toLowerCase().contains(lowerKeyword)) {
                    return true;
                }
                if (session.getDeviceId() != null && session.getDeviceId().toLowerCase().contains(lowerKeyword)) {
                    return true;
                }
                UserDTO user = userInfoMap.get(session.getUserId());
                if (user != null) {
                    if (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerKeyword)) {
                        return true;
                    }
                    if (user.getNickname() != null && user.getNickname().toLowerCase().contains(lowerKeyword)) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
        }
        
        sessionList.sort((a, b) -> {
            long timeA = getSortTime(a);
            long timeB = getSortTime(b);
            return Long.compare(timeB, timeA);
        });
        
        List<SessionInfoVO> voList = convertSessions(sessionList, userInfoMap);
        long total = voList.size();
        int fromIndex = Math.min((pageNum - 1) * pageSize, voList.size());
        int toIndex = Math.min(fromIndex + pageSize, voList.size());
        List<SessionInfoVO> pageList = voList.subList(fromIndex, toIndex);
        
        PageResult<SessionInfoVO> pageResult = PageResult.of(pageList, total, pageNum, pageSize);
        return Result.success(pageResult);
    }

    private List<SessionInfoVO> convertSessions(List<Session> sessions) {
        return convertSessions(sessions, null);
    }
    
    private List<SessionInfoVO> convertSessions(List<Session> sessions, Map<Long, UserDTO> userInfoMap) {
        List<SessionInfoVO> result = new ArrayList<>();
        if (sessions == null) {
            return result;
        }
        for (Session session : sessions) {
            SessionInfoVO vo = new SessionInfoVO();
            vo.setSessionKey(session.getSessionKey());
            vo.setUserId(session.getUserId());
            if (userInfoMap != null && session.getUserId() != null) {
                UserDTO user = userInfoMap.get(session.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                    vo.setNickname(user.getNickname());
                }
            }
            vo.setDeviceId(session.getDeviceId());
            vo.setStatus(session.getStatus());
            vo.setStatusDesc(resolveStatusDesc(session.getStatus()));
            vo.setStartTime(session.getStartTime());
            vo.setLastAccessTime(session.getLastAccessTime());
            vo.setExpireAt(session.getExpireAt());
            vo.setOperationTime(session.getOperateAt());
            result.add(vo);
        }
        return result;
    }
    
    private String resolveStatusDesc(Integer statusCode) {
        UserStatus status = UserStatus.fromCode(statusCode);
        switch (status) {
            case NORMAL:
                return "活跃";
            case DISABLED:
                return "已禁用";
            case KICK_OUT:
                return "强制下线";
            case DEVICE_KICK_OUT:
                return "设备已踢出";
            case REPLACED:
                return "已被顶替";
            default:
                return "未知";
        }
    }
    
    private Map<Long, UserDTO> loadUserInfo(Set<Long> userIds) {
        Map<Long, UserDTO> result = new HashMap<>();
        if (userService == null || userIds == null || userIds.isEmpty()) {
            return result;
        }
        for (Long userId : userIds) {
            if (userId == null) {
                continue;
            }
            try {
                UserDTO user = userService.getUserDetail(userId);
                if (user != null) {
                    result.put(userId, user);
                }
            } catch (Exception ignore) {
            }
        }
        return result;
    }
    
    private long getSortTime(Session session) {
        if (session.getOperateAt() != null) {
            return session.getOperateAt();
        }
        if (session.getLastAccessTime() != null) {
            return session.getLastAccessTime();
        }
        if (session.getStartTime() != null) {
            return session.getStartTime();
        }
        return 0L;
    }
}
