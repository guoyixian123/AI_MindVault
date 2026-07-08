package org.example.major_ai.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * AI咨询服务接口
 * 使用LangChain4j的@AiService注解，自动实现AI对话功能
 *
 * 核心能力：
 * 1. 调用Qwen大模型进行对话
 * 2. 支持流式返回（打字机效果）
 * 3. 集成聊天记忆（Redis存储，20条窗口）
 * 4. 集成RAG知识库检索（向量相似度搜索）
 *
 * 调用链路：
 * ChatWebSocketHandler → ConsultantService.chat() → LangChain4j → Qwen API
 */
@AiService(
        // 另一种模式是AUTOMATIC（自动装配），但本项目需要明确指定模型
        wiringMode = AiServiceWiringMode.EXPLICIT,
        // 对应application.yml中的langchain4j.community.dashscope.chat-model配置
        // 模型：qwen-plus（阿里云通义千问）
        chatModel = "openAiChatModel",
        // 模型：qwen-plus（阿里云通义千问，流式模式）
        // 用途：流式调用，返回Flux<String>，实现打字机效果
        streamingChatModel = "openAiStreamingChatModel",
        // 聊天记忆提供者的Bean名称
        // 对应CommonConfig.java中的chatMemoryProvider()方法
        // 存储：Redis（Key: chat_memory:{memoryId}）
        // 窗口：最多保留20条消息
        // TTL：7天过期
        chatMemoryProvider = "chatMemoryProvider",
        // RAG内容检索器的Bean名称
        // 对应CommonConfig.java中的contentRetriever()方法
        // 存储：Redis向量库（embedding:{id}）
        // 模型：text-embedding-v4（1536维向量）
        // 检索：相似度>0.7，返回Top3结果
        contentRetriever = "contentRetriever"
)
public interface ConsultantService {

    @SystemMessage(fromResource = "system.txt")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
}
