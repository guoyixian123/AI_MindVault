package org.example.major_ai.chatMemoryStore;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Redis聊天记忆存储
 * 实现LangChain4j的ChatMemoryStore接口，将聊天历史存储到Redis
 *
 * 存储结构：
 * - Key: chat:memory:{memoryId}
 * - Value: JSON格式的聊天消息列表
 * - TTL: 7天自动过期
 *
 * 用途：
 * - 支持多会话隔离（不同memoryId独立存储）
 * - 支持聊天上下文窗口（最多20条消息）
 * - 自动过期清理（7天TTL）
 */
@Component
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    /** Redis操作模板，用于执行Redis命令 */
    private final StringRedisTemplate redisTemplate;

    /** Redis Key前缀，用于区分聊天记忆和其他数据 */
    private static final String CHAT_MEMORY_KEY_PREFIX = "chat:memory:";

    /** 记忆过期时间：7天 */
    private static final Duration MEMORY_TTL = Duration.ofDays(7);

    /**
     * 获取指定会话的聊天历史
     *
     * @param memoryId 会话ID（由前端传入，如"user-123-session-456"）
     * @return 聊天消息列表，如果不存在则返回空列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // 构建Redis Key: chat:memory:user-123-session-456
        String key = CHAT_MEMORY_KEY_PREFIX + memoryId;

        // 从Redis获取JSON格式的聊天历史
        String json = redisTemplate.opsForValue().get(key);

        // 如果不存在，返回空列表
        if (json == null) {
            return List.of();
        }

        // 将JSON反序列化为ChatMessage列表
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    /**
     * 更新指定会话的聊天历史
     * 会覆盖原有的聊天历史，并重新设置过期时间
     *
     * @param memoryId 会话ID
     * @param list 新的聊天消息列表（最多20条，由ChatMemoryProvider控制）
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        // 构建Redis Key
        String key = CHAT_MEMORY_KEY_PREFIX + memoryId;

        // 将ChatMessage列表序列化为JSON
        String json = ChatMessageSerializer.messagesToJson(list);

        // 存储到Redis，并设置7天过期时间
        redisTemplate.opsForValue().set(key, json, MEMORY_TTL);
    }

    /**
     * 删除指定会话的聊天历史
     *
     * @param memoryId 会话ID
     */
    @Override
    public void deleteMessages(Object memoryId) {
        // 构建Redis Key
        String key = CHAT_MEMORY_KEY_PREFIX + memoryId;

        // 从Redis删除
        redisTemplate.delete(key);
    }
}
