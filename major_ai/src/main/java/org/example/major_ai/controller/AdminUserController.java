package org.example.major_ai.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.UserEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台用户控制器
 * 提供用户管理接口（仅ROOT_ADMIN可用）
 *
 * API前缀：/api/admin/users
 *
 * 功能：
 * - 创建医生账号
 * - 查询用户列表
 * - 更新用户状态（启用/停用）
 * - 绑定医生科室
 *
 * 权限：需要ROOT_ADMIN角色
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    /** 用户服务 */
    private final UserService userService;

    /**
     * 创建医生账号
     *
     * 请求：POST /api/admin/users
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN角色）
     * 请求体：{"username": "doctor1", "password": "123456", "nickname": "张医生", "departmentId": 3}
     * 响应：{"code": 200, "message": "医生账号创建成功", "data": {...}}
     *
     * @param admin 当前登录的管理员
     * @param request 创建医生请求
     * @return 创建成功的医生账号
     */
    @PostMapping
    @PreAuthorize("hasRole('ROOT_ADMIN')")  // 只有ROOT_ADMIN可以创建医生
    public ApiResponse<UserEntity> createDoctor(
            @AuthenticationPrincipal UserDetailsImpl admin,
            @RequestBody CreateDoctorRequest request) {
        // 检查用户名是否已存在
        if (userService.findByUsername(request.getUsername()) != null) {
            return ApiResponse.error(400, "用户名已存在");
        }

        // 创建医生账号
        UserEntity doctor = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                request.getPhone(),
                request.getEmail(),
                "DOCTOR"  // 角色为医生
        );

        // 绑定科室
        if (request.getDepartmentId() != null) {
            userService.updateUserDepartment(doctor.getId(), request.getDepartmentId());
        }

        return ApiResponse.success("医生账号创建成功", doctor);
    }

    /**
     * 查询用户列表
     *
     * 请求：GET /api/admin/users?role=DOCTOR&status=1
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN角色）
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * @param role 角色筛选（可选）
     * @param status 状态筛选（可选，1=启用，0=停用）
     * @return 用户列表（已清除密码哈希）
     */
    @GetMapping
    @PreAuthorize("hasRole('ROOT_ADMIN')")  // 只有ROOT_ADMIN可以查询用户列表
    public ApiResponse<List<UserEntity>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        List<UserEntity> users = userService.listUsers(role, status);

        // 清除密码哈希，不返回给前端
        users.forEach(u -> u.setPasswordHash(null));

        return ApiResponse.success(users);
    }

    /**
     * 更新用户状态
     *
     * 请求：PUT /api/admin/users/{id}/status?status=0
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN角色）
     * 响应：{"code": 200, "message": "状态更新成功"}
     *
     * @param id 用户ID
     * @param status 新状态（1=启用，0=停用）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROOT_ADMIN')")  // 只有ROOT_ADMIN可以更新用户状态
    public ApiResponse<String> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return ApiResponse.success("状态更新成功", null);
    }

    /**
     * 绑定医生科室
     *
     * 请求：PUT /api/admin/users/{id}/department?departmentId=3
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN角色）
     * 响应：{"code": 200, "message": "科室绑定成功"}
     *
     * @param id 用户ID
     * @param departmentId 科室ID
     * @return 更新结果
     */
    @PutMapping("/{id}/department")
    @PreAuthorize("hasRole('ROOT_ADMIN')")  // 只有ROOT_ADMIN可以绑定科室
    public ApiResponse<String> updateDepartment(
            @PathVariable Long id,
            @RequestParam Long departmentId) {
        userService.updateUserDepartment(id, departmentId);
        return ApiResponse.success("科室绑定成功", null);
    }

    /**
     * 创建医生请求体
     */
    @Data
    public static class CreateDoctorRequest {
        /** 用户名 */
        private String username;
        /** 密码 */
        private String password;
        /** 昵称 */
        private String nickname;
        /** 手机号 */
        private String phone;
        /** 邮箱 */
        private String email;
        /** 科室ID */
        private Long departmentId;
    }
}
