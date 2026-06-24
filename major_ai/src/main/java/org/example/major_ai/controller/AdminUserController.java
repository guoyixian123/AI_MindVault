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

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ApiResponse<UserEntity> createDoctor(
            @AuthenticationPrincipal UserDetailsImpl admin,
            @RequestBody CreateDoctorRequest request) {
        if (userService.findByUsername(request.getUsername()) != null) {
            return ApiResponse.error(400, "用户名已存在");
        }
        UserEntity doctor = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                request.getPhone(),
                request.getEmail(),
                "DOCTOR"
        );
        if (request.getDepartmentId() != null) {
            userService.updateUserDepartment(doctor.getId(), request.getDepartmentId());
        }
        return ApiResponse.success("医生账号创建成功", doctor);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ApiResponse<List<UserEntity>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        List<UserEntity> users = userService.listUsers(role, status);
        // 清除密码哈希
        users.forEach(u -> u.setPasswordHash(null));
        return ApiResponse.success(users);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ApiResponse<String> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return ApiResponse.success("状态更新成功", null);
    }

    @PutMapping("/{id}/department")
    @PreAuthorize("hasRole('ROOT_ADMIN')")
    public ApiResponse<String> updateDepartment(
            @PathVariable Long id,
            @RequestParam Long departmentId) {
        userService.updateUserDepartment(id, departmentId);
        return ApiResponse.success("科室绑定成功", null);
    }

    @Data
    public static class CreateDoctorRequest {
        private String username;
        private String password;
        private String nickname;
        private String phone;
        private String email;
        private Long departmentId;
    }
}
