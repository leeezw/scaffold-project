package com.kite.usercenter.service.impl;

import com.kite.common.exception.BusinessException;
import com.kite.common.response.ResultCode;
import com.kite.usercenter.dto.OrganizationDTO;
import com.kite.usercenter.dto.OrganizationRequest;
import com.kite.usercenter.entity.Organization;
import com.kite.usercenter.mapper.OrganizationMapper;
import com.kite.usercenter.service.OrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    
    private final OrganizationMapper organizationMapper;
    
    public OrganizationServiceImpl(OrganizationMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }
    
    @Override
    public List<OrganizationDTO> tree(Long tenantId) {
        if (tenantId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "tenantId 不能为空");
        }
        List<Organization> organizations = organizationMapper.selectByTenant(tenantId);
        if (CollectionUtils.isEmpty(organizations)) {
            return Collections.emptyList();
        }
        Map<Long, OrganizationDTO> cache = new LinkedHashMap<>();
        List<OrganizationDTO> roots = new ArrayList<>();
        for (Organization org : organizations) {
            OrganizationDTO dto = toDTO(org);
            cache.put(dto.getId(), dto);
        }
        for (OrganizationDTO dto : cache.values()) {
            Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
            if (parentId == 0) {
                roots.add(dto);
            } else {
                OrganizationDTO parent = cache.get(parentId);
                if (parent == null) {
                    roots.add(dto);
                } else {
                    parent.getChildren().add(dto);
                }
            }
        }
        sortTree(roots);
        return roots;
    }
    
    @Override
    public OrganizationDTO getById(Long id) {
        Organization organization = organizationMapper.selectById(id);
        if (organization == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return toDTO(organization);
    }
    
    @Override
    public void create(OrganizationRequest request) {
        Organization entity = new Organization();
        entity.setTenantId(request.getTenantId());
        entity.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setSort(request.getSort());
        entity.setLeaderId(request.getLeaderId());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        entity.setPath(buildPath(entity.getParentId()));
        organizationMapper.insert(entity);
    }
    
    @Override
    public void update(Long id, OrganizationRequest request) {
        Organization db = organizationMapper.selectById(id);
        if (db == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        db.setTenantId(request.getTenantId());
        db.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        db.setName(request.getName());
        db.setType(request.getType());
        db.setSort(request.getSort());
        db.setLeaderId(request.getLeaderId());
        db.setStatus(request.getStatus());
        db.setRemark(request.getRemark());
        db.setPath(buildPath(db.getParentId()));
        organizationMapper.update(db);
    }
    
    @Override
    public void delete(Long id) {
        Organization organization = organizationMapper.selectById(id);
        if (organization == null) {
            return;
        }
        List<Organization> children = organizationMapper.selectChildren(organization.getTenantId(), id);
        if (!CollectionUtils.isEmpty(children)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请先删除子节点");
        }
        organizationMapper.deleteById(id);
    }
    
    private void sortTree(List<OrganizationDTO> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        nodes.sort(Comparator.comparing(OrganizationDTO::getSort, Comparator.nullsLast(Integer::compareTo)));
        for (OrganizationDTO node : nodes) {
            sortTree(node.getChildren());
        }
    }
    
    private String buildPath(Long parentId) {
        if (parentId == null || parentId == 0) {
            return "0";
        }
        Organization parent = organizationMapper.selectById(parentId);
        if (parent == null) {
            return "0";
        }
        String parentPath = parent.getPath() == null ? "0" : parent.getPath();
        return parentPath + "/" + parentId;
    }
    
    private OrganizationDTO toDTO(Organization organization) {
        OrganizationDTO dto = new OrganizationDTO();
        dto.setId(organization.getId());
        dto.setTenantId(organization.getTenantId());
        dto.setParentId(organization.getParentId());
        dto.setName(organization.getName());
        dto.setType(organization.getType());
        dto.setSort(organization.getSort());
        dto.setPath(organization.getPath());
        dto.setLeaderId(organization.getLeaderId());
        dto.setStatus(organization.getStatus());
        dto.setRemark(organization.getRemark());
        dto.setCreateTime(organization.getCreateTime());
        dto.setUpdateTime(organization.getUpdateTime());
        return dto;
    }
}
