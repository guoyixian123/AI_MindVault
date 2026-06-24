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

@RestController
@RequestMapping("/api/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    @GetMapping
    public ApiResponse<List<AppointmentEntity>> listAppointments(
            @AuthenticationPrincipal UserDetailsImpl user) {
        UserEntity doctor = userService.findById(user.getId());
        List<AppointmentEntity> appointments;

        if ("ROOT_ADMIN".equals(doctor.getRole())) {
            // 根管理员查看所有预约（这里简化处理，实际可加筛选）
            appointments = appointmentService.listByDoctorId(null);
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

    @PutMapping("/{id}/status")
    public ApiResponse<String> updateStatus(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id,
            @RequestParam String status) {
        appointmentService.updateStatus(id, status);
        return ApiResponse.success("预约状态更新成功", null);
    }
}
