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

@RestController
@RequestMapping("/api/health/reports")
@RequiredArgsConstructor
public class HealthReportController {

    private final HealthReportService healthReportService;
    private final ConsultantService consultantService;

    @PostMapping("/upload")
    public ApiResponse<HealthReportEntity> uploadReport(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody UploadRequest request) {
        HealthReportEntity report = healthReportService.saveReport(
                user.getId(), request.getReportType(), request.getReportContent());
        return ApiResponse.success("体检报告保存成功", report);
    }

    @GetMapping
    public ApiResponse<List<HealthReportEntity>> listReports(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<HealthReportEntity> reports = healthReportService.listByUserId(user.getId());
        return ApiResponse.success(reports);
    }

    @PostMapping("/analyze/{id}")
    public Flux<String> analyzeReport(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        HealthReportEntity report = healthReportService.findById(id);
        if (report == null || !report.getUserId().equals(user.getId())) {
            return Flux.just("报告不存在或无权限访问");
        }

        String prompt = buildAnalysisPrompt(report.getReportType(), report.getReportContent());
        return consultantService.chat("report-" + user.getId(), prompt);
    }

    @PostMapping("/analyze-latest")
    public Flux<String> analyzeLatest(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody AnalyzeRequest request) {
        // 先保存报告
        HealthReportEntity report = healthReportService.saveReport(
                user.getId(), request.getReportType(), request.getReportContent());

        String prompt = buildAnalysisPrompt(request.getReportType(), request.getReportContent());
        Long reportId = report.getId();
        AtomicReference<StringBuilder> analysisRef = new AtomicReference<>(new StringBuilder());

        return consultantService.chat("report-" + user.getId(), prompt)
                .doOnNext(chunk -> analysisRef.get().append(chunk))
                .doOnComplete(() -> {
                    String analysis = analysisRef.get().toString();
                    if (!analysis.isEmpty()) {
                        healthReportService.updateAnalysis(reportId, analysis);
                    }
                });
    }

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

    @lombok.Data
    public static class UploadRequest {
        private String reportType;
        private String reportContent;
    }

    @lombok.Data
    public static class AnalyzeRequest {
        private String reportType;
        private String reportContent;
    }
}
