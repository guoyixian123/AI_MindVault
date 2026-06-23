package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ChatMessageEntity;
import org.example.major_ai.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    public void saveMessage(String sessionId, String role, String content) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSessionId(sessionId);
        entity.setRole(role);
        entity.setContent(content);
        entity.setCreatedAt(System.currentTimeMillis());

        chatMessageMapper.insert(entity);
    }

    public List<ChatMessageEntity> getSessionMessages(String sessionId) {
        LambdaQueryWrapper<ChatMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageEntity::getSessionId, sessionId)
                .orderByAsc(ChatMessageEntity::getCreatedAt);

        return chatMessageMapper.selectList(wrapper);
    }

    public void deleteSessionMessages(String sessionId) {
        LambdaQueryWrapper<ChatMessageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessageEntity::getSessionId, sessionId);

        chatMessageMapper.delete(wrapper);
    }
}
