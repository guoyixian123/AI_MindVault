package org.example.major_ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ChatSession;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String USER_SESSIONS_KEY_PREFIX = "user:sessions:";

    public ChatSession createSession(String userId, String title) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新对话");
        session.setCreatedAt(System.currentTimeMillis());
        session.setUpdatedAt(System.currentTimeMillis());

        try {
            // 保存会话
            String sessionJson = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, sessionJson, 30, TimeUnit.DAYS);

            // 添加到用户的会话列表
            String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;
            redisTemplate.opsForList().rightPush(userSessionsKey, sessionId);
            redisTemplate.expire(userSessionsKey, 30, TimeUnit.DAYS);

            return session;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("创建会话失败", e);
        }
    }

    public List<ChatSession> getUserSessions(String userId) {
        String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;
        List<String> sessionIds = redisTemplate.opsForList().range(userSessionsKey, 0, -1);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ChatSession> sessions = new ArrayList<>();
        for (String sessionId : sessionIds) {
            ChatSession session = getSession(sessionId);
            if (session != null) {
                sessions.add(session);
            }
        }

        // 按更新时间倒序排列
        return sessions.stream()
                .sorted((a, b) -> Long.compare(b.getUpdatedAt(), a.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public ChatSession getSession(String sessionId) {
        String sessionJson = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
        if (sessionJson == null) {
            return null;
        }
        try {
            return objectMapper.readValue(sessionJson, ChatSession.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public ChatSession updateSessionTitle(String sessionId, String title) {
        ChatSession session = getSession(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        session.setTitle(title);
        session.setUpdatedAt(System.currentTimeMillis());

        try {
            String sessionJson = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(SESSION_KEY_PREFIX + sessionId, sessionJson, 30, TimeUnit.DAYS);
            return session;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("更新会话失败", e);
        }
    }

    public void deleteSession(String userId, String sessionId) {
        // 从用户的会话列表中移除
        String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;
        redisTemplate.opsForList().remove(userSessionsKey, 1, sessionId);

        // 删除会话数据
        redisTemplate.delete(SESSION_KEY_PREFIX + sessionId);

        // 删除会话的聊天记忆
        redisTemplate.delete(sessionId);
    }
}
