package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("health_profile")
public class HealthProfileEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private BigDecimal height;

    private BigDecimal weight;

    private BigDecimal bmi;

    @TableField("blood_pressure_sys")
    private Integer bloodPressureSys;

    @TableField("blood_pressure_dia")
    private Integer bloodPressureDia;

    @TableField("blood_sugar")
    private BigDecimal bloodSugar;

    @TableField("blood_lipid")
    private String bloodLipid;

    private String allergies;

    @TableField("family_history")
    private String familyHistory;

    @TableField("surgery_history")
    private String surgeryHistory;

    @TableField("major_diseases")
    private String majorDiseases;

    @TableField("created_at")
    private Long createdAt;

    @TableField("updated_at")
    private Long updatedAt;
}
