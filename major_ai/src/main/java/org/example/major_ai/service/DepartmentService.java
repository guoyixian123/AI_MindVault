package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.DepartmentEntity;
import org.example.major_ai.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 科室服务
 * 负责科室的CRUD操作
 *
 * 调用方：
 * - AdminDepartmentController：科室管理
 * - AppointmentController：查询科室列表
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    /** 科室Mapper，负责department表的CRUD */
    private final DepartmentMapper departmentMapper;

    /**
     * 查询所有启用的科室
     *
     * @return 科室列表，按排序顺序排列
     */
    public List<DepartmentEntity> listAll() {
        // SQL: SELECT * FROM department WHERE status = 1 ORDER BY sort_order ASC
        return departmentMapper.selectList(
                new LambdaQueryWrapper<DepartmentEntity>()
                        .eq(DepartmentEntity::getStatus, 1)  // 只查启用的
                        .orderByAsc(DepartmentEntity::getSortOrder)  // 按排序字段升序
        );
    }

    /**
     * 创建科室
     *
     * @param department 科室实体
     * @return 创建成功的科室
     */
    public DepartmentEntity create(DepartmentEntity department) {
        department.setStatus(1);  // 默认启用
        long now = System.currentTimeMillis();
        department.setCreatedAt(now);
        department.setUpdatedAt(now);

        // SQL: INSERT INTO department(name, description, sort_order, status, created_at, updated_at)
        //      VALUES(?, ?, ?, 1, ?, ?)
        departmentMapper.insert(department);
        return department;
    }

    /**
     * 更新科室信息
     *
     * @param department 科室实体（包含ID和要更新的字段）
     * @return 更新成功返回true
     */
    public boolean update(DepartmentEntity department) {
        department.setUpdatedAt(System.currentTimeMillis());

        // SQL: UPDATE department SET name = ?, description = ?, sort_order = ?, updated_at = ? WHERE id = ?
        return departmentMapper.updateById(department) > 0;
    }

    /**
     * 更新科室状态
     *
     * @param id 科室ID
     * @param status 新状态（1=启用，0=停用）
     * @return 更新成功返回true
     */
    public boolean updateStatus(Long id, Integer status) {
        DepartmentEntity dept = new DepartmentEntity();
        dept.setId(id);
        dept.setStatus(status);
        dept.setUpdatedAt(System.currentTimeMillis());

        // SQL: UPDATE department SET status = ?, updated_at = ? WHERE id = ?
        return departmentMapper.updateById(dept) > 0;
    }
}
