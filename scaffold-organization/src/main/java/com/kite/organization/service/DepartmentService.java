package com.kite.organization.service;

import com.kite.organization.dto.DepartmentDTO;
import com.kite.organization.dto.DepartmentRequest;
import com.kite.organization.vo.DepartmentTreeVO;

import java.util.List;

public interface DepartmentService {

    List<DepartmentTreeVO> listTree(Long tenantId);

    DepartmentDTO detail(Long id);

    void create(DepartmentRequest request);

    void update(Long id, DepartmentRequest request);

    void delete(Long id);
}
