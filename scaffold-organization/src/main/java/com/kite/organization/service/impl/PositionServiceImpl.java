package com.kite.organization.service.impl;

import com.kite.organization.dto.PositionDTO;
import com.kite.organization.dto.PositionRequest;
import com.kite.organization.entity.Position;
import com.kite.organization.mapper.PositionMapper;
import com.kite.organization.service.PositionService;
import com.kite.organization.vo.PositionOptionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PositionServiceImpl implements PositionService {

    private final PositionMapper positionMapper;

    public PositionServiceImpl(PositionMapper positionMapper) {
        this.positionMapper = positionMapper;
    }

    @Override
    public List<PositionDTO> listByTenant(Long tenantId) {
        List<Position> positions = positionMapper.selectByTenantId(tenantId);
        if (CollectionUtils.isEmpty(positions)) {
            return Collections.emptyList();
        }
        return positions.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PositionOptionVO> listOptions(Long tenantId) {
        List<Position> positions = positionMapper.selectByTenantId(tenantId);
        if (CollectionUtils.isEmpty(positions)) {
            return Collections.emptyList();
        }
        return positions.stream().map(position -> {
            PositionOptionVO vo = new PositionOptionVO();
            vo.setId(position.getId());
            vo.setName(position.getName());
            vo.setCode(position.getCode());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PositionDTO detail(Long id) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            return null;
        }
        return toDTO(position);
    }

    @Override
    public void create(PositionRequest request) {
        Position position = new Position();
        BeanUtils.copyProperties(request, position);
        position.setCreateTime(LocalDateTime.now());
        position.setUpdateTime(LocalDateTime.now());
        positionMapper.insert(position);
    }

    @Override
    public void update(Long id, PositionRequest request) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new IllegalArgumentException("岗位不存在");
        }
        BeanUtils.copyProperties(request, position);
        position.setId(id);
        position.setUpdateTime(LocalDateTime.now());
        positionMapper.update(position);
    }

    @Override
    public void delete(Long id) {
        positionMapper.delete(id);
    }

    private PositionDTO toDTO(Position entity) {
        PositionDTO dto = new PositionDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
