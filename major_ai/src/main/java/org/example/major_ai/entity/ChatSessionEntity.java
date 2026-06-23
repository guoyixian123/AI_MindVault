package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("chat_session")
public class ChatSessionEntity {

    @TableId(type = IdType.INPUT)
    private String id;

    private String title;

    @TableField("created_at")
    private Long createdAt;

    @TableField("updated_at")
    private Long updatedAt;

    public ChatSession toChatSession() {
        ChatSession session = new ChatSession();
        session.setId(this.id);
        session.setTitle(this.title);
        session.setCreatedAt(this.createdAt);
        session.setUpdatedAt(this.updatedAt);
        return session;
    }

    public static ChatSessionEntity fromChatSession(ChatSession session) {
        ChatSessionEntity entity = new ChatSessionEntity();
        entity.setId(session.getId());
        entity.setTitle(session.getTitle());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setUpdatedAt(session.getUpdatedAt());
        return entity;
    }
}
