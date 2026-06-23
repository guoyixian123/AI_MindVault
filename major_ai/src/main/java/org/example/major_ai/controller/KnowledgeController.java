package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.service.KnowledgeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/upload")
    public ApiResponse<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String fileId = knowledgeService.uploadDocument(file);
            return ApiResponse.success("文档上传成功", fileId);
        } catch (IOException e) {
            return ApiResponse.error("文档上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<List<KnowledgeService.DocumentInfo>> listDocuments() {
        List<KnowledgeService.DocumentInfo> documents = knowledgeService.listDocuments();
        return ApiResponse.success(documents);
    }

    @DeleteMapping("/{filename}")
    public ApiResponse<Void> deleteDocument(@PathVariable String filename) {
        try {
            knowledgeService.deleteDocument(filename);
            return ApiResponse.success("文档删除成功", null);
        } catch (IOException e) {
            return ApiResponse.error("文档删除失败: " + e.getMessage());
        }
    }

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
