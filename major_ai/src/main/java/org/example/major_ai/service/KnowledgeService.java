package org.example.major_ai.service;

import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final RedisEmbeddingStore redisEmbeddingStore;
    private final EmbeddingModel embeddingModel;

    @Value("${knowledge.upload-dir:./knowledge-uploads}")
    private String uploadDir;

    @Value("${knowledge.chunk-size:500}")
    private int chunkSize;

    @Value("${knowledge.chunk-overlap:100}")
    private int chunkOverlap;

    public String uploadDocument(MultipartFile file) throws IOException {
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isSupportedFileType(originalFilename)) {
            throw new RuntimeException("不支持的文件类型，仅支持 txt、md、pdf 格式");
        }

        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        String fileId = UUID.randomUUID().toString().replace("-", "");
        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = fileId + fileExtension;
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 解析并向量化文档
        try {
            ingestDocument(filePath, originalFilename);
            log.info("文档 {} 已成功向量化", originalFilename);
        } catch (Exception e) {
            log.error("文档向量化失败: {}", e.getMessage());
            // 即使向量化失败也保留文件，可以稍后重试
        }

        return fileId;
    }

    private void ingestDocument(Path filePath, String originalFilename) throws IOException {
        DocumentParser parser = new TextDocumentParser();
        Document document;
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            document = parser.parse(inputStream);
        }

        // 添加元数据
        document.metadata().put("filename", originalFilename);
        document.metadata().put("uploadTime", String.valueOf(System.currentTimeMillis()));

        DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .documentSplitter(splitter)
                .embeddingStore(redisEmbeddingStore)
                .build();

        ingestor.ingest(document);
    }

    public List<DocumentInfo> listDocuments() {
        List<DocumentInfo> documents = new ArrayList<>();
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                return documents;
            }

            try (Stream<Path> files = Files.list(uploadPath)) {
                documents = files
                        .filter(Files::isRegularFile)
                        .map(this::toDocumentInfo)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error("列出文档失败: {}", e.getMessage());
        }
        return documents;
    }

    public void deleteDocument(String filename) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        Path filePath = uploadPath.resolve(filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("文档 {} 已删除", filename);
            // 注意：从 Redis 中删除对应的向量需要知道具体的 segment ID
            // 这里简化处理，仅删除文件
        } else {
            throw new RuntimeException("文档不存在");
        }
    }

    public void rebuildIndex() throws IOException {
        // 清空现有的向量存储
        // 注意：RedisEmbeddingStore 没有直接的清空方法
        // 实际生产中可能需要自定义实现

        // 重新索引所有文档
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            return;
        }

        try (Stream<Path> files = Files.list(uploadPath)) {
            files.filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String filename = file.getFileName().toString();
                            ingestDocument(file, filename);
                            log.info("重新索引文档: {}", filename);
                        } catch (Exception e) {
                            log.error("索引文档失败: {}", e.getMessage());
                        }
                    });
        }
    }

    private DocumentInfo toDocumentInfo(Path path) {
        try {
            DocumentInfo info = new DocumentInfo();
            info.setFilename(path.getFileName().toString());
            info.setSize(Files.size(path));
            info.setLastModified(Files.getLastModifiedTime(path).toMillis());
            return info;
        } catch (IOException e) {
            return new DocumentInfo(path.getFileName().toString(), 0, 0);
        }
    }

    private boolean isSupportedFileType(String filename) {
        String ext = getFileExtension(filename).toLowerCase();
        return ext.equals(".txt") || ext.equals(".md") || ext.equals(".pdf");
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }

    public static class DocumentInfo {
        private String filename;
        private long size;
        private long lastModified;

        public DocumentInfo() {}

        public DocumentInfo(String filename, long size, long lastModified) {
            this.filename = filename;
            this.size = size;
            this.lastModified = lastModified;
        }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public long getLastModified() { return lastModified; }
        public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    }
}
