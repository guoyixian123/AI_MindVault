package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.HealthRecordEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.HealthRecordService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health/records")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    @PostMapping
    public ApiResponse<HealthRecordEntity> saveRecord(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody HealthRecordEntity record) {
        record.setUserId(user.getId());
        HealthRecordEntity saved = healthRecordService.save(record);
        return ApiResponse.success("健康数据录入成功", saved);
    }

    @GetMapping
    public ApiResponse<List<HealthRecordEntity>> listRecords(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(defaultValue = "7") int days) {
        List<HealthRecordEntity> records = healthRecordService.listByUserId(user.getId(), days);
        return ApiResponse.success(records);
    }

    @GetMapping("/trend")
    public ApiResponse<Map<String, Object>> getTrend(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam String indicator,
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> trend = healthRecordService.getTrendData(user.getId(), indicator, days);
        return ApiResponse.success(trend);
    }
}
