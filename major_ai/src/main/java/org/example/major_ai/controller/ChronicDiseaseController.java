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

/**
 * 慢病管理控制器
 * 提供慢性疾病和指标记录的CRUD接口
 *
 * API前缀：/api/health/chronic
 *
 * 功能：
 * - 创建慢病档案
 * - 查询慢病列表
 * - 更新/删除慢病档案
 * - 添加指标记录
 * - 查询指标记录列表
 */
@RestController
@RequestMapping("/api/health/chronic")
@RequiredArgsConstructor
public class ChronicDiseaseController {

    /** 慢病服务 */
    private final ChronicDiseaseService chronicDiseaseService;

    /**
     * 创建慢病档案
     *
     * 请求：POST /api/health/chronic
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"diseaseName": "高血压", "diagnosisDate": "2023-01-15", ...}
     * 响应：{"code": 200, "message": "慢病档案创建成功", "data": {...}}
     *
     * @param user 当前登录用户
     * @param disease 慢病实体
     * @return 创建成功的慢病档案
     */
    @PostMapping
    public ApiResponse<ChronicDiseaseEntity> create(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody ChronicDiseaseEntity disease) {
        disease.setUserId(user.getId());
        ChronicDiseaseEntity created = chronicDiseaseService.create(disease);
        return ApiResponse.success("慢病档案创建成功", created);
    }

    /**
     * 查询慢病列表
     *
     * 请求：GET /api/health/chronic
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * @param user 当前登录用户
     * @return 慢病列表
     */
    @GetMapping
    public ApiResponse<List<ChronicDiseaseEntity>> list(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<ChronicDiseaseEntity> list = chronicDiseaseService.listByUserId(user.getId());
        return ApiResponse.success(list);
    }

    /**
     * 查询慢病详情
     *
     * 请求：GET /api/health/chronic/{id}
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": {...}}
     *
     * @param user 当前登录用户
     * @param id 慢病ID
     * @return 慢病详情，不存在或无权限返回404
     */
    @GetMapping("/{id}")
    public ApiResponse<ChronicDiseaseEntity> getById(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        ChronicDiseaseEntity disease = chronicDiseaseService.findById(id);

        // 验证权限
        if (disease == null || !disease.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "慢病档案不存在");
        }

        return ApiResponse.success(disease);
    }

    /**
     * 更新慢病档案
     *
     * 请求：PUT /api/health/chronic/{id}
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"diseaseName": "高血压", ...}
     * 响应：{"code": 200, "message": "更新成功"}
     *
     * @param user 当前登录用户
     * @param id 慢病ID
     * @param disease 更新内容
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ApiResponse<String> update(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id,
            @RequestBody ChronicDiseaseEntity disease) {
        ChronicDiseaseEntity existing = chronicDiseaseService.findById(id);

        // 验证权限
        if (existing == null || !existing.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "慢病档案不存在");
        }

        disease.setId(id);
        chronicDiseaseService.update(disease);
        return ApiResponse.success("更新成功", null);
    }

    /**
     * 删除慢病档案
     *
     * 请求：DELETE /api/health/chronic/{id}
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "message": "删除成功"}
     *
     * 注意：会同时删除关联的指标记录
     *
     * @param user 当前登录用户
     * @param id 慢病ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        ChronicDiseaseEntity existing = chronicDiseaseService.findById(id);

        // 验证权限
        if (existing == null || !existing.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "慢病档案不存在");
        }

        chronicDiseaseService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 添加慢病指标记录
     *
     * 请求：POST /api/health/chronic/{id}/records
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"recordDate": "2024-01-15", "indicatorValue": "135/85", ...}
     * 响应：{"code": 200, "message": "指标记录添加成功", "data": {...}}
     *
     * @param user 当前登录用户
     * @param id 慢病ID
     * @param record 指标记录
     * @return 创建成功的记录
     */
    @PostMapping("/{id}/records")
    public ApiResponse<ChronicRecordEntity> addRecord(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id,
            @RequestBody ChronicRecordEntity record) {
        ChronicDiseaseEntity disease = chronicDiseaseService.findById(id);

        // 验证权限
        if (disease == null || !disease.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "慢病档案不存在");
        }

        record.setChronicDiseaseId(id);
        ChronicRecordEntity saved = chronicDiseaseService.addRecord(record);
        return ApiResponse.success("指标记录添加成功", saved);
    }

    /**
     * 查询慢病指标记录列表
     *
     * 请求：GET /api/health/chronic/{id}/records
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * @param user 当前登录用户
     * @param id 慢病ID
     * @return 指标记录列表
     */
    @GetMapping("/{id}/records")
    public ApiResponse<List<ChronicRecordEntity>> listRecords(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        ChronicDiseaseEntity disease = chronicDiseaseService.findById(id);

        // 验证权限
        if (disease == null || !disease.getUserId().equals(user.getId())) {
            return ApiResponse.error(404, "慢病档案不存在");
        }

        List<ChronicRecordEntity> records = chronicDiseaseService.listRecords(id);
        return ApiResponse.success(records);
    }
}
