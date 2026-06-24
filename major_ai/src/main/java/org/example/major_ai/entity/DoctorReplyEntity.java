package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("doctor_reply")
public class DoctorReplyEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("post_id")
    private Long postId;

    @TableField("doctor_id")
    private Long doctorId;

    private String content;

    @TableField("created_at")
    private Long createdAt;

    @TableField(exist = false)
    private String doctorName;
}
