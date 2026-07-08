package org.example.major_ai.config;

import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 公共配置类
 * 配置LangChain4j的核心组件：
 * 1. 聊天记忆（ChatMemory）- Redis存储，20条消息窗口
 * 2. RAG知识库（EmbeddingStore）- 文档向量化并存储到Redis
 * 3. 内容检索器（ContentRetriever）- 向量相似度搜索
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommonConfig {

    /** Redis向量存储，用于存储文档的向量嵌入 */
    private final RedisEmbeddingStore redisEmbeddingStore;

    /** Redis聊天记忆存储，用于存储聊天历史 */
    private final ChatMemoryStore redischatMemoryStore;

    /** 嵌入模型，用于将文本转换为向量（text-embedding-v4，1536维） */
    private final EmbeddingModel embeddingModel;

    // ========== RAG知识库配置参数 ==========

    /** 文档分块大小：每块最多500个字符 */
    @Value("${knowledge.chunk-size:500}")
    private int chunkSize;

    /** 文档分块重叠：块间重叠100个字符，保证上下文连贯 */
    @Value("${knowledge.chunk-overlap:100}")
    private int chunkOverlap;

    /** RAG检索最低相似度阈值：只有相似度>0.7的结果才会返回 */
    @Value("${rag.min-score:0.7}")
    private double minScore;

    /** RAG检索最大返回结果数：最多返回3条最相关的知识片段 */
    @Value("${rag.max-results:3}")
    private int maxResults;

    /** 启动时是否自动加载知识库：默认false，避免启动慢 */
    @Value("${knowledge.auto-load-on-startup:false}")
    private boolean autoLoadOnStartup;

    /**
     * 配置聊天记忆提供者
     * 每个会话独立维护聊天历史，支持多用户并发
     *
     * @return ChatMemoryProvider 记忆提供者
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        // 使用Lambda表达式创建记忆提供者
        // memoryId参数由ConsultantService的@MemoryId注解传入
        return memoryId -> MessageWindowChatMemory.builder()
                .chatMemoryStore(redischatMemoryStore)  // 使用Redis存储
                .id(memoryId)                            // 会话ID，用于隔离不同用户的记忆
                .maxMessages(20)                         // 最多保留20条消息（10轮对话）
                .build();
        // Redis Key格式: chat_memory:{memoryId}
        // TTL: 7天（在Redis配置中设置）
    }

    /**
     * 配置RAG知识库（向量存储）
     * 负责将知识库文档加载、分块、向量化并存储到Redis
     *
     * 工作流程：
     * 1. 加载classpath下的content目录中的所有文档
     * 2. 将文档分块（每块500字，重叠100字）
     * 3. 使用text-embedding-v4模型将文本转换为1536维向量
     * 4. 将向量和原始文本存储到Redis
     *
     * @return EmbeddingStore 向量存储实例
     */
    @Bean
    public EmbeddingStore store() {
        // 检查是否启用启动时自动加载
        // 默认false，避免每次启动都重新向量化（耗时）
        if (!autoLoadOnStartup) {
            log.info("跳过启动时自动加载知识库（设置 knowledge.auto-load-on-startup=true 启用）");
            return redisEmbeddingStore;
        }

        log.info("开始加载知识库文档到向量数据库...");

        // 1. 加载classpath下的content目录中的所有文档
        // 文件：disease.txt, drug_interaction.txt, medicine_guide.txt等
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("content");

        if (documents.isEmpty()) {
            log.info("未找到知识库文档，跳过加载");
            return redisEmbeddingStore;
        }

        // 2. 创建文档分块器
        // recursive: 递归分割（优先按段落→句子→词→字符）
        // chunkSize=500: 每块最多500个字符
        // chunkOverlap=100: 块间重叠100个字符，保证上下文连贯
        DocumentSplitter ds = DocumentSplitters.recursive(chunkSize, chunkOverlap);

        // 3. 创建文档摄入器
        // 负责整个流程：分块 → 向量化 → 存储
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)      // 向量化模型（text-embedding-v4，1536维）
                .documentSplitter(ds)                // 分块器
                .embeddingStore(redisEmbeddingStore) // 存储目标（Redis）
                .build();

        // 4. 执行摄入：分块 → 向量化 → 存储到Redis
        // 这一步会调用DashScope API进行向量化，可能需要几秒到几十秒
        ingestor.ingest(documents);

        log.info("知识库文档加载完成，共 {} 个文档", documents.size());
        return redisEmbeddingStore;
    }

    /**
     * 配置RAG内容检索器
     * 用于在用户提问时，从知识库中检索最相关的知识片段
     *
     * 工作流程：
     * 1. 将用户问题向量化（text-embedding-v4）
     * 2. 在Redis中搜索最相似的向量（余弦相似度）
     * 3. 返回相似度>0.7的Top3结果
     * 4. 将结果注入到AI的上下文中
     *
     * @return ContentRetriever 内容检索器
     */
    @Bean
    public ContentRetriever contentRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(redisEmbeddingStore)  // Redis向量存储
                .embeddingModel(embeddingModel)        // 向量化模型
                .minScore(minScore)                    // 最低相似度：0.7
                .maxResults(maxResults)                // 最大返回数：3
                .build();
        // 检索示例：
        // 用户问题: "阿莫西林的副作用是什么？"
        // 向量化: [0.2345, -0.6789, ...]
        // Redis搜索: 找到相似度>0.7的Top3向量
        // 返回: [
        //   "阿莫西林常见副作用包括：腹泻、恶心、皮疹..."（相似度0.92）,
        //   "青霉素过敏者禁用阿莫西林..."（相似度0.85）,
        //   "阿莫西林与丙磺舒合用会增加血药浓度..."（相似度0.78）
        // ]
    }
}
