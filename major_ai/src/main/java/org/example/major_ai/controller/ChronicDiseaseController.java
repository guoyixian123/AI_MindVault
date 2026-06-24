package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.ChronicDiseaseEntity;
import org.example.major_ai.entity.ChronicRecordEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.ChronicDiseaseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health/chronic")
@RequiredArgsConstructor
public class ChronicDiseaseController {

    private final ChronicDiseaseService chronicDiseaseService;

    @PostMapping
    public ApiResponse<ChronicDiseaseEntity> create(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody ChronicDiseaseEntity disease) {
        disease.setUserId(user.getId());
        ChronicDiseaseEntity created = chronicDiseaseService.create(disease);
        return ApiResponse.success("慢病档案创建成功", created);
    }

    @GetMapping
    public ApiResponse<List<ChronicDiseaseEntity>> list(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<ChronicDiseaseEntity> list = chronicDiseaseService.listByUserId(user.getId());
        return ApiResponse.success(list);
    }

    @GetMapping("/{id}")
    public ApiResponse<ChronicDiseaseEntity> getById(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        ChronicDiseaseEntity disease = chronicDiseaseService.findById(id);
        if (disease == null || !disease.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "慢病档案不存在");
        }
        return ApiResponse.success(disease);
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id,
            @RequestBody ChronicDiseaseEntity disease) {
        ChronicDiseaseEntity existing = chronicDiseaseService.findById(id);
        if (existing == null || !existing.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "慢病档案不存在");
        }
        disease.setId(id);
        chronicDiseaseService.update(disease);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        ChronicDiseaseEntity existing = chronicDiseaseService.findById(id);
        if (existing == null || !existing.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "慢病档案不存在");
        }
        chronicDiseaseService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    // 慢病指标记录
    @PostMapping("/{id}/records")
    public ApiResponse<ChronicRecordEntity> addRecord(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id,
            @RequestBody ChronicRecordEntity record) {
        ChronicDiseaseEntity disease = chronicDiseaseService.findById(id);
        if (disease == null || !disease.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "慢病档案不存在");
        }
        record.setChronicDiseaseId(id);
        ChronicRecordEntity saved = chronicDiseaseService.addRecord(record);
        return ApiResponse.success("指标记录添加成功", saved);
    }

    @GetMapping("/{id}/records")
    public ApiResponse<List<ChronicRecordEntity>> listRecords(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        ChronicDiseaseEntity disease = chronicDiseaseService.findById(id);
        if (disease == null || !disease.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "慢病档案不存在");
        }
        List<ChronicRecordEntity> records = chronicDiseaseService.listRecords(id);
        return ApiResponse.success(records);
    }
}
