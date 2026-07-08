package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.AppointmentEntity;
import org.example.major_ai.mapper.AppointmentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约挂号服务
 * 负责预约的创建、查询和状态管理
 *
 * 预约状态流转：
 * PENDING（待确认）→ CONFIRMED（已确认）→ COMPLETED（已完成）
 *                                      → CANCELLED（已取消）
 *
 * 调用方：
 * - AppointmentController：用户预约接口
 * - AdminAppointmentController：管理员预约管理
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    /** 预约Mapper，负责appointment表的CRUD */
    private final AppointmentMapper appointmentMapper;

    /**
     * 创建预约
     *
     * @param appointment 预约实体
     * @return 创建成功的预约
     */
    public AppointmentEntity create(AppointmentEntity appointment) {
        // ① 日期校验：必须在明天 00:00 ~ 明天+30天之内
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime maxDate = now.plusDays(31).withHour(23).withMinute(59).withSecond(59).withNano(0);

        if (appointment.getAppointmentTime() == null) {
            throw new IllegalArgumentException("请选择预约时间");
        }
        if (appointment.getAppointmentTime().isBefore(tomorrow)) {
            throw new IllegalArgumentException("预约时间不能早于明天");
        }
        if (appointment.getAppointmentTime().isAfter(maxDate)) {
            throw new IllegalArgumentException("预约时间不能超过30天");
        }

        // ② 重复检查：同一科室 + 同一时间 + 非取消状态
        Long count = appointmentMapper.selectCount(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getDepartmentId, appointment.getDepartmentId())
                        .eq(AppointmentEntity::getAppointmentTime, appointment.getAppointmentTime())
                        .ne(AppointmentEntity::getStatus, "CANCELLED")
        );
        if (count > 0) {
            throw new IllegalArgumentException("该时段该科室已有预约，请选择其他时间");
        }

        long timestamp = System.currentTimeMillis();
        appointment.setCreatedAt(timestamp);
        appointment.setUpdatedAt(timestamp);
        appointment.setStatus("PENDING");  // 初始状态：待确认

        // SQL: INSERT INTO appointment(user_id, doctor_id, department_id, appointment_time, status, created_at, updated_at)
        //      VALUES(?, ?, ?, ?, 'PENDING', ?, ?)
        appointmentMapper.insert(appointment);
        return appointment;
    }

    /**
     * 查询用户的预约列表
     *
     * @param userId 用户ID
     * @return 预约列表，按预约时间降序排列
     */
    public List<AppointmentEntity> listByUserId(Long userId) {
        // SQL: SELECT * FROM appointment WHERE user_id = ? ORDER BY appointment_time DESC
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getUserId, userId)
                        .orderByDesc(AppointmentEntity::getAppointmentTime)
        );
    }

    /**
     * 查询科室的预约列表
     *
     * @param departmentId 科室ID
     * @return 预约列表，按预约时间降序排列
     */
    public List<AppointmentEntity> listByDepartmentId(Long departmentId) {
        // SQL: SELECT * FROM appointment WHERE department_id = ? ORDER BY appointment_time DESC
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getDepartmentId, departmentId)
                        .orderByDesc(AppointmentEntity::getAppointmentTime)
        );
    }

    /**
     * 查询医生的预约列表
     *
     * @param doctorId 医生ID
     * @return 预约列表，按预约时间降序排列
     */
    public List<AppointmentEntity> listByDoctorId(Long doctorId) {
        // SQL: SELECT * FROM appointment WHERE doctor_id = ? ORDER BY appointment_time DESC
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getDoctorId, doctorId)
                        .orderByDesc(AppointmentEntity::getAppointmentTime)
        );
    }

    /**
     * 根据ID查询预约
     *
     * @param id 预约ID
     * @return 预约实体，不存在返回null
     */
    public AppointmentEntity findById(Long id) {
        // SQL: SELECT * FROM appointment WHERE id = ?
        return appointmentMapper.selectById(id);
    }

    /**
     * 更新预约状态
     *
     * @param id 预约ID
     * @param status 新状态（PENDING/CONFIRMED/COMPLETED/CANCELLED）
     * @return 更新成功返回true
     */
    public boolean updateStatus(Long id, String status) {
        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setId(id);
        appointment.setStatus(status);
        appointment.setUpdatedAt(System.currentTimeMillis());

        // SQL: UPDATE appointment SET status = ?, updated_at = ? WHERE id = ?
        return appointmentMapper.updateById(appointment) > 0;
    }
}
