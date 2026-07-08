package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 聊天消息实体类
 * 对应数据库表：chat_message
 *
 * 表结构：
 * CREATE TABLE chat_message (
 *     id BIGINT AUTO_INCREMENT PRIMARY KEY,
 *     session_id VARCHAR(255) NOT NULL,
 *     role VARCHAR(50) NOT NULL,
 *     content TEXT NOT NULL,
 *     created_at BIGINT NOT NULL,
 *     FOREIGN KEY (session_id) REFERENCES chat_session(id)
 * );
 */
@Data
@TableName("chat_message")  // MyBatis-Plus注解：映射到chat_message表
public class ChatMessageEntity {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID（关联chat_session表） */
    @TableField("session_id")
    private String sessionId;

    /** 消息角色：user（用户）或 assistant（AI） */
    private String role;

    /** 消息内容 */
    private String content;

    /** 创建时间（时间戳，毫秒） */
    @TableField("created_at")
    private Long createdAt;
}
