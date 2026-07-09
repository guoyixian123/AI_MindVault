package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 预约挂号实体类
 * 对应数据库表：appointment
 *
 * 表结构：
 * CREATE TABLE appointment (
 *     id BIGINT AUTO_INCREMENT PRIMARY KEY,
 *     user_id BIGINT NOT NULL,
 *     department_id BIGINT NOT NULL,
 *     doctor_id BIGINT,
 *     appointment_time DATETIME NOT NULL,
 *     complaint TEXT,
 *     status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
 *     created_at BIGINT NOT NULL,
 *     updated_at BIGINT NOT NULL
 * );
 *
 * 预约状态：
 * - PENDING: 待确认
 * - CONFIRMED: 已确认
 * - CANCELLED: 已取消
 * - COMPLETED: 已完成
 */
@Data
@TableName("appointment")  // MyBatis-Plus注解：映射到appointment表
public class AppointmentEntity {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预约用户ID */
    @TableField("user_id")
    private Long userId;

    /** 科室ID */
    @TableField("department_id")
    private Long departmentId;

    /** 医生ID */
    @TableField("doctor_id")
    private Long doctorId;

    /** 预约时间 */
    @TableField("appointment_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appointmentTime;

    /** 主诉/症状描述 */
    private String complaint;

    /** 预约状态：PENDING（待确认）、CONFIRMED（已确认）、CANCELLED（已取消）、COMPLETED（已完成） */
    private String status;

    /** 创建时间（时间戳，毫秒） */
    @TableField("created_at")
    private Long createdAt;

    /** 更新时间（时间戳，毫秒） */
    @TableField("updated_at")
    private Long updatedAt;
}
