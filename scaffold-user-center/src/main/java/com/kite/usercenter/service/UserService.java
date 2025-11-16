package com.kite.usercenter.service;

import com.kite.usercenter.dto.*;
import com.kite.usercenter.entity.User;
import com.kite.common.util.PageResult;

import java.util.List;

public interface UserService {
    
    PageResult<UserDTO> pageUsers(UserPageRequest request);
    
    UserDTO getUserDetail(Long id);
    
    void createUser(UserCreateRequest request);
    
    void updateUser(UserUpdateRequest request);
    
    void changeStatus(ChangeStatusRequest request);
    
    void resetPassword(ResetPasswordRequest request);
    
    void assignRoles(Long userId, List<Long> roleIds);
    
    List<Long> listRoleIds(Long userId);
    
    User findByUsername(String username);
    
    User getById(Long id);
}
