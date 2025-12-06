package com.kite.organization.service;

import com.kite.common.util.PageResult;
import com.kite.organization.dto.TenantDTO;
import com.kite.organization.dto.TenantRequest;
import com.kite.organization.vo.TenantOptionVO;

import java.util.List;

public interface TenantService {

    PageResult<TenantDTO> pageTenants(String keyword, Integer pageNum, Integer pageSize);

    List<TenantOptionVO> listOptions();

    List<TenantOptionVO> listOptionsByUser(Long userId, Long tenantId);

    TenantDTO getDetail(Long id);

    void create(TenantRequest request);

    void update(Long id, TenantRequest request);

    void changeStatus(Long id, Integer status);

    void delete(Long id);
}
