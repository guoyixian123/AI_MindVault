package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.service.KnowledgeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 知识库管理控制器
 * 提供RAG知识库文档的管理接口（上传、列表、删除、重建索引）
 *
 * API前缀：/api/knowledge
 *
 * 功能：
 * - 上传知识库文档（txt文件）
 * - 查看文档列表
 * - 删除文档
 * - 重建向量索引
 *
 * 注意：文档上传后需要手动触发"重建索引"才能生效
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    /** 知识库服务，负责文档的上传、删除和索引重建 */
    private final KnowledgeService knowledgeService;

    /**
     * 上传知识库文档
     *
     * 请求：POST /api/knowledge/upload
     * 请求体：multipart/form-data，包含文件
     * 响应：{"code": 200, "message": "文档上传成功", "data": "fileId"}
     *
     * @param file 上传的文件（支持txt格式）
     * @return 文件ID，上传失败返回错误信息
     */
    @PostMapping("/upload")
    public ApiResponse<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String fileId = knowledgeService.uploadDocument(file);
            return ApiResponse.success("文档上传成功", fileId);
        } catch (IOException e) {
            return ApiResponse.error("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取知识库文档列表
     *
     * 请求：GET /api/knowledge/list
     * 响应：{"code": 200, "data": [{"filename": "...", "size": 1234, ...}]}
     *
     * @return 文档信息列表
     */
    @GetMapping("/list")
    public ApiResponse<List<KnowledgeService.DocumentInfo>> listDocuments() {
        List<KnowledgeService.DocumentInfo> documents = knowledgeService.listDocuments();
        return ApiResponse.success(documents);
    }

    /**
     * 删除知识库文档
     *
     * 请求：DELETE /api/knowledge/{filename}
     * 响应：{"code": 200, "message": "文档删除成功"}
     *
     * 注意：删除后需要手动触发"重建索引"才能从向量库中移除
     *
     * @param filename 文件名
     * @return 删除成功响应，失败返回错误信息
     */
    @DeleteMapping("/{filename}")
    public ApiResponse<Void> deleteDocument(@PathVariable String filename) {
        try {
            knowledgeService.deleteDocument(filename);
            return ApiResponse.success("文档删除成功", null);
        } catch (IOException e) {
            return ApiResponse.error("文档删除失败: " + e.getMessage());
        }
    }

    /**
     * 重建向量索引
     * 重新加载所有知识库文档，分块、向量化并存储到Redis
     *
     * 请求：POST /api/knowledge/rebuild
     * 响应：{"code": 200, "message": "索引重建成功"}
     *
     * 注意：这个操作可能需要几秒到几十秒，取决于文档数量
     *
     * @return 重建成功响应，失败返回错误信息
     */
    @PostMapping("/rebuild")
    public ApiResponse<Void> rebuildIndex() {
        try {
            knowledgeService.rebuildIndex();
            return ApiResponse.success("索引重建成功", null);
        } catch (IOException e) {
            return ApiResponse.error("索引重建失败: " + e.getMessage());
        }
    }
}
