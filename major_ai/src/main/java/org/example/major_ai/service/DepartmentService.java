package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.DepartmentEntity;
import org.example.major_ai.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentMapper departmentMapper;

    public List<DepartmentEntity> listAll() {
        return departmentMapper.selectList(
                new LambdaQueryWrapper<DepartmentEntity>()
                        .eq(DepartmentEntity::getStatus, 1)
                        .orderByAsc(DepartmentEntity::getSortOrder)
        );
    }

    public DepartmentEntity findById(Long id) {
        return departmentMapper.selectById(id);
    }

    public DepartmentEntity create(DepartmentEntity department) {
        department.setStatus(1);
        long now = System.currentTimeMillis();
        department.setCreatedAt(now);
        department.setUpdatedAt(now);
        departmentMapper.insert(department);
        return department;
    }

    public boolean update(DepartmentEntity department) {
        department.setUpdatedAt(System.currentTimeMillis());
        return departmentMapper.updateById(department) > 0;
    }

    public boolean updateStatus(Long id, Integer status) {
        DepartmentEntity dept = new DepartmentEntity();
        dept.setId(id);
        dept.setStatus(status);
        dept.setUpdatedAt(System.currentTimeMillis());
        return departmentMapper.updateById(dept) > 0;
    }
}
