package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.AppointmentEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.AppointmentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ApiResponse<AppointmentEntity> createAppointment(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody AppointmentEntity appointment) {
        appointment.setUserId(user.getId());
        AppointmentEntity created = appointmentService.create(appointment);
        return ApiResponse.success("预约挂号成功", created);
    }

    @GetMapping
    public ApiResponse<List<AppointmentEntity>> myAppointments(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<AppointmentEntity> appointments = appointmentService.listByUserId(user.getId());
        return ApiResponse.success(appointments);
    }

    @GetMapping("/{id}")
    public ApiResponse<AppointmentEntity> getAppointment(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        AppointmentEntity appointment = appointmentService.findById(id);
        if (appointment == null || !appointment.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "预约不存在");
        }
        return ApiResponse.success(appointment);
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<String> cancelAppointment(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        AppointmentEntity appointment = appointmentService.findById(id);
        if (appointment == null || !appointment.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "预约不存在");
        }
        appointmentService.updateStatus(id, "CANCELLED");
        return ApiResponse.success("预约已取消", null);
    }
}
