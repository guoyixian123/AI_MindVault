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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommonConfig {

    private final RedisEmbeddingStore redisEmbeddingStore;
    private final ChatMemoryStore redischatMemoryStore;
    private final EmbeddingModel embeddingModel;

    @Value("${knowledge.chunk-size:500}")
    private int chunkSize;

    @Value("${knowledge.chunk-overlap:100}")
    private int chunkOverlap;

    @Value("${rag.min-score:0.7}")
    private double minScore;

    @Value("${rag.max-results:3}")
    private int maxResults;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .chatMemoryStore(redischatMemoryStore)
                .id(memoryId)
                .maxMessages(20)
                .build();
    }

    @Value("${knowledge.auto-load-on-startup:false}")
    private boolean autoLoadOnStartup;

    // RAG 数据导入 - 可配置是否启动时自动加载
    @Bean
    public EmbeddingStore store() {
        if (!autoLoadOnStartup) {
            log.info("跳过启动时自动加载知识库（设置 knowledge.auto-load-on-startup=true 启用）");
            return redisEmbeddingStore;
        }

        log.info("开始加载知识库文档到向量数据库...");
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("content");
        if (documents.isEmpty()) {
            log.info("未找到知识库文档，跳过加载");
            return redisEmbeddingStore;
        }

        DocumentSplitter ds = DocumentSplitters.recursive(chunkSize, chunkOverlap);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .documentSplitter(ds)
                .embeddingStore(redisEmbeddingStore)
                .build();
        ingestor.ingest(documents);
        log.info("知识库文档加载完成，共 {} 个文档", documents.size());
        return redisEmbeddingStore;
    }

    @Bean
    public ContentRetriever contentRetriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(redisEmbeddingStore)
                .embeddingModel(embeddingModel)
                .minScore(minScore)
                .maxResults(maxResults)
                .build();
    }
}
