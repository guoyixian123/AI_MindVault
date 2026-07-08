package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ChatMessageEntity;
import org.example.major_ai.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天消息服务
 * 负责聊天消息的持久化存储（MySQL）
 *
 * 与Redis聊天记忆的区别：
 * - Redis：存储最近20条消息，用于AI上下文窗口，7天过期
 * - MySQL：永久存储所有消息，用于历史记录查询
 *
 * 调用方：
 * - ChatWebSocketHandler：保存用户消息和AI回复
 * - ChatSessionController：查询会话消息、删除会话消息
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    /** 消息Mapper，负责chat_message表的CRUD */
    private final ChatMessageMapper chatMessageMapper;

    /**
     * 保存消息到数据库
     *
     * @param sessionId 会话ID（与Redis中的memoryId对应）
     * @param role 消息角色：user（用户）或 assistant（AI）
     * @param content 消息内容
     */
    public void saveMessage(String sessionId, String role, String content) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSessionId(sessionId);
        entity.setRole(role);
        entity.setContent(content);
        entity.setCreatedAt(System.currentTimeMillis());

        // SQL: INSERT INTO chat_message(session_id, role, content, created_at)
        //      VALUES(?, ?, ?, ?)
        chatMessageMapper.insert(entity);
    }

    /**
     * 获取指定会话的所有消息
     *
     * @param sessionId 会话ID
     * @return 消息列表，按创建时间升序排列
     */
    public List<ChatMessageEntity> getSessionMessages(String sessionId) {
        // 构建查询条件：session_id = sessionId，按创建时间升序
        LambdaQueryWrapper<ChatMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByAsc(ChatMessageEntity::getCreatedAt);

        // SQL: SELECT * FROM chat_message
        //      WHERE session_id = ?
        //      ORDER BY created_at ASC
        return chatMessageMapper.selectList(wrapper);
    }

    /**
     * 删除指定会话的所有消息
     *
     * @param sessionId 会话ID
     */
    public void deleteSessionMessages(String sessionId) {
        // 构建删除条件：session_id = sessionId
        LambdaQueryWrapper<ChatMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageEntity::getSessionId, sessionId);

        // SQL: DELETE FROM chat_message WHERE session_id = ?
        chatMessageMapper.delete(wrapper);
    }
}
