package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("health_record")
public class HealthRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("record_date")
    private LocalDate recordDate;

    @TableField("blood_pressure_sys")
    private Integer bloodPressureSys;

    @TableField("blood_pressure_dia")
    private Integer bloodPressureDia;

    @TableField("blood_sugar")
    private BigDecimal bloodSugar;

    private Integer steps;

    @TableField("sleep_hours")
    private BigDecimal sleepHours;

    @TableField("body_temperature")
    private BigDecimal bodyTemperature;

    @TableField("menstrual_note")
    private String menstrualNote;

    @TableField("created_at")
    private Long createdAt;
}
