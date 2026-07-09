package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.AppointmentEntity;
import org.example.major_ai.entity.UserEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.AppointmentService;
import org.example.major_ai.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台预约控制器
 * 提供预约管理接口
 *
 * API前缀：/api/admin/appointments
 *
 * 权限：需要ROOT_ADMIN或DOCTOR角色
 *
 * 数据隔离：
 * - ROOT_ADMIN：查看所有预约
 * - DOCTOR：查看自己科室的预约
 */
@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController {

    /** 预约服务 */
    private final AppointmentService appointmentService;

    /** 用户服务 */
    private final UserService userService;

    /**
     * 查询预约列表
     *
     * 请求：GET /api/admin/appointments
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN或DOCTOR角色）
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * 数据隔离：
     * - ROOT_ADMIN：查看所有预约
     * - DOCTOR：查看自己科室的预约
     *
     * @param user 当前登录用户
     * @return 预约列表
     */
    @GetMapping
    public ApiResponse<List<AppointmentEntity>> listAppointments(
            @AuthenticationPrincipal UserDetailsImpl user) {
        // 查询当前用户信息
        UserEntity doctor = userService.findById(user.getId());
        List<AppointmentEntity> appointments;

        if ("ROOT_ADMIN".equals(doctor.getRole())) {
            // ROOT_ADMIN查看所有预约
            appointments = appointmentService.listAll();
        } else {
            // 医生查看自己科室的预约
            if (doctor.getDepartmentId() != null) {
                appointments = appointmentService.listByDepartmentId(doctor.getDepartmentId());
            } else {
                appointments = appointmentService.listByDoctorId(doctor.getId());
            }
        }
        return ApiResponse.success(appointments);
    }

    /**
     * 更新预约状态
     *
     * 请求：PUT /api/admin/appointments/{id}/status?status=CONFIRMED
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN或DOCTOR角色）
     * 响应：{"code": 200, "message": "预约状态更新成功"}
     *
     * 可选状态：
     * - PENDING: 待确认
     * - CONFIRMED: 已确认
     * - COMPLETED: 已完成
     * - CANCELLED: 已取消
     *
     * @param user 当前登录用户
     * @param id 预约ID
     * @param status 新状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public ApiResponse<String> updateStatus(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            appointmentService.updateStatus(id, status);
            return ApiResponse.success("预约状态更新成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
