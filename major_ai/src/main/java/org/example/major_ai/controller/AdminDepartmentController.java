package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.DepartmentEntity;
import org.example.major_ai.service.DepartmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
public class AdminDepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<DepartmentEntity>> listAll() {
        return ApiResponse.success(departmentService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ApiResponse<DepartmentEntity> create(@RequestBody DepartmentEntity department) {
        DepartmentEntity created = departmentService.create(department);
        return ApiResponse.success("科室创建成功", created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody DepartmentEntity department) {
        department.setId(id);
        departmentService.update(department);
        return ApiResponse.success("科室更新成功", null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ApiResponse<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        departmentService.updateStatus(id, status);
        return ApiResponse.success("状态更新成功", null);
    }
}
