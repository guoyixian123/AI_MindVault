package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("health_report")
public class HealthReportEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("report_type")
    private String reportType;

    @TableField("report_content")
    private String reportContent;

    @TableField("upload_file_path")
    private String uploadFilePath;

    @TableField("ai_analysis")
    private String aiAnalysis;

    @TableField("created_at")
    private Long createdAt;
}
