package com.kite.organization.service;

import com.kite.organization.dto.PositionDTO;
import com.kite.organization.dto.PositionRequest;
import com.kite.organization.vo.PositionOptionVO;

import java.util.List;

public interface PositionService {

    List<PositionDTO> listByTenant(Long tenantId);

    List<PositionOptionVO> listOptions(Long tenantId);

    PositionDTO detail(Long id);

    void create(PositionRequest request);

    void update(Long id, PositionRequest request);

    void delete(Long id);
}
