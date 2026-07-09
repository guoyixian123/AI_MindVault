package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.AppointmentEntity;
import org.example.major_ai.mapper.AppointmentMapper;
import org.example.major_ai.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 预约挂号服务
 * 负责预约的创建、查询和状态管理
 *
 * 预约状态流转：
 * PENDING（待确认）→ CONFIRMED（已确认）→ COMPLETED（已完成）
 *               → CANCELLED（已取消）
 * CONFIRMED    → CANCELLED（已取消）
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

    /** 科室Mapper，用于校验科室是否存在 */
    private final DepartmentMapper departmentMapper;

    /** 有效的预约状态 */
    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");

    /** 允许的状态流转 */
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
        "PENDING",   Set.of("CONFIRMED", "CANCELLED"),
        "CONFIRMED", Set.of("COMPLETED", "CANCELLED")
    );

    /**
     * 创建预约（事务保护，防止并发重复插入）
     *
     * @param appointment 预约实体
     * @return 创建成功的预约
     */
    @Transactional
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

        // ② 时段校验：上午 9:00-12:00，下午 14:00-18:00
        int hour = appointment.getAppointmentTime().getHour();
        int minute = appointment.getAppointmentTime().getMinute();
        boolean validMorning = hour >= 9 && hour < 12;
        boolean validAfternoon = hour >= 14 && hour < 18;
        if ((!validMorning && !validAfternoon) || minute % 30 != 0) {
            throw new IllegalArgumentException("预约时间仅支持 上午9:00-12:00 或 下午14:00-18:00，每30分钟一个时段");
        }

        // ③ 科室存在性校验
        if (appointment.getDepartmentId() == null
                || departmentMapper.selectById(appointment.getDepartmentId()) == null) {
            throw new IllegalArgumentException("所选科室不存在");
        }

        // ④ 重复检查：同一用户 + 同一时间 + 非取消状态（同一用户不能同时约多个号）
        Long userCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getUserId, appointment.getUserId())
                        .eq(AppointmentEntity::getAppointmentTime, appointment.getAppointmentTime())
                        .ne(AppointmentEntity::getStatus, "CANCELLED")
        );
        if (userCount > 0) {
            throw new IllegalArgumentException("该时段您已有预约，请选择其他时间");
        }

        // ⑤ 重复检查：同一科室 + 同一时间 + 非取消状态
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

        appointmentMapper.insert(appointment);
        return appointment;
    }

    /**
     * 查询所有预约（ROOT_ADMIN专用）
     */
    public List<AppointmentEntity> listAll() {
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .orderByDesc(AppointmentEntity::getAppointmentTime)
        );
    }

    /**
     * 查询用户的预约列表
     */
    public List<AppointmentEntity> listByUserId(Long userId) {
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getUserId, userId)
                        .orderByDesc(AppointmentEntity::getAppointmentTime)
        );
    }

    /**
     * 查询科室的预约列表
     */
    public List<AppointmentEntity> listByDepartmentId(Long departmentId) {
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getDepartmentId, departmentId)
                        .orderByDesc(AppointmentEntity::getAppointmentTime)
        );
    }

    /**
     * 查询医生的预约列表
     */
    public List<AppointmentEntity> listByDoctorId(Long doctorId) {
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<AppointmentEntity>()
                        .eq(AppointmentEntity::getDoctorId, doctorId)
                        .orderByDesc(AppointmentEntity::getAppointmentTime)
        );
    }

    /**
     * 根据ID查询预约
     */
    public AppointmentEntity findById(Long id) {
        return appointmentMapper.selectById(id);
    }

    /**
     * 更新预约状态（含状态合法性及流转校验）
     */
    public boolean updateStatus(Long id, String newStatus) {
        if (newStatus == null || !VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("无效的预约状态: " + newStatus);
        }

        // 状态流转校验（终态不可再变更）
        AppointmentEntity existing = appointmentMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("预约不存在");
        }

        String currentStatus = existing.getStatus();
        Set<String> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new IllegalArgumentException(
                String.format("不允许从 %s 变更为 %s", currentStatus, newStatus));
        }

        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setId(id);
        appointment.setStatus(newStatus);
        appointment.setUpdatedAt(System.currentTimeMillis());

        return appointmentMapper.updateById(appointment) > 0;
    }
}
