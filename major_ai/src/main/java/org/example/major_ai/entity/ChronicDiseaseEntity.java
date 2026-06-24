package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("chronic_disease")
public class ChronicDiseaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("disease_type")
    private String diseaseType;

    @TableField("diagnosis_date")
    private LocalDate diagnosisDate;

    @TableField("doctor_notes")
    private String doctorNotes;

    @TableField("current_medications")
    private String currentMedications;

    @TableField("created_at")
    private Long createdAt;

    @TableField("updated_at")
    private Long updatedAt;
}
