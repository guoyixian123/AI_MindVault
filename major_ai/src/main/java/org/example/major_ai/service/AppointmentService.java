package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.AppointmentEntity;
import org.example.major_ai.mapper.AppointmentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentMapper appointmentMapper;

    public AppointmentEntity create(AppointmentEntity appointment) {
        long now = System.currentTimeMillis();
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        appointment.setStatus("PENDING");
        appointmentMapper.insert(appointment);
        return appointment;
    }

    public List<AppointmentEntity> listByUserId(Long userId) {
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getUserId, userId)
                        .orderByDesc(AppointmentEntity::getAppointmentTime)
        );
    }

    public List<AppointmentEntity> listByDepartmentId(Long departmentId) {
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getDepartmentId, departmentId)
                        .orderByDesc(AppointmentEntity::getAppointmentTime)
        );
    }

    public List<AppointmentEntity> listByDoctorId(Long doctorId) {
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getDoctorId, doctorId)
                        .orderByDesc(AppointmentEntity::getAppointmentTime)
        );
    }

    public AppointmentEntity findById(Long id) {
        return appointmentMapper.selectById(id);
    }

    public boolean updateStatus(Long id, String status) {
        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setId(id);
        appointment.setStatus(status);
        appointment.setUpdatedAt(System.currentTimeMillis());
        return appointmentMapper.updateById(appointment) > 0;
    }
}
