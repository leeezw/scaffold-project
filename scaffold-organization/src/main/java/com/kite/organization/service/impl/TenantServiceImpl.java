package com.kite.organization.service.impl;

import com.kite.common.util.PageResult;
import com.kite.organization.dto.TenantDTO;
import com.kite.organization.dto.TenantRequest;
import com.kite.organization.entity.Tenant;
import com.kite.organization.mapper.TenantMapper;
import com.kite.organization.service.TenantService;
import com.kite.organization.vo.TenantOptionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TenantServiceImpl implements TenantService {

    private final TenantMapper tenantMapper;

    public TenantServiceImpl(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    @Override
    public PageResult<TenantDTO> pageTenants(String keyword, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        List<Tenant> tenants = tenantMapper.selectList(keyword);
        if (CollectionUtils.isEmpty(tenants)) {
            return PageResult.of(Collections.emptyList(), 0L, pageNum, pageSize);
        }
        int total = tenants.size();
        int fromIndex = Math.min((pageNum - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<TenantDTO> pageList = tenants.subList(fromIndex, toIndex)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return PageResult.of(pageList, (long) total, pageNum, pageSize);
    }

    @Override
    public List<TenantOptionVO> listOptions() {
        List<Tenant> tenants = tenantMapper.selectList(null);
        if (CollectionUtils.isEmpty(tenants)) {
            return Collections.emptyList();
        }
        return tenants.stream().map(this::toOption).collect(Collectors.toList());
    }

    @Override
    public List<TenantOptionVO> listOptionsByUser(Long userId, Long tenantId) {
        if (tenantId != null && tenantId > 0) {
            Tenant tenant = tenantMapper.selectById(tenantId);
            if (tenant == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(toOption(tenant));
        }
        return listOptions();
    }

    @Override
    public TenantDTO getDetail(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            return null;
        }
        return toDTO(tenant);
    }

    @Override
    public void create(TenantRequest request) {
        Tenant existed = tenantMapper.selectByCode(request.getCode());
        if (existed != null) {
            throw new IllegalArgumentException("租户编码已存在");
        }
        Tenant tenant = new Tenant();
        BeanUtils.copyProperties(request, tenant);
        tenant.setCreateTime(LocalDateTime.now());
        tenant.setUpdateTime(LocalDateTime.now());
        tenantMapper.insert(tenant);
    }

    @Override
    public void update(Long id, TenantRequest request) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new IllegalArgumentException("租户不存在");
        }
        if (!Objects.equals(tenant.getCode(), request.getCode())) {
            Tenant existed = tenantMapper.selectByCode(request.getCode());
            if (existed != null && !Objects.equals(existed.getId(), id)) {
                throw new IllegalArgumentException("租户编码已存在");
            }
        }
        BeanUtils.copyProperties(request, tenant);
        tenant.setId(id);
        tenant.setUpdateTime(LocalDateTime.now());
        tenantMapper.update(tenant);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new IllegalArgumentException("租户不存在");
        }
        tenant.setStatus(status);
        tenant.setUpdateTime(LocalDateTime.now());
        tenantMapper.update(tenant);
    }

    @Override
    public void delete(Long id) {
        tenantMapper.delete(id);
    }

    private TenantDTO toDTO(Tenant tenant) {
        TenantDTO dto = new TenantDTO();
        BeanUtils.copyProperties(tenant, dto);
        return dto;
    }

    private TenantOptionVO toOption(Tenant tenant) {
        TenantOptionVO vo = new TenantOptionVO();
        vo.setId(tenant.getId());
        vo.setCode(tenant.getCode());
        vo.setName(tenant.getName());
        vo.setStatus(tenant.getStatus());
        vo.setExpiredAt(tenant.getExpiredAt());
        return vo;
    }
}
