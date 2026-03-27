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
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommonConfig {
//    @Autowired
//    private ChatMemoryStore chatMemoryStore;
    @Autowired
    public RedisEmbeddingStore redisEmbeddingStore;
    @Autowired
    public ChatMemoryStore redischatMemoryStore;
    @Autowired
    public EmbeddingModel embeddingModel;
    @Autowired
    public OpenAiChatModel model;

    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        ChatMemoryProvider cmp=new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object o) {
                return MessageWindowChatMemory.builder()
                        .chatMemoryStore(redischatMemoryStore)
                        .id(o)
                        .maxMessages(20)
                        .build();
            }
        };
        return cmp;
    }
    //@Bean
    public EmbeddingStore  store(){//配置导入
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("content");
        DocumentSplitter ds= DocumentSplitters.recursive(500,100);

        EmbeddingStoreIngestor ingestor=EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .documentSplitter(ds)
                .embeddingStore(redisEmbeddingStore)
                .build();
        ingestor.ingest(documents);
        return redisEmbeddingStore;
    }
    @Bean
    public ContentRetriever contentRetriever(){
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(redisEmbeddingStore)
                .embeddingModel(embeddingModel)
                .minScore(0.8)
                .maxResults(3)
                .build();
    }
}
