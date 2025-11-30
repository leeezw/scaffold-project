package com.kite.usercenter.vo;

import com.kite.usercenter.dto.PermissionDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class RolePermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限树
     */
    private List<PermissionDTO> tree = Collections.emptyList();

    /**
     * 已授权的权限ID
     */
    private List<Long> checkedKeys = Collections.emptyList();
}
