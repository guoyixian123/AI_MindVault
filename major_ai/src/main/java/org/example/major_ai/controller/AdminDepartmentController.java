package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.DepartmentEntity;
import org.example.major_ai.service.DepartmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台科室控制器
 * 提供科室的CRUD接口
 *
 * API前缀：/api/admin/departments
 *
 * 权限：
 * - GET /api/admin/departments: 公开访问（无需认证）
 * - POST/PUT/DELETE: 需要ROOT_ADMIN角色
 */
@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
public class AdminDepartmentController {

    /** 科室服务 */
    private final DepartmentService departmentService;

    /**
     * 查询所有启用的科室（公开接口）
     *
     * 请求：GET /api/admin/departments
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * 注意：这个接口公开访问，无需认证
     *
     * @return 科室列表
     */
    @GetMapping
    public ApiResponse<List<DepartmentEntity>> listAll() {
        return ApiResponse.success(departmentService.listAll());
    }

    /**
     * 创建科室
     *
     * 请求：POST /api/admin/departments
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN角色）
     * 请求体：{"name": "内科", "description": "...", "sortOrder": 1}
     * 响应：{"code": 200, "message": "科室创建成功", "data": {...}}
     *
     * @param department 科室实体
     * @return 创建成功的科室
     */
    @PostMapping
    @PreAuthorize("hasRole('ROOT_ADMIN')")  // 只有ROOT_ADMIN可以创建科室
    public ApiResponse<DepartmentEntity> create(@RequestBody DepartmentEntity department) {
        DepartmentEntity created = departmentService.create(department);
        return ApiResponse.success("科室创建成功", created);
    }

    /**
     * 更新科室信息
     *
     * 请求：PUT /api/admin/departments/{id}
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN角色）
     * 请求体：{"name": "内科", "description": "...", "sortOrder": 1}
     * 响应：{"code": 200, "message": "科室更新成功"}
     *
     * @param id 科室ID
     * @param department 更新内容
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROOT_ADMIN')")  // 只有ROOT_ADMIN可以更新科室
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody DepartmentEntity department) {
        department.setId(id);
        departmentService.update(department);
        return ApiResponse.success("科室更新成功", null);
    }

    /**
     * 更新科室状态
     *
     * 请求：PUT /api/admin/departments/{id}/status?status=0
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN角色）
     * 响应：{"code": 200, "message": "状态更新成功"}
     *
     * @param id 科室ID
     * @param status 新状态（1=启用，0=停用）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROOT_ADMIN')")  // 只有ROOT_ADMIN可以更新科室状态
    public ApiResponse<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        departmentService.updateStatus(id, status);
        return ApiResponse.success("状态更新成功", null);
    }
}
