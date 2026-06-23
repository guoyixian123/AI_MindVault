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

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;

    public ChatSession createSession(String title) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        long now = System.currentTimeMillis();

        ChatSessionEntity entity = new ChatSessionEntity();
        entity.setId(sessionId);
        entity.setTitle(title != null ? title : "新对话");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        chatSessionMapper.insert(entity);
        return entity.toChatSession();
    }

    public List<ChatSession> getAllSessions() {
        LambdaQueryWrapper<ChatSessionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ChatSessionEntity::getUpdatedAt);

        return chatSessionMapper.selectList(wrapper)
                .stream()
                .map(ChatSessionEntity::toChatSession)
                .collect(Collectors.toList());
    }

    public ChatSession getSession(String sessionId) {
        ChatSessionEntity entity = chatSessionMapper.selectById(sessionId);
        return entity != null ? entity.toChatSession() : null;
    }

    public ChatSession updateSessionTitle(String sessionId, String title) {
        LambdaUpdateWrapper<ChatSessionEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatSessionEntity::getId, sessionId)
                .set(ChatSessionEntity::getTitle, title)
                .set(ChatSessionEntity::getUpdatedAt, System.currentTimeMillis());

        chatSessionMapper.update(null, wrapper);
        return getSession(sessionId);
    }

    public void deleteSession(String sessionId) {
        chatSessionMapper.deleteById(sessionId);
    }
}
