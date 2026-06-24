package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("chronic_record")
public class ChronicRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("chronic_disease_id")
    private Long chronicDiseaseId;

    @TableField("record_date")
    private LocalDate recordDate;

    @TableField("indicator_name")
    private String indicatorName;

    @TableField("indicator_value")
    private String indicatorValue;

    private String notes;

    @TableField("created_at")
    private Long createdAt;
}
