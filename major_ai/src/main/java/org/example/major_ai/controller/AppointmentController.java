package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.AppointmentEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.AppointmentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约挂号控制器
 * 提供预约的创建、查询和取消接口
 *
 * API前缀：/api/appointment
 *
 * 预约状态：
 * - PENDING: 待确认
 * - CONFIRMED: 已确认
 * - COMPLETED: 已完成
 * - CANCELLED: 已取消
 */
@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    /** 预约服务，负责预约的CRUD操作 */
    private final AppointmentService appointmentService;

    /**
     * 创建预约
     *
     * 请求：POST /api/appointment
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"doctorId": 1, "departmentId": 3, "appointmentTime": "2024-01-15 10:00:00"}
     * 响应：{"code": 200, "message": "预约挂号成功", "data": {...}}
     *
     * @param user 当前登录用户
     * @param appointment 预约实体
     * @return 创建成功的预约
     */
    @PostMapping
    public ApiResponse<AppointmentEntity> createAppointment(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody AppointmentEntity appointment) {
        // 设置预约用户ID
        appointment.setUserId(user.getId());
        AppointmentEntity created = appointmentService.create(appointment);
        return ApiResponse.success("预约挂号成功", created);
    }

    /**
     * 查询我的预约列表
     *
     * 请求：GET /api/appointment
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * @param user 当前登录用户
     * @return 预约列表
     */
    @GetMapping
    public ApiResponse<List<AppointmentEntity>> myAppointments(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<AppointmentEntity> appointments = appointmentService.listByUserId(user.getId());
        return ApiResponse.success(appointments);
    }

    /**
     * 查询预约详情
     *
     * 请求：GET /api/appointment/{id}
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": {...}}
     *
     * @param user 当前登录用户
     * @param id 预约ID
     * @return 预约详情，不存在或无权限返回404
     */
    @GetMapping("/{id}")
    public ApiResponse<AppointmentEntity> getAppointment(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        AppointmentEntity appointment = appointmentService.findById(id);

        // 验证权限：只能查看自己的预约
        if (appointment == null || !appointment.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "预约不存在");
        }

        return ApiResponse.success(appointment);
    }

    /**
     * 取消预约
     *
     * 请求：PUT /api/appointment/{id}/cancel
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "message": "预约已取消"}
     *
     * @param user 当前登录用户
     * @param id 预约ID
     * @return 取消结果
     */
    @PutMapping("/{id}/cancel")
    public ApiResponse<String> cancelAppointment(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        AppointmentEntity appointment = appointmentService.findById(id);

        // 验证权限：只能取消自己的预约
        if (appointment == null || !appointment.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "预约不存在");
        }

        // 更新状态为已取消
        appointmentService.updateStatus(id, "CANCELLED");
        return ApiResponse.success("预约已取消", null);
    }
}
