package org.example.major_ai.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.aiservice.ConsultantService;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.HealthReportEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.HealthReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 健康报告控制器
 * 提供体检报告的上传、查询和AI分析接口
 *
 * API前缀：/api/health/reports
 *
 * 功能：
 * - 上传体检报告
 * - 查询报告列表
 * - AI分析报告（流式返回）
 * - 上传并立即分析
 */
@RestController
@RequestMapping("/api/health/reports")
@RequiredArgsConstructor
public class HealthReportController {

    /** 健康报告服务 */
    private final HealthReportService healthReportService;

    /** AI咨询服务，用于报告分析 */
    private final ConsultantService consultantService;

    /**
     * 上传体检报告
     *
     * 请求：POST /api/health/reports/upload
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"reportType": "综合体检报告", "reportContent": "血常规：白细胞 5.2..."}
     * 响应：{"code": 200, "message": "体检报告保存成功", "data": {...}}
     *
     * @param user 当前登录用户
     * @param request 上传请求
     * @return 保存成功的报告
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
     *
     * 请求：GET /api/health/reports
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * @param user 当前登录用户
     * @return 报告列表
     */
    @GetMapping
    public ApiResponse<List<HealthReportEntity>> listReports(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<HealthReportEntity> reports = healthReportService.listByUserId(user.getId());
        return ApiResponse.success(reports);
    }

    /**
     * AI分析报告（流式返回）
     *
     * 请求：POST /api/health/reports/analyze/{id}
     * 请求头：Authorization: Bearer {token}
     * 响应：text/event-stream（流式返回AI分析结果）
     *
     * @param user 当前登录用户
     * @param id 报告ID
     * @return 流式分析结果
     */
    @PostMapping("/analyze/{id}")
    public Flux<String> analyzeReport(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        // 查询报告
        HealthReportEntity report = healthReportService.findById(id);

        // 验证权限
        if (report == null || !report.getUserId().equals(user.getId())) {
            return Flux.just("报告不存在或无权限访问");
        }

        // 构建分析prompt
        String prompt = buildAnalysisPrompt(report.getReportType(), report.getReportContent());

        // 调用AI分析（流式返回）
        return consultantService.chat("report-" + user.getId(), prompt);
    }

    /**
     * 上传并立即分析报告（流式返回）
     *
     * 请求：POST /api/health/reports/analyze-latest
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"reportType": "综合体检报告", "reportContent": "血常规：白细胞 5.2..."}
     * 响应：text/event-stream（流式返回AI分析结果）
     *
     * 特性：分析完成后自动将结果保存到数据库
     *
     * @param user 当前登录用户
     * @param request 分析请求
     * @return 流式分析结果
     */
    @PostMapping("/analyze-latest")
    public Flux<String> analyzeLatest(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody AnalyzeRequest request) {
        // 先保存报告
        HealthReportEntity report = healthReportService.saveReport(
                user.getId(), request.getReportType(), request.getReportContent());

        // 构建分析prompt
        String prompt = buildAnalysisPrompt(request.getReportType(), request.getReportContent());
        Long reportId = report.getId();
        AtomicReference<StringBuilder> analysisRef = new AtomicReference<>(new StringBuilder());

        // 调用AI分析（流式返回）
        return consultantService.chat("report-" + user.getId(), prompt)
                .doOnNext(chunk -> analysisRef.get().append(chunk))  // 拼接分析结果
                .doOnComplete(() -> {
                    // 分析完成后，将结果保存到数据库
                    String analysis = analysisRef.get().toString();
                    if (!analysis.isEmpty()) {
                        healthReportService.updateAnalysis(reportId, analysis);
                    }
                });
    }

    /**
     * 构建体检报告分析prompt
     *
     * @param reportType 报告类型
     * @param content 报告内容
     * @return 分析prompt
     */
    private String buildAnalysisPrompt(String reportType, String content) {
        return "你是一位专业的体检报告解读顾问。请用通俗易懂的语言解读以下体检报告，要求：\n" +
                "1. 逐项解读每个指标，说明其含义\n" +
                "2. 标注异常指标（偏高用↑，偏低用↓）\n" +
                "3. 解释异常指标可能的原因\n" +
                "4. 给出通用的改善建议\n" +
                "5. 提示哪些指标需要复查或进一步检查\n\n" +
                "报告类型：" + (reportType != null ? reportType : "综合体检报告") + "\n" +
                "报告内容：\n" + (content != null ? content : "无内容");
    }

    /**
     * 上传报告请求体
     */
    @lombok.Data
    public static class UploadRequest {
        /** 报告类型（如：综合体检报告、血常规、尿常规等） */
        private String reportType;
        /** 报告内容 */
        private String reportContent;
    }

    /**
     * 分析报告请求体
     */
    @lombok.Data
    public static class AnalyzeRequest {
        /** 报告类型 */
        private String reportType;
        /** 报告内容 */
        private String reportContent;
    }
}
