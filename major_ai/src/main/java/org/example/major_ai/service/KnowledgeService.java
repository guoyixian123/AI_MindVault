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

/**
 * 知识库服务
 * 负责知识库文档的上传、删除、列表和向量化
 *
 * 功能：
 * 1. 上传文档（txt/md/pdf格式）
 * 2. 删除文档
 * 3. 查询文档列表
 * 4. 重建向量索引
 *
 * 存储结构：
 * - 文件存储：本地文件系统（knowledge-uploads目录）
 * - 向量存储：Redis（embedding:{id}）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    /** Redis向量存储 */
    private final RedisEmbeddingStore redisEmbeddingStore;

    /** 嵌入模型（text-embedding-v4） */
    private final EmbeddingModel embeddingModel;

    /** 上传目录（默认：./knowledge-uploads） */
    @Value("${knowledge.upload-dir:./knowledge-uploads}")
    private String uploadDir;

    /** 分块大小（默认：500字符） */
    @Value("${knowledge.chunk-size:500}")
    private int chunkSize;

    /** 分块重叠（默认：100字符） */
    @Value("${knowledge.chunk-overlap:100}")
    private int chunkOverlap;

    /**
     * 上传文档
     * 1. 验证文件类型
     * 2. 保存到本地文件系统
     * 3. 解析并向量化到Redis
     *
     * @param file 上传的文件
     * @return 文件ID
     * @throws IOException 文件操作异常
     */
    public String uploadDocument(MultipartFile file) throws IOException {
        // 1. 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isSupportedFileType(originalFilename)) {
            throw new RuntimeException("不支持的文件类型，仅支持 txt、md、pdf 格式");
        }

        // 2. 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 3. 保存文件（使用UUID作为文件名，避免冲突）
        String fileId = UUID.randomUUID().toString().replace("-", "");
        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = fileId + fileExtension;
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 4. 解析并向量化文档
        try {
            ingestDocument(filePath, originalFilename);
            log.info("文档 {} 已成功向量化", originalFilename);
        } catch (Exception e) {
            log.error("文档向量化失败: {}", e.getMessage());
            // 即使向量化失败也保留文件，可以稍后重试
        }

        return fileId;
    }

    /**
     * 解析并摄入文档到向量库
     *
     * @param filePath 文件路径
     * @param originalFilename 原始文件名
     * @throws IOException 文件读取异常
     */
    private void ingestDocument(Path filePath, String originalFilename) throws IOException {
        // 1. 解析文档
        DocumentParser parser = new TextDocumentParser();
        Document document;
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            document = parser.parse(inputStream);
        }

        // 2. 添加元数据
        document.metadata().put("filename", originalFilename);
        document.metadata().put("uploadTime", String.valueOf(System.currentTimeMillis()));

        // 3. 创建分块器
        DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);

        // 4. 创建摄入器并执行
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)      // 向量化模型
                .documentSplitter(splitter)           // 分块器
                .embeddingStore(redisEmbeddingStore)  // 存储目标
                .build();

        ingestor.ingest(document);  // 分块 → 向量化 → 存储
    }

    /**
     * 查询文档列表
     *
     * @return 文档信息列表
     */
    public List<DocumentInfo> listDocuments() {
        List<DocumentInfo> documents = new ArrayList<>();
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                return documents;
            }

            try (Stream<Path> files = Files.list(uploadPath)) {
                documents = files
                        .filter(Files::isRegularFile)  // 只要文件，不要目录
                        .map(this::toDocumentInfo)      // 转换为DocumentInfo
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error("列出文档失败: {}", e.getMessage());
        }
        return documents;
    }

    /**
     * 删除文档
     *
     * @param filename 文件名
     * @throws IOException 文件删除异常
     */
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

    /**
     * 重建向量索引
     * 重新加载所有文档，分块、向量化并存储到Redis
     *
     * @throws IOException 文件操作异常
     */
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

    /**
     * 将文件路径转换为DocumentInfo
     *
     * @param path 文件路径
     * @return DocumentInfo
     */
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

    /**
     * 检查文件类型是否支持
     *
     * @param filename 文件名
     * @return 支持返回true
     */
    private boolean isSupportedFileType(String filename) {
        String ext = getFileExtension(filename).toLowerCase();
        return ext.equals(".txt") || ext.equals(".md") || ext.equals(".pdf");
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（包含"."，如".txt"）
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }

    /**
     * 文档信息内部类
     */
    public static class DocumentInfo {
        /** 文件名 */
        private String filename;

        /** 文件大小（字节） */
        private long size;

        /** 最后修改时间（时间戳，毫秒） */
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
