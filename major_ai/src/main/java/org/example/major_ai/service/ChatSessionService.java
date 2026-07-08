package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ChatSession;
import org.example.major_ai.entity.ChatSessionEntity;
import org.example.major_ai.mapper.ChatSessionMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 聊天会话服务
 * 负责会话的CRUD操作（创建、查询、更新、删除）
 *
 * 会话ID生成规则：
 * - 自动生成：UUID（去掉横线）
 * - 指定ID：由ChatWebSocketHandler传入（与Redis中的memoryId对应）
 *
 * 调用方：
 * - ChatWebSocketHandler：创建会话、更新标题
 * - ChatSessionController：会话CRUD接口
 */
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    /** 会话Mapper，负责chat_session表的CRUD */
    private final ChatSessionMapper chatSessionMapper;

    /**
     * 创建新会话（自动生成会话ID）
     *
     * @param title 会话标题（可选，默认为"新对话"）
     * @return 创建成功的会话
     */
    public ChatSession createSession(String title) {
        // 生成UUID作为会话ID（去掉横线，如：550e8400e29b41d4a716446655440000）
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        return createSessionWithId(sessionId, title);
    }

    /**
     * 创建新会话（指定会话ID）
     * 用于ChatWebSocketHandler，确保会话ID与Redis中的memoryId一致
     *
     * @param sessionId 会话ID（由前端传入或自动生成）
     * @param title 会话标题（可选，默认为"新对话"）
     * @return 创建成功的会话
     */
    public ChatSession createSessionWithId(String sessionId, String title) {
        long now = System.currentTimeMillis();

        ChatSessionEntity entity = new ChatSessionEntity();
        entity.setId(sessionId);
        entity.setTitle(title != null ? title : "新对话");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // SQL: INSERT INTO chat_session(id, title, created_at, updated_at)
        //      VALUES(?, ?, ?, ?)
        chatSessionMapper.insert(entity);

        // 将Entity转换为DTO返回
        return entity.toChatSession();
    }

    /**
     * 获取所有会话列表
     *
     * @return 会话列表，按更新时间降序排列（最近活跃的在前面）
     */
    public List<ChatSession> getAllSessions() {
        // 构建查询条件：按更新时间降序
        LambdaQueryWrapper<ChatSessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ChatSessionEntity::getUpdatedAt);

        // SQL: SELECT * FROM chat_session ORDER BY updated_at DESC

        // 将Entity列表转换为DTO列表
        return chatSessionMapper.selectList(wrapper)
                .stream()
                .map(ChatSessionEntity::toChatSession)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定会话
     *
     * @param sessionId 会话ID
     * @return 会话信息，不存在则返回null
     */
    public ChatSession getSession(String sessionId) {
        // SQL: SELECT * FROM chat_session WHERE id = ?
        ChatSessionEntity entity = chatSessionMapper.selectById(sessionId);
        return entity != null ? entity.toChatSession() : null;
    }

    /**
     * 更新会话标题
     *
     * @param sessionId 会话ID
     * @param title 新标题
     * @return 更新后的会话
     */
    public ChatSession updateSessionTitle(String sessionId, String title) {
        // 构建更新条件
        LambdaUpdateWrapper<ChatSessionEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatSessionEntity::getId, sessionId)
                .set(ChatSessionEntity::getTitle, title)
                .set(ChatSessionEntity::getUpdatedAt, System.currentTimeMillis());

        // SQL: UPDATE chat_session
        //      SET title = ?, updated_at = ?
        //      WHERE id = ?
        chatSessionMapper.update(null, wrapper);

        // 返回更新后的会话
        return getSession(sessionId);
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     */
    public void deleteSession(String sessionId) {
        // SQL: DELETE FROM chat_session WHERE id = ?
        chatSessionMapper.deleteById(sessionId);
        // 注意：消息需要单独删除（在ChatMessageService中处理）
    }
}
