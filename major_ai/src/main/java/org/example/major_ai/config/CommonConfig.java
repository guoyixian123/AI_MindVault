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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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

    // 取消注释以启用 RAG 数据导入
    // @Bean
    public EmbeddingStore store() {
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("content");
        DocumentSplitter ds = DocumentSplitters.recursive(chunkSize, chunkOverlap);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .documentSplitter(ds)
                .embeddingStore(redisEmbeddingStore)
                .build();
        ingestor.ingest(documents);
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
