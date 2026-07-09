package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("consultation_post")
public class ConsultationPostEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("department_id")
    private Long departmentId;

    private String title;

    private String content;

    private String images; // JSON array of image URLs

    private String status; // PENDING, REPLIED, CLOSED

    @TableField("created_at")
    private Long createdAt;

    @TableField("updated_at")
    private Long updatedAt;

    /** 发帖人昵称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String userName;
}
