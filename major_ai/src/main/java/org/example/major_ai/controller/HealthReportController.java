package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.HealthReportEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.HealthReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 健康报告控制器
 * 提供体检报告的上传和查询接口
 *
 * API前缀：/api/health/reports
 */
@RestController
@RequestMapping("/api/health/reports")
@RequiredArgsConstructor
public class HealthReportController {

    /** 健康报告服务 */
    private final HealthReportService healthReportService;

    /**
     * 上传体检报告
     */
    @PostMapping("/upload")
    public ApiResponse<HealthReportEntity> uploadReport(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody UploadRequest request) {
        HealthReportEntity report = healthReportService.saveReport(
                user.getId(), request.getReportType(), request.getReportContent());
        return ApiResponse.success("体检报告保存成功", report);
    }

    /**
     * 查询报告列表
     */
    @GetMapping
    public ApiResponse<List<HealthReportEntity>> listReports(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<HealthReportEntity> reports = healthReportService.listByUserId(user.getId());
        return ApiResponse.success(reports);
    }

    /**
     * 上传报告请求体
     */
    @lombok.Data
    public static class UploadRequest {
        private String reportType;
        private String reportContent;
    }
}
