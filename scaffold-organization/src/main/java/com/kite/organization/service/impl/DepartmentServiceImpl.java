package com.kite.organization.service.impl;

import com.kite.organization.dto.DepartmentDTO;
import com.kite.organization.dto.DepartmentRequest;
import com.kite.organization.entity.Department;
import com.kite.organization.mapper.DepartmentMapper;
import com.kite.organization.service.DepartmentService;
import com.kite.organization.vo.DepartmentTreeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<DepartmentTreeVO> listTree(Long tenantId) {
        List<Department> departments = departmentMapper.selectByTenantId(tenantId);
        if (CollectionUtils.isEmpty(departments)) {
            return Collections.emptyList();
        }
        Map<Long, DepartmentTreeVO> cache = new HashMap<>();
        List<DepartmentTreeVO> roots = new ArrayList<>();
        departments.forEach(dept -> {
            DepartmentTreeVO node = toTreeNode(dept);
            cache.put(dept.getId(), node);
        });
        departments.forEach(dept -> {
            if (dept.getParentId() == null || dept.getParentId() == 0) {
                roots.add(cache.get(dept.getId()));
                return;
            }
            DepartmentTreeVO parent = cache.get(dept.getParentId());
            if (parent == null) {
                roots.add(cache.get(dept.getId()));
                return;
            }
            parent.getChildren().add(cache.get(dept.getId()));
        });
        Comparator<DepartmentTreeVO> comparator = Comparator
                .comparing((DepartmentTreeVO node) -> findSort(node.getId(), departments))
                .thenComparing(DepartmentTreeVO::getName);
        sortTree(roots, comparator, departments);
        return roots;
    }

    @Override
    public DepartmentDTO detail(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            return null;
        }
        DepartmentDTO dto = new DepartmentDTO();
        BeanUtils.copyProperties(department, dto);
        return dto;
    }

    @Override
    public void create(DepartmentRequest request) {
        Department department = new Department();
        BeanUtils.copyProperties(request, department);
        department.setCreateTime(LocalDateTime.now());
        department.setUpdateTime(LocalDateTime.now());
        setDepartmentPath(department);
        departmentMapper.insert(department);
    }

    @Override
    public void update(Long id, DepartmentRequest request) {
        Department existed = departmentMapper.selectById(id);
        if (existed == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        BeanUtils.copyProperties(request, existed);
        existed.setId(id);
        existed.setUpdateTime(LocalDateTime.now());
        setDepartmentPath(existed);
        departmentMapper.update(existed);
    }

    @Override
    public void delete(Long id) {
        departmentMapper.delete(id);
    }

    private void sortTree(List<DepartmentTreeVO> nodes, Comparator<DepartmentTreeVO> comparator, List<Department> origin) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort(comparator);
        nodes.forEach(node -> sortTree(node.getChildren(), comparator, origin));
    }

    private int findSort(Long id, List<Department> departments) {
        return departments.stream()
                .filter(item -> Objects.equals(item.getId(), id))
                .map(Department::getSort)
                .findFirst()
                .orElse(0);
    }

    private DepartmentTreeVO toTreeNode(Department department) {
        DepartmentTreeVO vo = new DepartmentTreeVO();
        vo.setId(department.getId());
        vo.setName(department.getName());
        vo.setCode(department.getCode());
        vo.setLeaderName(department.getLeaderName());
        return vo;
    }

    private void setDepartmentPath(Department department) {
        if (department.getParentId() == null || department.getParentId() == 0) {
            department.setPath("/");
            return;
        }
        Department parent = departmentMapper.selectById(department.getParentId());
        if (parent == null) {
            department.setPath("/");
            return;
        }
        String parentPath = parent.getPath() == null ? "/" : parent.getPath();
        department.setPath(parentPath.endsWith("/") ? parentPath + parent.getId() : parentPath + "/" + parent.getId());
    }
}
