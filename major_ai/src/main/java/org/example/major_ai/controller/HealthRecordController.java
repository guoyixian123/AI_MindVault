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

/**
 * 健康记录控制器
 * 提供健康数据的录入、查询和趋势分析接口
 *
 * API前缀：/api/health/records
 *
 * 支持的健康指标：
 * - blood_pressure_sys: 收缩压（高压）
 * - blood_pressure_dia: 舒张压（低压）
 * - blood_sugar: 血糖
 * - steps: 步数
 * - sleep_hours: 睡眠时长
 * - body_temperature: 体温
 */
@RestController
@RequestMapping("/api/health/records")
@RequiredArgsConstructor
public class HealthRecordController {

    /** 健康记录服务 */
    private final HealthRecordService healthRecordService;

    /**
     * 录入健康数据
     *
     * 请求：POST /api/health/records
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"recordDate": "2024-01-15", "bloodPressureSys": 120, "bloodPressureDia": 80, ...}
     * 响应：{"code": 200, "message": "健康数据录入成功", "data": {...}}
     *
     * @param user 当前登录用户
     * @param record 健康记录实体
     * @return 保存成功的记录
     */
    @PostMapping
    public ApiResponse<HealthRecordEntity> saveRecord(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody HealthRecordEntity record) {
        // 设置用户ID
        record.setUserId(user.getId());
        HealthRecordEntity saved = healthRecordService.save(record);
        return ApiResponse.success("健康数据录入成功", saved);
    }

    /**
     * 查询健康记录列表
     *
     * 请求：GET /api/health/records?days=7
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * @param user 当前登录用户
     * @param days 查询天数（默认7天）
     * @return 健康记录列表
     */
    @GetMapping
    public ApiResponse<List<HealthRecordEntity>> listRecords(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(defaultValue = "7") int days) {
        List<HealthRecordEntity> records = healthRecordService.listByUserId(user.getId(), days);
        return ApiResponse.success(records);
    }

    /**
     * 获取健康指标趋势数据
     *
     * 请求：GET /api/health/records/trend?indicator=blood_pressure_sys&days=30
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": {"indicator": "...", "days": 30, "data": [...], "warnings": [...]}}
     *
     * @param user 当前登录用户
     * @param indicator 指标名称
     * @param days 查询天数（默认30天）
     * @return 趋势数据（包含数据点和异常警告）
     */
    @GetMapping("/trend")
    public ApiResponse<Map<String, Object>> getTrend(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam String indicator,
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> trend = healthRecordService.getTrendData(user.getId(), indicator, days);
        return ApiResponse.success(trend);
    }
}
