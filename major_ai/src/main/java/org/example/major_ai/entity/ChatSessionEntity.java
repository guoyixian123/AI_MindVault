package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 聊天会话实体类
 * 对应数据库表：chat_session
 *
 * 表结构：
 * CREATE TABLE chat_session (
 *     id VARCHAR(255) PRIMARY KEY,
 *     title VARCHAR(255) NOT NULL,
 *     created_at BIGINT NOT NULL,
 *     updated_at BIGINT NOT NULL
 * );
 *
 * ID生成规则：
 * - 自动生成：UUID（去掉横线）
 * - 指定ID：由前端传入（与Redis中的memoryId对应）
 */
@Data
@TableName("chat_session")  // MyBatis-Plus注解：映射到chat_session表
public class ChatSessionEntity {

    /** 会话ID（手动输入，非自增） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 会话标题 */
    private String title;

    /** 创建时间（时间戳，毫秒） */
    @TableField("created_at")
    private Long createdAt;

    /** 更新时间（时间戳，毫秒） */
    @TableField("updated_at")
    private Long updatedAt;

    /**
     * 将Entity转换为DTO
     *
     * @return ChatSession DTO对象
     */
    public ChatSession toChatSession() {
        ChatSession session = new ChatSession();
        session.setId(this.id);
        session.setTitle(this.title);
        session.setCreatedAt(this.createdAt);
        session.setUpdatedAt(this.updatedAt);
        return session;
    }

    /**
     * 将DTO转换为Entity
     *
     * @param session DTO对象
     * @return ChatSessionEntity 实体对象
     */
    public static ChatSessionEntity fromChatSession(ChatSession session) {
        ChatSessionEntity entity = new ChatSessionEntity();
        entity.setId(session.getId());
        entity.setTitle(session.getTitle());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setUpdatedAt(session.getUpdatedAt());
        return entity;
    }
}
