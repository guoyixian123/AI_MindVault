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

@Component
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;

    private static final String CHAT_MEMORY_KEY_PREFIX = "chat:memory:";
    private static final Duration MEMORY_TTL = Duration.ofDays(7);

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = CHAT_MEMORY_KEY_PREFIX + memoryId;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return List.of();
        }
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        String key = CHAT_MEMORY_KEY_PREFIX + memoryId;
        String json = ChatMessageSerializer.messagesToJson(list);
        redisTemplate.opsForValue().set(key, json, MEMORY_TTL);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = CHAT_MEMORY_KEY_PREFIX + memoryId;
        redisTemplate.delete(key);
    }
}
